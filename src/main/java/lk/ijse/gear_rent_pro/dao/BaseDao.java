package lk.ijse.gear_rent_pro.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Base DAO interface providing transaction-aware CRUD operations.
 * Implementations should support both standard and transaction-scoped database operations.
 * 
 * @param <T> The entity type
 * @param <ID> The ID type
 */
public interface BaseDao<T, ID> {
    
    /**
     * Saves an entity and returns it with the generated ID set.
     * If in a transaction, uses the transaction connection; otherwise uses a new connection.
     * 
     * @param entity The entity to save
     * @return The saved entity with ID set
     * @throws SQLException if database operation fails
     */
    T save(T entity) throws SQLException;
    
    /**
     * Updates an existing entity.
     * If in a transaction, uses the transaction connection; otherwise uses a new connection.
     * 
     * @param entity The entity to update
     * @return true if update was successful, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean update(T entity) throws SQLException;
    
    /**
     * Deletes an entity by ID.
     * If in a transaction, uses the transaction connection; otherwise uses a new connection.
     * 
     * @param id The ID of the entity to delete
     * @return true if deletion was successful, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean deleteById(ID id) throws SQLException;
    
    /**
     * Finds an entity by ID.
     * If in a transaction, uses the transaction connection; otherwise uses a new connection.
     * 
     * @param id The ID to find
     * @return The entity if found, null otherwise
     * @throws SQLException if database operation fails
     */
    T findById(ID id) throws SQLException;
    
    /**
     * Finds all entities.
     * If in a transaction, uses the transaction connection; otherwise uses a new connection.
     * 
     * @return A list of all entities
     * @throws SQLException if database operation fails
     */
    List<T> findAll() throws SQLException;
}
