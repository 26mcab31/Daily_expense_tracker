package dao;

import database.DBConnection;
import model.Expense;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    public boolean addExpense(Expense expense, long userId)
            throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO expenses "
                + "(expense_date, category, description, amount, user_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        if (expense == null || !expense.isValid() || userId <= 0) {
            return false;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, expense.getExpenseDate());
            ps.setString(2, expense.getCategory());
            ps.setString(3, expense.getDescription());
            ps.setDouble(4, expense.getAmount());
            ps.setLong(5, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Expense> getExpensesForUser(long userId)
            throws SQLException, ClassNotFoundException {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT expense_date, category, description, amount "
                   + "FROM expenses WHERE user_id = ? "
                   + "ORDER BY expense_date DESC, expense_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Expense expense = new Expense();

                    expense.setExpenseDate(rs.getString("expense_date"));
                    expense.setCategory(rs.getString("category"));
                    expense.setDescription(rs.getString("description"));
                    expense.setAmount(rs.getDouble("amount"));

                    expenses.add(expense);
                }
            }
        }
        return expenses;
    }
}