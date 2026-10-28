
package servlet;

import dao.ExpenseDAO;
import model.Expense;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.List;

@WebServlet("/expenses")
public class ExpenseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final BigDecimal MAX_AMOUNT =
            new BigDecimal("99999999.99");
    private static final Set<String> CATEGORIES = Set.of(
            "Food", "Transport", "Shopping", "Bills",
            "Education", "Health", "Entertainment", "Other");

    private ExpenseDAO expenseDAO;

    @Override
    public void init() throws ServletException {
        expenseDAO = new ExpenseDAO();
    }

    // GET: Display expenses as JSON
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Long)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Authentication required.\"}");
            return;
        }

        try {
            long userId = (Long) session.getAttribute("userId");
            List<Expense> expenses = expenseDAO.getExpensesForUser(userId);

            StringBuilder json = new StringBuilder();
            json.append("[");

            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"expenseDate\":\"")
                    .append(escapeJson(expense.getExpenseDate()))
                    .append("\",")
                    .append("\"category\":\"")
                    .append(escapeJson(expense.getCategory()))
                    .append("\",")
                    .append("\"description\":\"")
                    .append(escapeJson(expense.getDescription()))
                    .append("\",")
                    .append("\"amount\":")
                    .append(expense.getAmount())
                    .append("}");
            }

            json.append("]");
            response.getWriter().write(json.toString());

        } catch (Exception e) {
            log("Error retrieving expenses", e);

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to retrieve expenses."
            );
        }
    }

    // POST: Save a new expense
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String date = trim(request.getParameter("expenseDate"));
        String category = trim(request.getParameter("category"));
        String description = trim(request.getParameter("description"));
        String amountText = trim(request.getParameter("amount"));

        if (date == null || category == null || description == null
                || amountText == null) {
            writeMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Please complete all expense fields.");
            return;
        }

        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            writeMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Please enter a valid expense date.");
            return;
        }
        if (!CATEGORIES.contains(category)) {
            writeMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Please select a valid expense category.");
            return;
        }
        if (description.length() > 255) {
            writeMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Expense description must be 255 characters or fewer.");
            return;
        }

        final BigDecimal amountValue;
        try {
            amountValue = new BigDecimal(amountText)
                    .setScale(2, RoundingMode.UNNECESSARY);
        } catch (NumberFormatException | ArithmeticException e) {
            writeMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Enter a valid amount with no more than two decimal places.");
            return;
        }
        if (amountValue.signum() <= 0) {
            writeMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Amount must be greater than zero.");
            return;
        }
        if (amountValue.compareTo(MAX_AMOUNT) > 0) {
            writeMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Amount cannot exceed 99,999,999.99.");
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Long)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Authentication required.\"}");
            return;
        }

        try {
            Expense expense = new Expense();
            expense.setExpenseDate(date);
            expense.setCategory(category);
            expense.setDescription(description);
            expense.setAmount(amountValue.doubleValue());

            long userId = (Long) session.getAttribute("userId");
            if (expenseDAO.addExpense(expense, userId)) {
                writeMessage(response, HttpServletResponse.SC_OK,
                        "Expense saved successfully.");
            } else {
                writeMessage(response,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Expense could not be saved.");
            }

        } catch (Exception e) {
            log("Error saving expense", e);

            writeMessage(response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An error occurred while saving the expense.");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private void writeMessage(HttpServletResponse response, int status,
                              String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (character < 0x20
                            || character == '\u2028'
                            || character == '\u2029') {
                        escaped.append(String.format("\\u%04x",
                                (int) character));
                    } else {
                        escaped.append(character);
                    }
            }
        }
        return escaped.toString();
    }
}
