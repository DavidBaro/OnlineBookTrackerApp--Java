package io.project.onlinebooktracker.userbooks;

import java.time.LocalDate;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import io.project.onlinebooktracker.book.Book;
import io.project.onlinebooktracker.book.BookRepository;
import io.project.onlinebooktracker.user.BooksByUser;
import io.project.onlinebooktracker.user.BooksByUserRepository;
import io.project.onlinebooktracker.usersignupandlogin.User;
import io.project.onlinebooktracker.usersignupandlogin.UserRepository;

@Controller
public class UserBooksController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserBooksRepository userBooksRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    BooksByUserRepository booksByUserRepository;

    @PostMapping("/addUserBook")
    public ModelAndView addBookForUser(
        @RequestBody MultiValueMap<String, String> formData,
        @AuthenticationPrincipal OAuth2User principal,
        HttpSession session
    ) {

        // Registered User Session
        System.out.println("USER EMAIL AS ID: " + session.getAttribute("userVerify"));

        

        if (session.getAttribute("userVerify") != null) {

            String userEmail = session.getAttribute("userVerify").toString();
            User principal2 = userRepository.findByName(userEmail);

            // Printing the found user in the console
            System.out.println("USER: " + principal2.toString());

            if (principal2 == null || principal2.getName() == null) {
                return null;
            }

            // Getting Book from BookRepository
            String bookId = formData.getFirst("bookId");
            Optional<Book> optionalBook = bookRepository.findById(bookId);
            if (!optionalBook.isPresent()) {
                return new ModelAndView("redirect:/");
            }
            Book book = optionalBook.get();

            // Saving book_by_user_and_bookid table of entity/class UserBooks
            UserBooks userBooks = new UserBooks();
            UserBooksPrimaryKey key = new UserBooksPrimaryKey();
            // String userId = principal.getAttribute("name");
            String userId = principal2.getName();
            key.setUserId(userId);
            key.setBookId(bookId);

            userBooks.setKey(key);

            int rating = Integer.parseInt(formData.getFirst("rating"));

            userBooks.setStartedDate(LocalDate.parse(formData.getFirst("startedDate")));
            userBooks.setCompletedDate(LocalDate.parse(formData.getFirst("completedDate")));
            userBooks.setRating(Integer.parseInt(formData.getFirst("rating")));
            userBooks.setReadingStatus(formData.getFirst("readingStatus"));

            userBooksRepository.save(userBooks);

            // Saving to table "books_by_user" of entity/class BooksByUser
            BooksByUser booksByUser = new BooksByUser();
            booksByUser.setId(userId);
            booksByUser.setBookId(bookId);
            booksByUser.setBookName(book.getName());
            booksByUser.setCoverIds(book.getCoverIds());
            booksByUser.setAuthorNames(book.getAuthorNames());
            booksByUser.setReadingStatus(formData.getFirst("readingStatus"));
            booksByUser.setRating(rating);
            booksByUserRepository.save(booksByUser);

            return new ModelAndView("redirect:/books/" + bookId);

        } else {
            if (principal == null || principal.getAttribute("name") == null) {
                return null;
            }

            // Getting Book from BookRepository
            String bookId = formData.getFirst("bookId");
            Optional<Book> optionalBook = bookRepository.findById(bookId);
            if (!optionalBook.isPresent()) {
                return new ModelAndView("redirect:/");
            }
            Book book = optionalBook.get();

            // Saving book_by_user_and_bookid table of entity/class UserBooks
            UserBooks userBooks = new UserBooks();
            UserBooksPrimaryKey key = new UserBooksPrimaryKey();
            String userId = principal.getAttribute("name");
            key.setUserId(userId);
            key.setBookId(bookId);

            userBooks.setKey(key);

            int rating = Integer.parseInt(formData.getFirst("rating"));

            userBooks.setStartedDate(LocalDate.parse(formData.getFirst("startedDate")));
            userBooks.setCompletedDate(LocalDate.parse(formData.getFirst("completedDate")));
            userBooks.setRating(Integer.parseInt(formData.getFirst("rating")));
            userBooks.setReadingStatus(formData.getFirst("readingStatus"));

            userBooksRepository.save(userBooks);

            // Saving to table "books_by_user" of entity/class BooksByUser
            BooksByUser booksByUser = new BooksByUser();
            booksByUser.setId(userId);
            booksByUser.setBookId(bookId);
            booksByUser.setBookName(book.getName());
            booksByUser.setCoverIds(book.getCoverIds());
            booksByUser.setAuthorNames(book.getAuthorNames());
            booksByUser.setReadingStatus(formData.getFirst("readingStatus"));
            booksByUser.setRating(rating);
            booksByUserRepository.save(booksByUser);

            return new ModelAndView("redirect:/books/" + bookId);
        }
    }
}
