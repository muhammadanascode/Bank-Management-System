package Transaction;

import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import mongo.Connection;

public class Transaction {

    private String fromAccount;
    private String toAccount;
    private String password;

    public Transaction(String fromAccount, String password, String toAccount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.password = password;
    }

    public void transfer(double amount) {
        try (Connection connection = new Connection()) {
            Document sender = connection.findAccount(this.fromAccount);
            Document receiver = connection.findAccount(this.toAccount);

            if (sender == null) {
                throw new Exception("The sender account number provided does not exist");
                
            } else if (receiver == null) {
                throw new Exception("Receiver account number does not exist");
            }

            String hashPassword = (String) sender.get("password");

            if (!verifyPassword(this.password, hashPassword)) {
                throw new Exception("Provided Password is wrong. Please try again.");
            }

            // Assuming balance is of type Double
            Double senderBalance = (Double) sender.get("balance");

            if (senderBalance < amount) {
                throw new Exception("Insufficeint balance ");
            } else {
                Double amount1 = senderBalance - amount;
                Double receiverBalance = (Double) receiver.get("balance");
                Double amount2 = receiverBalance + amount;

                // Updating sender's account
                connection.updateBalance(fromAccount, amount1);

                // Updating receiver's account
                connection.updateBalance(toAccount, amount2);

                Document transReceipt = connection.TransactionReceipt(fromAccount, toAccount, "transfer", amount);
                connection.insertTransaction(transReceipt);

                System.out.println("Transaction successful!");
            }
        } catch (Exception e) {
            // Log or propagate the exception
           System.out.println("An error occured in transfer: " + e.getMessage());
        }
    }

    private static boolean verifyPassword(String plainPassword, String hashedPassword) {
        // Check if the plain password matches the hashed one
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
