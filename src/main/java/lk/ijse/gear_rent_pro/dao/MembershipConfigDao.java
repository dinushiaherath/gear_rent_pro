package lk.ijse.gear_rent_pro.dao;

import java.util.List;

import lk.ijse.gear_rent_pro.model.MembershipConfig;
import lk.ijse.gear_rent_pro.model.MembershipLevel;

public interface MembershipConfigDao {
    List<MembershipConfig> findAll() throws Exception;
    MembershipConfig findByLevel(MembershipLevel level) throws Exception;
    void saveOrUpdate(MembershipConfig config) throws Exception;
}
