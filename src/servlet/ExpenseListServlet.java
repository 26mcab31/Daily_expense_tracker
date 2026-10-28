
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
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/expenses")
public class ExpenseListServlet extends HttpServlet {

    private final ExpenseDAO expenseDAO = new ExpenseDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Long)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"error\":\"Authentication required.\"}");
            return;
        }

        try {
            long userId = (Long) session.getAttribute("userId");
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
