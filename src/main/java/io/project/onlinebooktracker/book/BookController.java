package io.project.onlinebooktracker.book;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.project.onlinebooktracker.userbooks.UserBooks;
import io.project.onlinebooktracker.userbooks.UserBooksPrimaryKey;
import io.project.onlinebooktracker.userbooks.UserBooksRepository;
import io.project.onlinebooktracker.usersignupandlogin.User;
import io.project.onlinebooktracker.usersignupandlogin.UserRepository;
import io.project.onlinebooktracker.wishlist.WishListBook;
import io.project.onlinebooktracker.wishlist.WishListRepository;

@Controller
public class BookController {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserBooksRepository userBooksRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WishListRepository wishListRepository;

    private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";
    
    @GetMapping(value = "/books/{bookId}")
    public String getBook(@PathVariable String bookId, Model model, @AuthenticationPrincipal OAuth2User principal, HttpSession session) {

        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            String coverImageUrl = "/img/nobookimage.png";
            if (book.getCoverIds() != null & book.getCoverIds().size() > 0) {
                coverImageUrl = COVER_IMAGE_ROOT + book.getCoverIds().get(0) + "-L.jpg";
            }
            model.addAttribute("coverImage", coverImageUrl);
            model.addAttribute("book", book);

            // Object curPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // Registered User Session
            System.out.println("USER EMAIL as id: " + session.getAttribute("userVerify"));

            if (session.getAttribute("userVerify") != null) {

                String userEmail = session.getAttribute("userVerify").toString();
                User principal2 = userRepository.findByName(userEmail);

                // Getting the user's FirstName and last name for viewing the logged in user
                String userName = principal2.getUserFirstName() + principal2.getUserLastName();
                model.addAttribute("userName", userName);
                System.out.println("USER NAME: " + userName);

                // Printing the found user in the console
                System.out.println("USER: " + principal2.toString());

                // Getting logged in user's info

                if (principal2 != null && principal2.getName() != null) {

                    String userId = principal2.getName();
                    model.addAttribute("loginId", userId);
                    UserBooksPrimaryKey key = new UserBooksPrimaryKey();
                    key.setBookId(bookId);
                    key.setUserId(userId);
                    Optional<UserBooks> userBooks = userBooksRepository.findById(key);
                    
                    if (userBooks.isPresent()) {
                        model.addAttribute("userBooks", userBooks.get());
                    } else {
                        model.addAttribute("userBooks", new UserBooks());
                    }

                    // Check if the book is already in the user wishlist
                    WishListBook wishListBook = wishListRepository.findByIdAndBookId(userId, bookId);
                    if (wishListBook != null) {
                        model.addAttribute("wishListBook", wishListBook);
                    } else {
                        model.addAttribute("wishListBook", wishListBook);
                    }
                    
                }

            } else {

                // Getting logged in user's info
            
                if (principal != null && principal.getAttribute("name") != null) {

                    String userId = principal.getAttribute("name");

                    // Getting the user name for viewing the logged in user
                    model.addAttribute("userName", userId);

                    model.addAttribute("loginId", userId);
                    UserBooksPrimaryKey key = new UserBooksPrimaryKey();
                    key.setBookId(bookId);
                    key.setUserId(userId);
                    Optional<UserBooks> userBooks = userBooksRepository.findById(key);
                    
                    if (userBooks.isPresent()) {
                        model.addAttribute("userBooks", userBooks.get());
                    } else {
                        model.addAttribute("userBooks", new UserBooks());
                    }

                    // Check if the book is already in the user wishlist
                    WishListBook wishListBook = wishListRepository.findByIdAndBookId(userId, bookId);
                    if (wishListBook != null) {
                        model.addAttribute("wishListBook", wishListBook);
                    } else {
                        model.addAttribute("wishListBook", wishListBook);
                    }

                } else {
                    model.addAttribute("userName", null);
                    return "non-user/non_user_book";
                }
            }
            
            return "book";
        }
        return "book-not-found";
    }
}
