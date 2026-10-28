package app;

import service.UserService;
import exception.InvalidInputException;

public class Phase2ValidationTest {
    public static void main(String[] args) {
        UserService service = new UserService();

        System.out.println("Testing Expense Amount Validation");
        System.out.println("-----------------------------");

        double[] amounts = {100.0, 0.0, -50.0};

        for (double amount : amounts) {
            try {
                service.validateExpenseAmount(amount);
                System.out.println("Amount " + amount + ": Accepted");
            } catch (InvalidInputException e) {
                System.out.println("Amount " + amount + ": Rejected");
            }
        }

        System.out.println();
        System.out.println("Phase 2 validation test completed.");
    }
}