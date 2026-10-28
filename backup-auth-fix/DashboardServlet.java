package servlet;

import dao.ExpenseDAO;
import model.Expense;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ExpenseDAO expenseDAO;

    @Override
    public void init() {
        expenseDAO = new ExpenseDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        Long userId = (Long) request.getSession(false).getAttribute("userId");
        List<Expense> expenses = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal todayTotal = BigDecimal.ZERO;
        boolean loadError = false;
        String today = LocalDate.now().toString();

        try {
            expenses = expenseDAO.getExpensesForUser(userId);
            for (Expense expense : expenses) {
                BigDecimal amount = BigDecimal.valueOf(expense.getAmount());
                total = total.add(amount);
                if (today.equals(expense.getExpenseDate())) {
                    todayTotal = todayTotal.add(amount);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            log("Unable to load dashboard expenses for user " + userId, e);
            loadError = true;
        }

        request.setAttribute("expenseTotal", total);
        request.setAttribute("transactionCount", expenses.size());
        request.setAttribute("todayTotal", todayTotal);
        request.setAttribute("recentExpenses",
                expenses.subList(0, Math.min(5, expenses.size())));
        request.setAttribute("dashboardLoadError", loadError);
        request.getRequestDispatcher("/WEB-INF/dashboard.jsp")
                .forward(request, response);
    }
}
