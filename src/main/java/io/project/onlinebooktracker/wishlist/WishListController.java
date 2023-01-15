package io.project.onlinebooktracker.wishlist;

import java.util.List;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestParam;

import io.project.onlinebooktracker.book.Book;
import io.project.onlinebooktracker.book.BookRepository;
import io.project.onlinebooktracker.usersignupandlogin.User;
import io.project.onlinebooktracker.usersignupandlogin.UserRepository;

@Controller
public class WishListController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    WishListRepository wishListRepository;

    @Autowired
    BookRepository bookRepository;

    private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

    // Wish List View Handler
    @GetMapping("/wishlist")
    public String viewWishList(
        @AuthenticationPrincipal OAuth2User principal,
        HttpSession session, Model model
    ) {

        if (session.getAttribute("userVerify") != null) {

            String userEmail = session.getAttribute("userVerify").toString();
            User userInfo = userRepository.findByName(userEmail);

            // if principal is null then return index
            if (userInfo == null || userInfo.getName() == null) {
                return "null";
            }

            // Printing the found user in the console
            System.out.println("USER: " + userInfo.toString());

            String userId = userInfo.getName();
            Slice<WishListBook> booksSlice = wishListRepository.findAllById(userId, CassandraPageRequest.of(0, 100));
            List<WishListBook> wishlistedBooksByUser = booksSlice.getContent();
            wishlistedBooksByUser = wishlistedBooksByUser.stream().distinct().map(book -> {
                String coverImageUrl = "/img/nobookimage.png";
                if (book.getCoverId() != null & book.getCoverId().size() > 0) {
                    coverImageUrl = COVER_IMAGE_ROOT + book.getCoverId().get(0) + "-M.jpg";
                }
                book.setCoverUrl(coverImageUrl);
                return book;
            }).collect(Collectors.toList());
            model.addAttribute("books", wishlistedBooksByUser);
            String title = "MyBookList";
            model.addAttribute("title", title);

            // Getting the user's FirstName and last name
            String userName = userInfo.getUserFirstName() + userInfo.getUserLastName();
            model.addAttribute("userName", userName);

            return "wishlist";

        } else {

            // Checking the user if it's logged in
            if (principal == null || principal.getAttribute("name") == null) {
                return "index";
            }
            
            String userId = principal.getAttribute("name");
            Slice<WishListBook> booksSlice = wishListRepository.findAllById(userId, CassandraPageRequest.of(0, 100));
            List<WishListBook> wishlistedBooksByUser = booksSlice.getContent();
            wishlistedBooksByUser = wishlistedBooksByUser.stream().distinct().map(book -> {
                String coverImageUrl = "/img/nobookimage.png";
                if (book.getCoverId() != null & book.getCoverId().size() > 0) {
                    coverImageUrl = COVER_IMAGE_ROOT + book.getCoverId().get(0) + "-M.jpg";
                }
                book.setCoverUrl(coverImageUrl);
                return book;
            }).collect(Collectors.toList());
            model.addAttribute("books", wishlistedBooksByUser);
            String title = "MyBookList";
            model.addAttribute("title", title);

            // User Name
            model.addAttribute("userName", principal.getAttribute("name"));

            return "wishlist";
        }
        
    }


    // Add To or Delete From Wishlist Handler
    @GetMapping("/wishlist-user-book")
    public String addToOrDeleteFromWishList(
        @RequestParam("bookId") String bookId,
        @AuthenticationPrincipal OAuth2User principal,
        HttpSession session
    ) {
        
        
        // Registered User Session
        System.out.println("USER EMAIL AS ID: " + session.getAttribute("userVerify"));
        // Print the bookId
        System.out.println("BOOK ID: " + bookId);

        if (session.getAttribute("userVerify") != null) {

            String userEmail = session.getAttribute("userVerify").toString();
            User principal2 = userRepository.findByName(userEmail);

            // Printing the found user in the console
            System.out.println("USER: " + principal2.toString());

            String userId = principal2.getName();

            if (principal2 == null || userId == null) {
                return null;
            }

            // Getting Book from BookRepository
            // String bookId = formData.getFirst("bookId");
            Optional<Book> optionalBook = bookRepository.findById(bookId);
            if (!optionalBook.isPresent()) {
                // return new ModelAndView("redirect:/");
                return "redirect:/";
            }
            Book book = optionalBook.get();

            // Checking if the user has already added the book
            WishListBook userInWishList = wishListRepository.findByIdAndBookId(userId, bookId);
            // if user has not added a book already then book will be added right away
            if (userInWishList == null) {

                // Saving book to the table - wishlistedbooks_by_user table of entity/class WishListBook
                WishListBook wishListBook = new WishListBook();

                wishListBook.setId(userId);
                wishListBook.setBookId(bookId);
                wishListBook.setBookName(book.getName());
                wishListBook.setCoverId(book.getCoverIds());
                wishListBook.setAuthorNames(book.getAuthorNames());
                wishListRepository.save(wishListBook);
            } else {
                // If the userId with same boodId exists, delete the book from the table
                wishListRepository.delete(userInWishList);
            }

            // return new ModelAndView("redirect:/books/" + bookId);
            return "redirect:/books/" + bookId;

        } else {
            if (principal == null || principal.getAttribute("name") == null) {
                return null;
            }

            String userId = principal.getAttribute("name");
            // Getting Book from BookRepository
            // String bookId = formData.getFirst("bookId");
            Optional<Book> optionalBook = bookRepository.findById(bookId);
            if (!optionalBook.isPresent()) {
                // return new ModelAndView("redirect:/");
                return "redirect:/";
            }
            Book book = optionalBook.get();

            // Checking if the user has already added a book
            WishListBook userInWishList = wishListRepository.findByIdAndBookId(userId, bookId);
            // if user has not added a book already then book will be added right away
            if (userInWishList == null) {

                // Saving book to the table - wishlistedbooks_by_user table of entity/class WishListBook
                WishListBook wishListBook = new WishListBook();

                wishListBook.setId(userId);
                wishListBook.setBookId(bookId);
                wishListBook.setBookName(book.getName());
                wishListBook.setCoverId(book.getCoverIds());
                wishListBook.setAuthorNames(book.getAuthorNames());
                wishListRepository.save(wishListBook);
            } else {
                // If the userId with same boodId exists, delete the book from the table
                wishListRepository.delete(userInWishList);
            }

            // return new ModelAndView("redirect:/books/" + bookId);
            return "redirect:/books/" + bookId;
        }
    }
}
