package io.project.onlinebooktracker.usersignupandlogin;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import io.project.onlinebooktracker.book.BookRepository;
import io.project.onlinebooktracker.helper.Messages;
import io.project.onlinebooktracker.user.BooksByUser;
import io.project.onlinebooktracker.user.BooksByUserRepository;
import io.project.onlinebooktracker.userbooks.UserBooksRepository;

@Controller
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserBooksRepository userBooksRepository;

    @Autowired
    BooksByUserRepository booksByUserRepository;

    // private final WebClient webClient;

    private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

    // Sign up page
    @GetMapping("/sign-up")
    public String signUp() {
        return "signup";
    }

    // Log in page
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Sign up process
    @PostMapping("/sign-up-process")
    public String processSignUp(@RequestBody MultiValueMap<String, String> formData, Model model, HttpSession session) {

        // Saving table user_by_id for User class/entity
        User user = new User();

        user.setId(Uuids.timeBased());

        user.setUserFirstName(formData.getFirst("userFirstName"));
        user.setUserLastName(formData.getFirst("userLastName"));
        user.setName(formData.getFirst("name"));
        user.setUserPassword(formData.getFirst("userPassword"));

        userRepository.save(user);

        // Successfully registered message
        session.setAttribute("message", new Messages("Succesfully Registered", "success"));

        return "redirect:/login";
    }

    // Log in process handler
    @PostMapping("/do-login")
    // public String processLogIn(@RequestParam("name") String userName, @RequestParam("userPassword") String userPassword, HttpSession session, Model model) {
    public String processLogIn(@RequestBody MultiValueMap<String, String> formData, HttpSession session, Model model) {

        System.out.println("USER EMAIL "+ formData.getFirst("name"));
		System.out.println("USER PASSWORD " + formData.getFirst("userPassword"));

        // Checking the email id
        // User user = new User();

        String userEmail = formData.getFirst("name");
        String userPass = formData.getFirst("userPassword");

        User userInfo = userRepository.findByName(userEmail);

        // if principal is null then return index
        if (userInfo == null || userInfo.getName() == null) {
            return "index";
        }

        // Checking if the password matches
        String correctPassword = userInfo.getUserPassword();

        // Getting the user uuid (id)
        String userVerify = userInfo.getName();

        // Printing the found user in the console
        System.out.println("USER: " + userInfo.toString());

        if (correctPassword.matches(userPass)) {

            String userId = userInfo.getName();
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

            // Getting the user's FirstName and last name
            String userName = userInfo.getUserFirstName() + userInfo.getUserLastName();
            model.addAttribute("userName", userName);

            // Setting session
            session.setAttribute("userVerify", userVerify);

            // return "home";
            return "redirect:/registered-user";
        }

        // Login error message

        return "redirect:/login";
    }

    // Returning User Home Page
    @GetMapping("/registered-user")
    // public String processLogIn(@RequestParam("name") String userName, @RequestParam("userPassword") String userPassword, HttpSession session, Model model) {
    public String processUser(HttpSession session, Model model) {

        System.out.println("USER EMAIL id: " + session.getAttribute("userVerify"));
        String userEmail = session.getAttribute("userVerify").toString();

        User userInfo = userRepository.findByName(userEmail);

        // if principal is null then return index
        if (userInfo == null || userInfo.getName() == null) {
            return "null";
        }

        // Printing the found user in the console
        System.out.println("USER: " + userInfo.toString());

        String userId = userInfo.getName();
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

        // Getting the user's FirstName and last name
        String userName = userInfo.getUserFirstName() + userInfo.getUserLastName();
        model.addAttribute("userName", userName);
        System.out.println("USER NAME: " + userName);

        return "home";

    }

    // Change Password Handler

    @GetMapping("/change-password")
    public String changePassword(HttpSession session, Model model) {

        return "changepassword";
    }

    // Save changed password handler
    @PostMapping("/do-change-password")
    public String saveChangedPassword(@RequestParam("userPassword") String enteredOldPassword, @RequestParam("userNewPassword") String enteredNewPassword,HttpSession session, Model model) {

        String userEmail = session.getAttribute("userVerify").toString();
        // Getting the user
        User userInfo = userRepository.findByName(userEmail);

        // Getting the user's Old Password
        String oldPassword = userInfo.getUserPassword();

        // Check if the entered old passoword is the actual old password
        if (oldPassword.matches(enteredOldPassword)) {
            userInfo.setUserPassword(enteredNewPassword);
            userRepository.save(userInfo);

            // Success message
            session.setAttribute("message", new Messages("New Password Saved", "alert-success"));
        } else {
            session.setAttribute("message", new Messages("Incorrect Password", "alert-danger"));
            return "redirect:/changepassword";
        }

        return "redirect:/login";
    }
}
