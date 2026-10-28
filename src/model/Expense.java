package model;

public class Expense {

    private int expenseId;
    private String expenseDate;
    private String category;
    private String description;
    private double amount;

    // Default constructor
    public Expense() {
    }

    // Parameterized constructor
    public Expense(int expenseId, String expenseDate, String category,
                   String description, double amount) {
        this.expenseId = expenseId;
        this.expenseDate = expenseDate;
        this.category = category;
        this.description = description;
        this.amount = amount;
    }

    // Getters
    public int getExpenseId() {
        return expenseId;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    // Setters
    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    // Basic validation
    public boolean isValid() {
        return expenseDate != null && !expenseDate.isBlank()
                && category != null && !category.isBlank()
                && description != null && !description.isBlank()
                && amount > 0;
    }
}