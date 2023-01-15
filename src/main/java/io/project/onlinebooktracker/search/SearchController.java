package io.project.onlinebooktracker.search;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.project.onlinebooktracker.usersignupandlogin.User;
import io.project.onlinebooktracker.usersignupandlogin.UserRepository;
import reactor.core.publisher.Mono;

@Controller
public class SearchController {

    @Autowired
    UserRepository userRepository;

    private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

    private final WebClient webClient;

    // Constructor for WebClient
    public SearchController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.exchangeStrategies(ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs()
            .maxInMemorySize(16 * 1024 * 1024)).build())
            .baseUrl("http://openlibrary.org/search.json").build();
    }
    
    @GetMapping(value = "/search")
    public String getSearchResults(@RequestParam String query, Model model, HttpSession session, @AuthenticationPrincipal OAuth2User principal) {

        if (session.getAttribute("userVerify") != null) {
            String userEmail = session.getAttribute("userVerify").toString();
            // Getting the user
            User userInfo = userRepository.findByName(userEmail);

            // Getting the user's FirstName and last name
            String userName = userInfo.getUserFirstName() + userInfo.getUserLastName();
            model.addAttribute("userName", userName);
            System.out.println("USER NAME: " + userName);
        } else {
            if (principal != null && principal.getAttribute("name") != null) {
                model.addAttribute("userName", principal.getAttribute("name"));
            } else {
                model.addAttribute("userName", null);
            }
        }

        Mono<SearchResult> resultsMono = this.webClient.get()
            .uri("?q={query}", query)
            .retrieve().bodyToMono(SearchResult.class);
            
            // Getting the search results
            SearchResult result = resultsMono.block();

            // Showing limited results from the original search results
            List<SearchResultBook> books = result.getDocs().stream().limit(10)
                .map(bookResult -> {
                    bookResult.setKey(bookResult.getKey().replace("/works/", ""));
                    String coverId = bookResult.getCover_i();
                    if (StringUtils.hasText(coverId)) {
                        coverId = COVER_IMAGE_ROOT + coverId + "-M.jpg";
                    } else {
                        coverId = "/img/nobookimage.png";
                    }

                    bookResult.setCover_i(coverId);
                    return bookResult;
                })
                .collect(Collectors.toList());

            model.addAttribute("searchResults", books);

            if (session.getAttribute("userVerify") == null) {
                // Check if user is logged in
                if (principal == null) {
                    return "non-user/non_user_search";
                }
            }

        return "search";
    }
}
