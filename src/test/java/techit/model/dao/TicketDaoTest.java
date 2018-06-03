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

@Test(groups = "TicketDaoTest")
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class TicketDaoTest extends AbstractTransactionalTestNGSpringContextTests {

    @Autowired
    TicketDao ticketDao;

    @Autowired
    UserDao userDao;

    @Autowired
    UnitDao unitDao;

    @Test
    public void getTicket() {
        assert ticketDao.getTicket(1L).getReqUser() != null;
    }

    @Test
    public void getTickets() {
        assert ticketDao.getTickets().size() > 1;
    }

    @Test
    public void getTicketsByUnit() {
        Unit unit = unitDao.getUnit(1L);
        assert ticketDao.getTickets(unit).size() > 1;
    }

    @Test
    public void getTicketsByRequestor() {
        User user = userDao.getUser(5L);
        assert ticketDao.getTickets(user).size() > 1;
    }

    @Test
    public void saveTicket() {

        User user = userDao.getUser(5L);

        Ticket ticket = new Ticket();
        ticket.setId(2L);
        ticket.setCurrentPriority(Ticket.Priority.MEDIUM);
        ticket.setCurrentProgress(Ticket.Progress.INPROGRESS);
        ticket.setDetails("test");
        ticket.setEndDate(new Date());
        ticket.setReqUser(user);
        ticket.setStartDate(new Date());
        ticket.setSubject("subject");

        Unit unit = new Unit();
        unit.setId(1L);
        unit.setName("test");
        unit.setLocation("Test");
        unit.setEmail("Email@email.com");

        ticket.setUnit(unit);
        ticket = ticketDao.saveTicket(ticket);
        assert ticket.getId() != null;
    }
}
