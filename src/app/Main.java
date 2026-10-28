package app;

import model.User;
import model.Expense;
import service.UserService;
import service.UserOperations;
import exception.InvalidInputException;

public class Main {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println("   DAILY EXPENSE TRACKER");
        System.out.println("=================================");

        try {

            // Demonstrate validation without storing example credentials.
            UserService userService = new UserService();
            UserOperations userOperations = userService;
            User user = new User("sample.user", "",
                    "sample@example.invalid");
            try {
                userOperations.validateRegistration(user);
            } catch (InvalidInputException e) {
                System.out.println(
                        "Registration validation rejected incomplete input."
                );
            }

            // Create valid Expense object
            Expense expense = new Expense(
                    1,
                    "2026-10-06",
                    "Food",
                    "Lunch",
                    100.00
            );

            // Validate Expense
            if (!expense.isValid()) {
                throw new InvalidInputException(
                        "Invalid expense details."
                );
            }

            // Validate expense amount using custom exception
            userService.validateExpenseAmount(
                    expense.getAmount()
            );

            System.out.println();
            System.out.println("Expense Details");
            System.out.println("-----------------------------");
            System.out.println(
                    "Date        : " + expense.getExpenseDate()
            );
            System.out.println(
                    "Category    : " + expense.getCategory()
            );
            System.out.println(
                    "Description : " + expense.getDescription()
            );
            System.out.println(
                    "Amount      : Rs. " + expense.getAmount()
            );

            // Test custom exception
            System.out.println();
            System.out.println("Testing Custom Exception");
            System.out.println("-----------------------------");

            try {

                // Invalid amount for testing
                double invalidAmount = 0;

                userService.validateExpenseAmount(
                        invalidAmount
                );

                System.out.println(
                        "Invalid amount accepted."
                );

            } catch (InvalidInputException e) {

                System.out.println(
                        "Custom Exception Caught: "
                        + e.getMessage()
                );
            }

            System.out.println();
            System.out.println(
                    "Phase 2 exception handling test completed."
            );

        } catch (InvalidInputException e) {

            System.out.println(
                    "Error: " + e.getMessage()
            );

        } catch (Exception e) {

            System.out.println(
                    "Unexpected error: " + e.getMessage()
            );
        }
    }
}