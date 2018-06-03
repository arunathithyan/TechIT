package techit.model.dao;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.Test;
import techit.model.Ticket;
import techit.model.Update;

import techit.model.User;

@Test(groups = "UpdateDaoTest")
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class UpdateDaoTest extends AbstractTransactionalTestNGSpringContextTests {

    @Autowired
    UpdateDao updateDao;
    
    @Autowired
    TicketDao ticketDao;
    
    @Autowired
    UserDao userDao;

    @Test
    public void getUpdate() {
        assert updateDao.getUpdate(1L).getId() != null;
    }
    
    @Test
    public void getUpdates() {
        assert updateDao.getUpdates().size() > 1;
    }
    
    @Test
    public void getUpdatesForTicket() {
        Ticket ticket = ticketDao.getTicket(1L);
        assert updateDao.getUpdates(ticket).size() == 2;
    }
    
    @Test
    public void getUpdatesMadeByUser() {
        User user = userDao.getUser(4L);
        assert updateDao.getUpdates(user).size() == 1;
    }
    
    @Test
    public void saveUpdates() {
        Update update = new Update();
        Ticket ticket = ticketDao.getTicket(1L);
        User user = userDao.getUser(1L);
        update.setModifiedDate(new Date());
        update.setUpdateDetails("New Test");
        update.setTicket(ticket);
        update.setModifier(user);
        update = updateDao.saveUpdate(update);
        assert update.getId() != null;
    }
}
