package lk.ijse.gear_rent_pro.service.impl;

import java.sql.SQLException;
import java.util.List;
import lk.ijse.gear_rent_pro.dao.UserDao;
import lk.ijse.gear_rent_pro.dao.impl.UserDaoImpl;
import lk.ijse.gear_rent_pro.model.User;
import lk.ijse.gear_rent_pro.service.UserService;
import lk.ijse.gear_rent_pro.util.PasswordUtil;

public class UserServiceImpl implements UserService {

    private final UserDao dao = new UserDaoImpl();

    @Override
    public User create(User user) {
        try {
            if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
                user.setPasswordHash(PasswordUtil.hashPassword(user.getPasswordHash()));
            }
            return dao.save(user);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean update(User user) {
        try {
            if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
                User existing = dao.findById(user.getId());
                if (existing != null) {
                    user.setPasswordHash(existing.getPasswordHash());
                }
            } else {
                user.setPasswordHash(PasswordUtil.hashPassword(user.getPasswordHash()));
            }
            return dao.update(user);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean delete(int id) {
        try { return dao.deleteById(id); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public User findById(int id) {
        try { return dao.findById(id); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public User findByUsername(String username) {
        try { return dao.findByUsername(username); } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<User> findAll() {
        try { return dao.findAll(); } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
