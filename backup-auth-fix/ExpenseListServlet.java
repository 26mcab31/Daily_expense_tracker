
package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import dao.ExpenseDAO;
import model.Expense;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/expenses")
public class ExpenseListServlet extends HttpServlet {

    private final ExpenseDAO expenseDAO = new ExpenseDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            long userId = (Long) request.getSession(false)
                    .getAttribute("userId");
            List<Expense> expenses = expenseDAO.getExpensesForUser(userId);
            var out = response.getWriter();

            out.print("[");
            for (int i = 0; i < expenses.size(); i++) {
                Expense e = expenses.get(i);

                if (i > 0) out.print(",");

                out.print("{\"expenseDate\":\"" + escape(e.getExpenseDate())
                    + "\",\"category\":\"" + escape(e.getCategory())
                    + "\",\"description\":\"" + escape(e.getDescription())
                    + "\",\"amount\":" + e.getAmount() + "}");
            }
            out.print("]");

        } catch (SQLException | ClassNotFoundException e) {
            getServletContext().log("Unable to load expenses", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"Unable to load expenses.\"}");
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
