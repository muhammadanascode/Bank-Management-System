package package1;

import com.mongodb.client.FindIterable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import mongo.Connection;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

public class Account {

  private String title;
  private Double balance;
  private String accountNumber;
  private int age;
  private String type;
  private String email;
  private String password;

  public Account(
    String title,
    Double balance,
    int age,
    String type,
    String email,
    String password
  ) {
    this.title = title;
    this.balance = balance;
    this.accountNumber = AccountNumber();
    this.age = age;
    this.type = type;
    this.email = email;
    this.password = HashPassword(password);

    try (Connection connection = new Connection()) {
      connection.newAccount(
        this.title,
        this.accountNumber,
        this.type,
        this.age,
        this.email,
        this.balance,
        this.password
      );
      System.out.println(
        "Your Account has been created .\n Account number is: " +
        this.accountNumber
      );
    } catch (Exception e) {
      // Log or propagate the exception
      e.printStackTrace();
    }
  }

  public Account(String accountNumber) {
    try (Connection connection = new Connection()) {
      // Finding Account
      Document account = connection.findAccount(accountNumber);
      if (account != null) {
        this.title = (String) account.get("title");
        this.balance = (Double) account.get("balance");
        this.accountNumber = (String) account.get("accountNumber");
        this.email = (String) account.get("email");
        this.title = (String) account.get("title");
        this.age = (Integer) account.get("age");
        this.type = (String) account.get("type");
        this.password = (String) account.get("password");
      } else {
        throw new Exception("Account number doensot exists");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Account(String accountNumber, String password) throws Exception {
    try (Connection connection = new Connection()) {
      // Finding Account
      Document account = connection.findAccount(accountNumber);
      if (account != null) {
        this.title = (String) account.get("title");
        this.balance = (Double) account.get("balance");
        this.accountNumber = (String) account.get("accountNumber");
        this.email = (String) account.get("email");
        this.title = (String) account.get("title");
        this.age = (Integer) account.get("age");
        this.type = (String) account.get("type");
        this.password = (String) account.get("password");

        if (!verifyPassword(password, this.password)) {
          throw new Exception("Password is not correct");
        }
      } else {
        throw new Exception("Account number doesnot exists");
      }
    }
  }

  public String getTitle() {
    return title;
  }

  public Double getBalance() {
    return balance;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public int getAge() {
    return age;
  }

  public String getEmail() {
    return email;
  }

  public String getType() {
    return type;
  }

  private void setBalance(Double newBalance) {
    this.balance = newBalance;
  }

  private static String AccountNumber() {
    // Get current timestamp
    String timestamp = new SimpleDateFormat("yyyyMMddHHmmss")
      .format(new Date());
    // Generate a random 6-digit number
    int randomPart = new Random().nextInt(900000) + 100000;
    // Combine timestamp and random number
    String accountNumber = timestamp + randomPart;
    return accountNumber;
  }

  private static String HashPassword(String plainPassword) {
    // Generate a salt for better security
    String salt = BCrypt.gensalt();

    // Hash the password with the salt
    return BCrypt.hashpw(plainPassword, salt);
  }

  private static boolean verifyPassword(
    String plainPassword,
    String hashedPassword
  ) {
    // Check if the plain password matches the hashed one
    return BCrypt.checkpw(plainPassword, hashedPassword);
  }

  public void deposit(double amount) {
    try (Connection connection = new Connection()) {
      if (amount <= 0) {
        throw new IllegalArgumentException("Enter an amount greater than zero");
      } else if (amount > 5000) {
        handleLargeDeposit(amount, connection);
      } else {
        this.setBalance(this.balance + amount);
        connection.updateBalance(this.accountNumber, this.balance);

        Document transReceipt = connection.TransactionReceipt(
          "",
          this.accountNumber,
          "deposit",
          amount
        );
        connection.insertTransaction(transReceipt);
        System.out.println("Amount deposited successfully!");
      }
    } catch (Exception e) {
      // Log or propagate the exception
      e.printStackTrace();
    }
  }

  public void withdraw(double amount) {
    try (Connection connection = new Connection()) {
      if (amount <= 0) {
        throw new IllegalArgumentException("Enter an amount greater than zero");
      } else if (amount > this.balance) {
        System.out.println(
          "You do not have sufficient balance in your account"
        );
      } else {
        handleRegularWithdrawal(amount, connection);
      }
    } catch (Exception e) {
      // Log or propagate the exception
      e.printStackTrace();
    }
  }

  private void handleLargeDeposit(double amount, Connection connection) {
    double tax = (amount * 5) / 100;
    this.setBalance(this.balance + amount - tax);
    connection.updateBalance(this.accountNumber, this.balance);
    Document transReceipt = connection.TransactionReceipt(
      "",
      this.accountNumber,
      "deposit",
      amount
    );
    connection.insertTransaction(transReceipt);
    System.out.println("Amount deposited successfully after tax deduction");
  }

  private void handleRegularWithdrawal(double amount, Connection connection) {
    if (amount > 5000) {
      double tax = (amount * 5) / 100;
      if (this.balance < (amount + tax)) {
        System.out.println(
          "You do not have a sufficient balance to pay tax on this withdrawal. Try a smaller withdrawal"
        );
      } else {
        this.setBalance(this.balance - (amount + tax));
        connection.updateBalance(this.accountNumber, this.balance);

        Document transReceipt = connection.TransactionReceipt(
          this.accountNumber,
          "",
          "withdrawl",
          amount
        );
        connection.insertTransaction(transReceipt);
        System.out.println("Amount withdrawn successfully after tax deduction");
      }
    } else {
      this.setBalance(this.balance - amount);
      connection.updateBalance(this.accountNumber, this.balance);

      Document transReceipt = connection.TransactionReceipt(
        this.accountNumber,
        "",
        "withdrawl",
        amount
      );
      connection.insertTransaction(transReceipt);
      System.out.println("Amount withdrawn successfully!");
    }
  }

  public List<Document> transactionDetails() throws Exception {
    try (Connection connection = new Connection()) {
      FindIterable<Document> allTransactions = connection.transactionDetails(
        this.accountNumber
      );

      // Convert FindIterable<Document> to List<Document>
      List<Document> transactionsList = new ArrayList<>();
      allTransactions.into(transactionsList);

      return transactionsList;
    } catch (Exception e) {
      throw new Exception(
        "Failed to query trnsaction details" + e.getMessage()
      );
    }
  }

  @Override
  public String toString() {
    if (title == null) {
      return null;
    }

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Account Details:");

    appendField(stringBuilder, "Title", title);
    appendField(stringBuilder, "Balance", balance);
    appendField(stringBuilder, "Account Number", accountNumber);
    appendField(stringBuilder, "Age", age);
    appendField(stringBuilder, "Type", type);
    appendField(stringBuilder, "Email", email);

    return stringBuilder.toString();
  }

  private void appendField(
    StringBuilder stringBuilder,
    String fieldName,
    Object value
  ) {
    stringBuilder.append("\n").append(fieldName).append(": ");
    if (value != null) {
      stringBuilder.append(value);
    } else {
      stringBuilder.append("null");
    }
  }
}
