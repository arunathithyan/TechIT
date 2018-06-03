package techit.model.dao.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import techit.model.Ticket;
import techit.model.Unit;

import techit.model.User;
import techit.model.dao.TicketDao;
import techit.model.dao.UserDao;

@Repository
public class TicketDaoImpl implements TicketDao {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    UserDao userDao;

    @Override
    public Ticket getTicket(Long id) {
        return entityManager.find(Ticket.class, id);
    }

    @Override
    public List<Ticket> getTickets() {
        return entityManager.createQuery("from Ticket order by id", Ticket.class)
                .getResultList();
    }

    @Override
    public List<Ticket> getTickets(Unit unit) {
        return entityManager.createQuery("from Ticket where unitid = :unitid", Ticket.class)
                .setParameter("unitid", unit.getId())
                .getResultList();
    }

    @Override
    public List<Ticket> getTickets(User requestor) {
        return entityManager.createQuery("from Ticket where userid = :userid", Ticket.class)
                .setParameter("userid", requestor.getId())
                .getResultList();
    }
    
    @Override
    public List<Ticket> getTicketsAssignedToTechnician(User technician) {
        return entityManager.createQuery("select t from Ticket t join t.technicians tt where tt = :user", Ticket.class)
                .setParameter("user", technician)
                .getResultList();
    }
    
    @Override
    @Transactional
    public List<User> assignTechnicianToTicket(Ticket ticket, User technician) {
        List<User> technicians = userDao.getTechnicians(ticket);
        technicians.add(technician);
        ticket.setTechnicians(technicians);
        entityManager.merge(ticket);
        //return entityManager.merge(ticket);
        return technicians;
    }

    @Override
    @Transactional
    public Ticket saveTicket(Ticket ticket) {
        ticket.setStartDate(new Date());
        return entityManager.merge(ticket);
    }

}
