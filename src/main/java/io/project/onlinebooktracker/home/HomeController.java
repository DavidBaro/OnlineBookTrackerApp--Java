package io.project.onlinebooktracker.home;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.project.onlinebooktracker.user.BooksByUser;
import io.project.onlinebooktracker.user.BooksByUserRepository;
import io.project.onlinebooktracker.usersignupandlogin.User;
import io.project.onlinebooktracker.usersignupandlogin.UserRepository;

@Controller
public class HomeController {

    @Autowired
    BooksByUserRepository booksByUserRepository;

    @Autowired
    UserRepository userRepository;

    private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model, HttpSession session) {

        String userVerify = null;

        // Checking the user if it's logged in
        if (principal == null || principal.getAttribute("name") == null) {
            return "index";
        }
        
        String userId = principal.getAttribute("name");
        Slice<BooksByUser> booksSlice = booksByUserRepository.findAllById(userId, CassandraPageRequest.of(0, 100));
        List<BooksByUser> booksByUser = booksSlice.getContent();
        booksByUser = booksByUser.stream().distinct().map(book -> {
            String coverImageUrl = "/img/nobookimage.png";
            if (book.getCoverIds() != null & book.getCoverIds().size() > 0) {
                coverImageUrl = COVER_IMAGE_ROOT + book.getCoverIds().get(0) + "-M.jpg";
            }
            book.setCoverUrl(coverImageUrl);
            return book;
        }).collect(Collectors.toList());
        model.addAttribute("books", booksByUser);
        String title = "MyBookList";
        model.addAttribute("title", title);

        // Setting session for userIdentification
        session.setAttribute("userVerify", userVerify);

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

        return "home";
    }   
}
