package lk.ijse.gear_rent_pro.service.impl;

import java.util.ArrayList;
import java.util.List;

import lk.ijse.gear_rent_pro.dao.MembershipConfigDao;
import lk.ijse.gear_rent_pro.dao.impl.MembershipConfigDaoImpl;
import lk.ijse.gear_rent_pro.model.MembershipConfig;
import lk.ijse.gear_rent_pro.model.MembershipLevel;
import lk.ijse.gear_rent_pro.service.MembershipConfigService;

public class MembershipConfigServiceImpl implements MembershipConfigService {

    private final MembershipConfigDao dao = new MembershipConfigDaoImpl();

    @Override
    public List<MembershipConfig> findAll() throws Exception {
        List<MembershipConfig> stored = dao.findAll();
        // ensure we always return entries for all enum values
        List<MembershipConfig> result = new ArrayList<>();
        for (MembershipLevel level : MembershipLevel.values()) {
            MembershipConfig cfg = stored.stream()
                    .filter(s -> s.getLevel() == level)
                    .findFirst()
                    .orElse(new MembershipConfig(level, 0));
            result.add(cfg);
        }
        return result;
    }

    @Override
    public MembershipConfig findByLevel(MembershipLevel level) throws Exception {
        MembershipConfig cfg = dao.findByLevel(level);
        if (cfg == null) return new MembershipConfig(level, 0);
        return cfg;
    }

    @Override
    public void saveOrUpdate(MembershipConfig config) throws Exception {
        if (config.getDiscountPercent() < 0) throw new IllegalArgumentException("Discount cannot be negative");
        dao.saveOrUpdate(config);
    }
}
