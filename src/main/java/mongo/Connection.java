package mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

public class Connection implements AutoCloseable {

    private static final String DATABASE_NAME = "ExampleDB";
    private static final String COLLECTION_NAME_ACCOUNT = "Account";
    private static final String COLLECTION_NAME_TRANSACTION = "Transaction";

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> accountCollection;
    private MongoCollection<Document> transactionCollection;

    public Connection() {
        try {
            mongoClient = new MongoClient("localhost", 27017);
            database = mongoClient.getDatabase(DATABASE_NAME);
            accountCollection = database.getCollection(COLLECTION_NAME_ACCOUNT);
            transactionCollection = database.getCollection(COLLECTION_NAME_TRANSACTION);
            disableMongoDBLogs();
        } catch (Exception e) {
           System.out.println("Error connecting mongodb" + e.getMessage());
        }
    }

    private void disableMongoDBLogs() {
        // Get the root logger
        Logger rootLogger = Logger.getLogger("");

        // Set the logging level to WARNING or higher to suppress INFO messages
        rootLogger.setLevel(Level.WARNING);

        // Remove MongoDB-specific handlers
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            if (handler.getClass().getName().startsWith("org.mongodb")) {
                rootLogger.removeHandler(handler);

            }
        }
    }

    public void newAccount(String name, String accountNumber, String accountType, int age, String email,
            double balance, String password) {
        try {
            // Checking if account already exists
            Document account = findAccountByAccountNumber(email);
            if (account != null) {
                System.out.println("Account already exists with this account number");
            } else {
                Document newAccountDocument = new Document("title", name)
                        .append("accountNumber", accountNumber)
                        .append("type", accountType)
                        .append("age", age)
                        .append("email", email)
                        .append("balance", balance)
                        .append("password", password);
                insertAccount(newAccountDocument);
            }
        } catch (Exception e) {
            // Log or propagate the exception
            e.printStackTrace();
        }
    }

    public void updateBalance(String accountNumber, double newBalance) {
        try {
            // Find the account based on the account number
            Document accountToUpdate = findAccountByAccountNumber(accountNumber);

            // Check if the account exists
            if (accountToUpdate != null) {
                accountToUpdate.put("balance", newBalance);
                accountCollection.replaceOne(Filters.eq("accountNumber", accountNumber),
                        accountToUpdate);

            } else {
                System.out.println("Account not found with account number: " + accountNumber);
            }
        } catch (Exception e) {
            // Log or propagate the exception
            e.printStackTrace();
        }
    }

    public Document findAccount(String accountNumber) {
        // Find the account based on the account number
        return findAccountByAccountNumber(accountNumber);
    }

    private Document findAccountByAccountNumber(String accountNumber) {
        return accountCollection.find(Filters.eq("accountNumber", accountNumber)).first();
    }

    public Document TransactionReceipt(String fromAccount, String toAccount, String type, double amount) {
        Document newDoc = new Document("from", fromAccount)
                .append("to", toAccount)
                .append("type", type)
                .append("amount", amount);

        return newDoc;
    }

    public FindIterable<Document> transactionDetails(String accountNumber) {
        return transactionCollection
                .find(Filters.or(Filters.eq("from", accountNumber), Filters.eq("to", accountNumber)));

    }

    public void insertAccount(Document accountDocument) {
        accountCollection.insertOne(accountDocument);
    }

    public void insertTransaction(Document transactionReceipt) {
        transactionCollection.insertOne(transactionReceipt);
    }

    @Override
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
