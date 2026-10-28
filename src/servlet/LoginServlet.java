package servlet;

import dao.UserDAO;
import exception.InvalidInputException;
import model.UserAccount;
import security.PasswordUtil;
import service.UserOperations;
import service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

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
        request.getRequestDispatcher("/login.html").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String identifier = request.getParameter("username");
        String password = request.getParameter("password");
        identifier = identifier == null ? null : identifier.trim();
        try {
            userOperations.validateLogin(identifier, password);
        } catch (InvalidInputException e) {
            redirectToLogin(request, response);
            return;
        }

        char[] passwordChars = password.toCharArray();
        try {
            UserAccount user = userDAO.findByUsernameOrEmail(identifier);
            if (user == null
                    || !PasswordUtil.verify(passwordChars,
                    user.getPasswordHash())) {
                HttpSession existingSession = request.getSession(false);
                if (existingSession != null) {
                    existingSession.invalidate();
                }
                redirectToLogin(request, response);
                return;
            }

            HttpSession session = request.getSession(true);
            request.changeSessionId();
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setMaxInactiveInterval(30 * 60);
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (SQLException | ClassNotFoundException
                 | GeneralSecurityException e) {
            log("Unable to authenticate user", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to process login right now.");
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }

    private void redirectToLogin(HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath()
                + "/login?error=1");
    }
}
