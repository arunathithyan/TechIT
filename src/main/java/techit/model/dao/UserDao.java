package techit.model.dao;

import java.util.List;
import techit.model.Ticket;
import techit.model.Unit;

import techit.model.User;

public interface UserDao {

    User getUser(String username);

    User getUser(Long id);

    List<User> getUsers();

    List<User> getUsers(int position);

    List<User> getTechnicians(Ticket ticket);

    List<User> getTechnicians(Unit unit);

    List<User> getSupervisors(Unit unit);

    User saveUser(User user);

    String login(String username, String password);

}
