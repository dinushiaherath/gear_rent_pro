package lk.ijse.gear_rent_pro.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class for managing database transactions.
 * Handles multi-step operations that require atomic execution (all succeed or all fail).
 */
public class TransactionManager {
    
    private static final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();
    
    /**
     * Begins a new transaction by setting a connection in thread-local storage.
     * Auto-commit is disabled on the connection.
     * 
     * @return The connection for the transaction
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection beginTransaction() throws SQLException {
        Connection connection = DBConnection.getConnection();
        connection.setAutoCommit(false);
        transactionConnection.set(connection);
        return connection;
    }
    
    /**
     * Commits the current transaction and closes the connection.
     * 
     * @throws SQLException if commit fails
     */
    public static void commit() throws SQLException {
        Connection connection = transactionConnection.get();
        if (connection != null) {
            try {
                connection.commit();
            } finally {
                connection.close();
                transactionConnection.remove();
            }
        }
    }
    
    /**
     * Rolls back the current transaction and closes the connection.
     * 
     * @throws SQLException if rollback fails
     */
    public static void rollback() throws SQLException {
        Connection connection = transactionConnection.get();
        if (connection != null) {
            try {
                connection.rollback();
            } finally {
                connection.close();
                transactionConnection.remove();
            }
        }
    }
    
    /**
     * Gets the current transaction connection.
     * Returns the thread-local connection if in a transaction, otherwise gets a new one.
     * 
     * @return The connection for database operations
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = transactionConnection.get();
        if (connection != null) {
            return connection;
        }
        return DBConnection.getConnection();
    }
    
    /**
     * Checks if a transaction is currently active.
     * 
     * @return true if in a transaction, false otherwise
     */
    public static boolean isInTransaction() {
        return transactionConnection.get() != null;
    }
    
    /**
     * Executes a transactional operation.
     * Automatically handles commit/rollback based on operation success.
     * 
     * @param operation The operation to execute
     * @return The result of the operation
     * @throws Exception if the operation fails
     */
    public static <T> T executeTransaction(TransactionalOperation<T> operation) throws Exception {
        Connection connection = beginTransaction();
        try {
            T result = operation.execute(connection);
            commit();
            return result;
        } catch (Exception e) {
            rollback();
            throw e;
        }
    }
    
    /**
     * Functional interface for transactional operations.
     * 
     * @param <T> The return type of the operation
     */
    @FunctionalInterface
    public interface TransactionalOperation<T> {
        /**
         * Executes the transactional operation.
         * 
         * @param connection The transaction connection
         * @return The result
         * @throws Exception if the operation fails
         */
        T execute(Connection connection) throws Exception;
    }
    
    /**
     * Functional interface for void transactional operations.
     */
    @FunctionalInterface
    public interface VoidTransactionalOperation {
        /**
         * Executes the transactional operation.
         * 
         * @param connection The transaction connection
         * @throws Exception if the operation fails
         */
        void execute(Connection connection) throws Exception;
    }
}
