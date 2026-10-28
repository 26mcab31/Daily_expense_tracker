package servlet;

import dao.UserDAO;
import exception.InvalidInputException;
import model.User;
import security.PasswordUtil;
import service.UserOperations;
import service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserDAO userDAO;
    private UserOperations userOperations;

    @Override
    public void init() {
        userDAO = new UserDAO();
        userOperations = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher("/register.html").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String username = trim(request.getParameter("username"));
        String email = trim(request.getParameter("email"));
        String password = request.getParameter("password");
        String confirmation = request.getParameter("confirmPassword");

        if (username == null || username.isEmpty()
                || email == null || email.isEmpty()
                || password == null || password.isEmpty()
                || confirmation == null || confirmation.isEmpty()) {
            redirectWithMessage(request, response, "required_fields");
            return;
        }
        email = email.toLowerCase(Locale.ROOT);
        if (!password.equals(confirmation)) {
            redirectWithMessage(request, response, "password_mismatch");
            return;
        }

        try {
            userOperations.validateRegistration(
                    new User(username, password, email));
        } catch (InvalidInputException e) {
            redirectWithMessage(request, response, e.getMessage());
            return;
        }

        char[] passwordChars = password.toCharArray();
        try {
            String passwordHash = PasswordUtil.hash(passwordChars);
            if (!userDAO.create(username, email, passwordHash)) {
                redirectWithMessage(request, response, "account_exists");
                return;
            }
            response.sendRedirect(request.getContextPath()
                    + "/login?registered=1");
        } catch (SQLException | ClassNotFoundException
                 | GeneralSecurityException e) {
            log("Unable to register user", e);
            redirectWithMessage(request, response, "registration_failed");
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private void redirectWithMessage(HttpServletRequest request,
                                     HttpServletResponse response,
                                     String message)
            throws IOException {
        response.sendRedirect(request.getContextPath()
                + "/register?message=" + message);
    }
}
