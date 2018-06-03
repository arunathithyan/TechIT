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

@Test(groups = "UserDaoTest")
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class UserDaoTest extends AbstractTransactionalTestNGSpringContextTests {

    @Autowired
    UserDao userDao;
    
    @Autowired
    UnitDao unitDao;

    @Test
    public void getUserByUsername() {
        assert userDao.getUser("ADMIN").getUsername() != null;
    }

    @Test
    public void getUserByID() {
        assert userDao.getUser(2L).getUsername() != null;
    }

    @Test
    public void getUsers() {
        assert userDao.getUsers().size() > 0;
    }

    @Test
    public void getTechniciansByUnit() {
        Unit unit = unitDao.getUnit(1L);
        assert userDao.getTechnicians(unit).size() > 0;
    }

    @Test
    public void getSupervisorsByUnit() {
        Unit unit = new Unit();
        unit.setId(1L);
        unit.setName("test");
        unit.setLocation("Test");
        unit.setEmail("Email@email.com");
        assert userDao.getSupervisors(unit).size() >0;
    }

    @Test
    public void saveUser() {
        User user = new User();        
        user.setEmail("email2@email.com");
        user.setFirstName("firstName");
        user.setLastName("LastName");
        user.setPassword("password");
        user.setPhoneNumber("1123456789");
        user.setPosition(Position.USER);
        user.setUsername("UserNameTest");
        user = userDao.saveUser(user);
        assert user.getId() != null;
    }

    @Test
    public void getTechniciansByTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setCurrentPriority(Ticket.Priority.MEDIUM);
        ticket.setCurrentProgress(Ticket.Progress.INPROGRESS);
        ticket.setDetails("test");
        ticket.setEndDate(new Date());
        ticket.setReqUser(userDao.getUser(1L));
        ticket.setStartDate(new Date());
        ticket.setSubject("subject");
        ticket.setUnit(null);
        assert userDao.getTechnicians(ticket).size() >0;
    }
}
