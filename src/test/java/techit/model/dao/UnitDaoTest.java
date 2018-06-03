package techit.model.dao;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.Test;
import techit.model.Ticket;
import techit.model.Unit;

import techit.model.User;
import techit.model.User.Position;

@Test(groups = "UnitDaoTest")
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class UnitDaoTest extends AbstractTransactionalTestNGSpringContextTests {

    @Autowired
    UnitDao unitDao;
    
    @Autowired
    UserDao userDao;

    @Test
    public void getUnit() {
        assert unitDao.getUnit(1L).getName() != null;
    }
    
    @Test
    public void getUnitsAUserSupervises() {
        User user = userDao.getUser(22L);
        assert unitDao.getUnits(user).size() == 1;
    }

    @Test
    public void getUnits() {
        assert unitDao.getUnits().size() > 1;
    }

    @Test
    public void saveTicket() {
        Unit unit = new Unit();
        unit.setId(2L);
        unit.setName("test");
        unit.setLocation("Test");
        unit.setEmail("Email@email.com");
        unit = unitDao.saveUnit(unit);
        assert unit.getId() != null;
    }
}
