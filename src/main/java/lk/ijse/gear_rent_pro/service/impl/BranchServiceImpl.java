package lk.ijse.gear_rent_pro.service.impl;

import java.sql.SQLException;
import java.util.List;
import lk.ijse.gear_rent_pro.dao.BranchDao;
import lk.ijse.gear_rent_pro.dao.impl.BranchDaoImpl;
import lk.ijse.gear_rent_pro.model.Branch;
import lk.ijse.gear_rent_pro.service.BranchService;

public class BranchServiceImpl implements BranchService {

    private final BranchDao dao = new BranchDaoImpl();

    @Override
    public Branch create(Branch branch) {
        try { return dao.save(branch); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean update(Branch branch) {
        try { return dao.update(branch); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean delete(int id) {
        try { return dao.deleteById(id); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Branch findById(int id) {
        try { return dao.findById(id); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Branch> findAll() {
        try { return dao.findAll(); } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
