import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankSimulation {
    private static final String URL = "jdbc:mysql://localhost:3306/swift_bank";
    private static final String USER = "root";
    private static final String PASS = "123456"; // 

    public static void transferMoney(int fromAcc, int toAcc, double amount) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            // CRITICAL STEP FOR BANKS: Disable Auto-Commit to handle multi-step updates safely
            conn.setAutoCommit(false);

            // 1. Lock rows in a deterministic order to prevent Deadlocks
            int firstLock = Math.min(fromAcc, toAcc);
            int secondLock = Math.max(fromAcc, toAcc);

            String lockSql = "SELECT balance FROM accounts WHERE account_id = ? FOR UPDATE";
            
            // Lock first account
            try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                ps.setInt(1, firstLock);
                ps.executeQuery();
            }
            // Lock second account
            try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                ps.setInt(1, secondLock);
                ps.executeQuery();
            }

            // 2. Verify Sender Balance
            double senderBalance = 0;
            String checkBalanceSql = "SELECT balance FROM accounts WHERE account_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkBalanceSql)) {
                ps.setInt(1, fromAcc);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) senderBalance = rs.getDouble("balance");
                }
            }

            if (senderBalance < amount) {
                System.out.println("Transfer Failed: Insufficient funds for Account " + fromAcc);
                conn.rollback();
                return;
            }

            // 3. Update Balances
            String deductSql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deductSql)) {
                ps.setDouble(1, amount);
                ps.setInt(2, fromAcc);
                ps.executeUpdate();
            }

            String creditSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(creditSql)) {
                ps.setDouble(1, amount);
                ps.setInt(2, toAcc);
                ps.executeUpdate();
            }

            // 4. Log the transaction
            String logSql = "INSERT INTO transfer_log (from_account, to_account, amount) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                ps.setInt(1, fromAcc);
                ps.setInt(2, toAcc);
                ps.setDouble(3, amount);
                ps.executeUpdate();
            }

            // Commit the entire transaction atomically
            conn.commit();
            System.out.println("Successfully transferred $" + amount + " from " + fromAcc + " to " + toAcc);

        } catch (SQLException e) {
            System.out.println("Transaction Error. Rolling back changes.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Simulate high-concurrency: 5 threads trying to send $300 from Alice (101) to Bob (102) at the exact same time
        // Alice only has $1000, so only 3 transactions should succeed!
        ExecutorService executor = Executors.newFixedThreadPool(5);

        System.out.println("Starting Concurrent Bank Transfer Simulation...");
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> transferMoney(101, 102, 300.00));
        }

        executor.shutdown();
    }
}