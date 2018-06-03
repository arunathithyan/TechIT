package techit.model.dao.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import techit.model.Ticket;
import techit.model.Update;

import techit.model.User;
import techit.model.dao.UpdateDao;

@Repository
public class UpdateDaoImpl implements UpdateDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Update getUpdate(Long id) {
        return entityManager.find(Update.class, id);
    }
    
    @Override
    public List<Update> getUpdates() {
        return entityManager.createQuery("from Update order by id", Update.class)
                .getResultList();
    }

    @Override
    public List<Update> getUpdates(Ticket ticket) {
        return entityManager.createQuery("from Update where ticketid= :ticketid", Update.class)
                .setParameter("ticketid", ticket.getId())
                .getResultList();
    }

    @Override
    public List<Update> getUpdates(User user) {
        return entityManager.createQuery("from Update where modifier= :modifier", Update.class)
                .setParameter("modifier", user)
                .getResultList();
    }

    @Override
    @Transactional  //Use this for all save or update methods
    public Update saveUpdate(Update update) {
        update.setModifiedDate(new Date());
        return entityManager.merge(update);
    }

}
