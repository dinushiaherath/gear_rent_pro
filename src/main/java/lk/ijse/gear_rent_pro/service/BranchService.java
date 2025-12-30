package lk.ijse.gear_rent_pro.service;

import java.util.List;
import lk.ijse.gear_rent_pro.model.Branch;

public interface BranchService {
    Branch create(Branch branch);
    boolean update(Branch branch);
    boolean delete(int id);
    Branch findById(int id);
    List<Branch> findAll();
}
