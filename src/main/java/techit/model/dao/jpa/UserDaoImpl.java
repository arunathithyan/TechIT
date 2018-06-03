package techit.model.dao.jpa;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import techit.model.Ticket;
import techit.model.Unit;

import techit.model.User;
import techit.model.User.Position;
import techit.model.dao.UserDao;

@Repository
public class UserDaoImpl implements UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User getUser(String username) {
        return entityManager.createQuery("from User where username= :username", User.class)
                .setParameter("username", username)
                .getSingleResult();
    }

    @Override
    public User getUser(Long id) {
        return entityManager.find(User.class, id);
    }

    @Override
    public String login(String username, String password) {
        User user = getUser(username);
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days
        //If username doesn't point to any user, return null
        if (user == null) {
            return null;
        } //Else
        else {
            //Check if the password is valid,
            boolean matched = BCrypt.checkpw(password, user.getHash());
            //If so, return user
            if (matched) {
                String token = Jwts.builder()
                        .setSubject("user")
                        .claim("id", user.getId())
                        .claim("type", user.getPosition())
                        .claim("email", user.getEmail())
                        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                        .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                        .compact(); // Sign and build the JWT
                return token;
            } //If not so, return null
            else {
                return null;
            }
        }
    }

    @Override
    public List<User> getUsers() {
        return entityManager.createQuery("from User order by id", User.class)
                .getResultList();
    }

    @Override
    public List<User> getUsers(int position) {
        return entityManager.createQuery("from User where position = :position", User.class)
                .setParameter("position", User.Position.values()[position])
                .getResultList();
    }

    @Override
    public List<User> getTechnicians(Ticket ticket) {
        //Write the class u want to get its return value from. 'User.class' in this case since we r looking for technicians
        //The t.'...' is the property value of the joined table. 'tickets_technician_is_working_on' in this case

        //If we were trying to get tickets, we would write 'Ticket' and 't.technicians'
        return entityManager.createQuery("select t from User t join t.tickets_technician_is_working_on tt where tt =:ticket", User.class)
                .setParameter("ticket", ticket)
                .getResultList();
    }

    @Override
    public List<User> getTechnicians(Unit unit) {
        return entityManager.createQuery("from User where unitid= :unitid and position =:position", User.class)
                .setParameter("unitid", unit.getId())
                .setParameter("position", Position.TECHNICIAN)
                .getResultList();
    }

    @Override
    public List<User> getSupervisors(Unit unit) {
        return entityManager.createQuery("select t from User t join t.supervised_unit tt where tt =:unit", User.class)
                .setParameter("unit", unit)
                .getResultList();
    }

    @Override
    @Transactional  //Use this for all save or update methods
    public User saveUser(User user) {
        //If this is a new user, hash the password
        if (user.getHash() == null) {
            String hash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10));
            user.setHash(hash);
        }
        return entityManager.merge(user);
    }

}
