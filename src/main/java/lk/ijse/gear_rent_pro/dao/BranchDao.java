package lk.ijse.gear_rent_pro.dao;

import java.sql.SQLException;
import java.util.List;
import lk.ijse.gear_rent_pro.model.Branch;

public interface BranchDao {
    Branch save(Branch branch) throws SQLException;
    boolean update(Branch branch) throws SQLException;
    boolean deleteById(int id) throws SQLException;
    Branch findById(int id) throws SQLException;
    List<Branch> findAll() throws SQLException;
}
