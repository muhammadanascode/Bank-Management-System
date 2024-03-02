package Bank;

import Account.Account;
import Transaction.Transaction;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bson.Document;

public class Bank {

  private static final String EMAIL_REGEX =
    "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
  private static final String PASSWORD_REGEX =
    "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

  private static void deposit(Scanner sc) {
    try {
      System.out.print("Enter Account Number: ");
      String accountNumber = sc.next();
      Account acc1 = new Account(accountNumber);

      System.out.print("Enter Amount to Deposit: ");
      double amount = sc.nextDouble();

      if (amount > 0) {
        acc1.deposit(amount);
      } else {
        System.out.println("Amount should be greater than zero");
      }
    } catch (InputMismatchException e) {
      System.out.println("Invalid input. Please enter a valid numeric value.");
    } catch (Exception e) {
      System.out.println("An error occurred: " + e.getMessage());
    }
  }

  private static void cashWithdrawal(Scanner sc) {
    try {
      System.out.print("Enter Account Number: ");
      String accountNumber = sc.next();
      System.out.print("Enter Password: ");
      String password = sc.next();
      Account acc1 = new Account(accountNumber, password);

      System.out.print("Enter Amount to Withdraw: ");
      double amount = sc.nextDouble();

      if (amount > 0) {
        acc1.withdraw(amount);
      } else {
        System.out.println("Amount should be greater than zero");
      }
    } catch (InputMismatchException e) {
      System.out.println("Invalid input. Please enter a valid numeric value.");
    } catch (Exception e) {
      System.out.println("An error occurred: " + e.getMessage());
    }
  }

  private static void transfer(Scanner sc) {
    try {
      System.out.println("Enter your account number: ");
      String fromAccount = sc.next();
      System.out.println("Enter your password: ");
      String password = sc.next();
      System.out.println(
        "Please input the account number of the recipient for the transfer: "
      );
      String toAccount = sc.next();

      Transaction transaction = new Transaction(
        fromAccount,
        password,
        toAccount
      );
      System.out.println("Enter amount to transfer: ");
      double amount = sc.nextDouble();
      transaction.transfer(amount);
    } catch (Exception e) {
      System.out.println("An error occured: " + e.getMessage());
    }
  }

  private static void createAccount(Scanner sc) {
    try {
      System.out.println("Enter account title: ");
      // Adding extra sc.nextLine() so it should fixed the behaviour of returing empty
      // string
      sc.nextLine();
      String title = sc.nextLine();
      if (title.length() < 3) {
        System.out.println("Account title should be of at least 3 characters");
        return;
      }
      System.out.println("Enter your age: ");
      int age = sc.nextInt();
      if (age < 18 || age > 150) {
        System.out.println("Age should be in between 18 till 150");
        return;
      }
      System.out.println("Select account type:\n1:current\n2:saving");
      int type = sc.nextInt();
      if (type != 1 && type != 2) {
        System.out.println("Select valid type");
        return;
      }

      System.out.println("Enter your email: ");
      String email = sc.next();
      if (!isValidEmail(email)) {
        System.out.println("Email is not valid");
        return;
      }

      System.out.println("Enter password");
      String password = sc.next();

      if (!isValidPassword(password)) {
        System.out.println(
          "Password length should be at least 8 including one uppercase one lowercase one digit and one special character"
        );
        return;
      }

      System.out.println("Enter amount to deposit: ");
      double amount = sc.nextDouble();

      if (amount < 100) {
        System.out.println("Deposit at least Rs 100 to create account");
        return;
      }
      if (type == 1) {
        new Account(title, amount, age, "current", email, password);
      } else {
        new Account(title, amount, age, "saving", email, password);
      }
    } catch (InputMismatchException e) {
      System.out.println("Invalid input. Please enter a valid value.");
    } catch (IllegalArgumentException | NoSuchElementException e) {
      System.out.println(e.getMessage());
    } catch (Exception e) {
      System.out.println("An error occurred: " + e.getMessage());
    }
  }

  private static void TransactionDetails(Scanner sc) {
    try {
      System.out.println("Enter your account number: ");
      String accountNumber = sc.next();
      System.out.println("Enter account password");
      String password = sc.next();

      Account acc = new Account(accountNumber, password);
      List<Document> allTransactions = acc.transactionDetails();

      // Check if there are any transactions
      if (!allTransactions.isEmpty()) {
        // Process and display transactions
        System.out.println(
          "Transaction details for account " + accountNumber + ":"
        );
        for (Document transaction : allTransactions) {
          System.out.println(transaction);
        }
      } else {
        System.out.println(
          "No transactions found for account " + accountNumber
        );
      }
    } catch (Exception e) {
      System.out.println("An error occurred in finding transaction details: ");
      e.printStackTrace();
    }
  }

  private static void displayMenu() {
    System.out.println("Welcome to PAK BANK. Select the below options:");
    System.out.println(
      "1: Deposit\n2: Cash Withdrawal\n3: Transfer\n4: Create Account\n5:Transaction Details"
    );
  }

  private static boolean isValidEmail(String email) {
    Pattern pattern = Pattern.compile(EMAIL_REGEX);
    Matcher matcher = pattern.matcher(email);
    return matcher.matches();
  }

  private static boolean isValidPassword(String password) {
    Pattern pattern = Pattern.compile(PASSWORD_REGEX);
    Matcher matcher = pattern.matcher(password);
    return matcher.matches();
  }

  private static void program(Scanner sc) {
    displayMenu();

    try {
      int opt = sc.nextInt();

      switch (opt) {
        case 1:
          deposit(sc);
          break;
        case 2:
          cashWithdrawal(sc);
          break;
        case 3:
          transfer(sc);
          break;
        case 4:
          createAccount(sc);
          break;
        case 5:
          TransactionDetails(sc);
          break;
        default:
          System.out.println("Invalid option. Please select a valid option.");
          break;
      }
    } catch (InputMismatchException e) {
      System.out.println("Invalid input. Please enter a valid numeric option.");
      // Consume the remaining invalid input
      sc.nextLine();
    } catch (Exception e) {
      System.out.println("An error occurred: " + e.getMessage());
      // Consume the remaining invalid input
      sc.nextLine();
    }
  }

  public static void main(String[] args) {
    try {
      Scanner sc = new Scanner(System.in);
      String num = "";
      do {
        program(sc);
        System.out.println("Press any key to continue and -1 to exit");
        num = sc.next();
      } while (!num.equals("-1"));

      sc.close();
    } catch (Exception e) {
      System.out.println("An error occured: " + e.getMessage());
    }
  }
}
