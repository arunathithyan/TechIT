package techit.rest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import techit.model.Ticket;

import techit.model.User;
import techit.model.Update;
import techit.model.dao.TicketDao;
import techit.model.dao.UserDao;
import techit.model.dao.UpdateDao;

@RestController
public class UpdateController {

    @Autowired
    private UpdateDao updateDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TicketDao ticketDao;

    @RequestMapping(value = "/updates", method = RequestMethod.GET)
    public List<Update> getUpdates() {
        return updateDao.getUpdates();
    }

    @RequestMapping(value = "/updates/id/{id}", method = RequestMethod.GET)
    public Update getUpdateWithId(@PathVariable Long id) {
        return updateDao.getUpdate(id);
    }

    @RequestMapping(value = "/updates/ticket/{id}", method = RequestMethod.GET)
    public List<Update> getUpdatesToATicket(@PathVariable long id) {
        Ticket ticket = ticketDao.getTicket(id);
        return updateDao.getUpdates(ticket);
    }

    @RequestMapping(value = "/updates/user/{id}", method = RequestMethod.GET)
    public List<Update> getUpdatesMadeByUser(@PathVariable long id) {
        User user = userDao.getUser(id);
        return updateDao.getUpdates(user);
    }

    @RequestMapping(value = "/updates/{id}", method = RequestMethod.POST)
    public Update updateTicket(@PathVariable long id, @RequestBody Update update) {
        update.setId(id);
        return updateDao.saveUpdate(update);
    }

    @RequestMapping(value = "/updates", method = RequestMethod.POST)
    public Update addUpdate(@RequestBody Update update) {
        return updateDao.saveUpdate(update);
    }
}
