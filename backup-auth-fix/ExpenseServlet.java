
package servlet;

import dao.ExpenseDAO;
import model.Expense;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/expenses")
public class ExpenseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

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

        try {
            long userId = (Long) request.getSession(false)
                    .getAttribute("userId");
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

        String date = request.getParameter("expenseDate");
        String category = request.getParameter("category");
        String description = request.getParameter("description");
        String amountText = request.getParameter("amount");

        // Validate required fields
        if (date == null || date.trim().isEmpty()
                || category == null || category.trim().isEmpty()
                || description == null || description.trim().isEmpty()
                || amountText == null || amountText.trim().isEmpty()) {

            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Missing or empty expense fields. Check the HTML form."
            );
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

            if (!Double.isFinite(amount) || amount <= 0) {
                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Amount must be a valid number greater than zero."
                );
                return;
            }

            Expense expense = new Expense();
            expense.setExpenseDate(date.trim());
            expense.setCategory(category.trim());
            expense.setDescription(description.trim());
            expense.setAmount(amount);

            long userId = (Long) request.getSession(false)
                    .getAttribute("userId");
            if (expenseDAO.addExpense(expense, userId)) {
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Expense saved successfully!");
            } else {
                response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Expense could not be saved."
                );
            }

        } catch (NumberFormatException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Please enter a valid amount."
            );
        } catch (Exception e) {
            log("Error saving expense", e);

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "An error occurred while saving the expense."
            );
        }
    }

    // Escape special characters for JSON
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
