package techit.rest.controller;

//import com.sun.xml.internal.ws.dump.LoggingDumpTube;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import techit.model.Ticket;

import techit.model.User;
import techit.model.Unit;
import techit.model.User.Position;
import techit.model.UserDetails;
import techit.model.dao.TicketDao;
import techit.model.dao.UserDao;
import techit.model.dao.UnitDao;
import techit.rest.error.RestException;

@RestController
public class UnitController {

    @Autowired
    private UnitDao unitDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TicketDao ticketDao;

    public enum Action {
        GET_USERS, SAVE_USER, GET_UNITS, GET_TECHNICIANS, GET_TICKETS_IN_UNIT, SAVE_UNIT, GET_USER, GET_TICKETS_MADE_BY_USER, GET_TICKETS_ASSIGNED_TO_TECHNICIAN;
    };

    /**
     * This method checks the JWT to determine the user making the request. It
     * returns a class which holds the user's id and position if the JWT is
     * valid. If the JWT is invalid, null is returned.
     *
     * @param request - The HttpServletRequest for this request
     * @return UserDetails - A class which holds the id and position of the user
     */
    public UserDetails checkRequestor(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        String secret = "SECRET";
        Jws<Claims> claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret.getBytes())
                    .parseClaimsJws(token);
        } catch (Exception e) {
            return null;
        }
        String userType = (String) claims.getBody().get("type");
        long userId = (int) claims.getBody().get("id");
        return new UserDetails(userId, Position.valueOf(userType));
    }

    /*public List<User> performActionIfAdmin(HttpServletRequest request, Action action, String error_message) {
        long userId = checkRequestor(request);
        if (userId != 0 || userId == -1) {
            throw new RestException(400, error_message);
        }
        if (action == Action.GET_USERS) {
            return userDao.getUsers();
        } else {
            return null;
        }
    }

    public List<User> performActionIfAdmin(long unitId, HttpServletRequest request, Action action, String error_message) {
        long userId = checkRequestor(request);
        if (userId != 0 || userId == -1) {
            throw new RestException(400, error_message);
        }
        if (action == Action.GET_TECHNICIANS) {
            Unit unit = unitDao.getUnit(unitId);
            return userDao.getTechnicians(unit);
        } else {
            return null;
        }
    }*/
    public List<Ticket> performActionIfAdminForUnit(long unitId, HttpServletRequest request, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);
        if (userDetails == null) {
            throw new RestException(403, error_message);
        } else if (userDetails.getPosition() == Position.ADMIN) {
            if (action == Action.GET_TICKETS_IN_UNIT) {
                Unit unit = unitDao.getUnit(unitId);
                if(unit==null)
                    throw new RestException(404, "Unit not found in database");
                List<Ticket> t= ticketDao.getTickets(unit); 
                if(t==null || t.size()==0)
                	throw new RestException(404, "No Tickets found for the given Unit");
                return t;
                
            } else {
                throw new RestException(403, error_message);
            }
        } else if (userDetails.getPosition() == Position.SUPERVISOR) {
            if (action == Action.GET_TICKETS_IN_UNIT) {
                Unit unit = unitDao.getUnit(unitId);
                User supervisor = userDao.getUser(userDetails.getId());
                if (supervisor.getSupervised_unit() == null) {
                    throw new RestException(403, error_message);
                }
                if (supervisor.getSupervised_unit().getId() == unitId) {
                	if(unit==null)
                        throw new RestException(404, "Unit not found in database");
                	 List<Ticket> t= ticketDao.getTickets(unit); 
                     if(t==null || t.size()==0)
                     	throw new RestException(404, "No Tickets found for the given Unit");
                     return t;
                     
                }
            }
        }
        throw new RestException(403, error_message);
    }

    public List<Unit> performActionIfAdminForUnits(HttpServletRequest request, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);
        if (userDetails == null) {
            throw new RestException(403, error_message);
        }
        if (action == Action.valueOf("GET_UNITS")) {
            return unitDao.getUnits();
        } else {
            return null;
        }
    }/**/

    public Unit performActionIfAdmin(Unit unit, HttpServletRequest request, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);
        if (userDetails == null || userDetails.getPosition() != Position.ADMIN) {
            throw new RestException(403, error_message);
        }
        if (action == Action.valueOf("SAVE_UNIT")) {
            return unitDao.saveUnit(unit);
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/units", method = RequestMethod.GET)
    public List<Unit> getUnits(HttpServletRequest request) {
        return performActionIfAdminForUnits(request, Action.GET_UNITS, "You do not have authorization to view these units");
    }

    /*
    @RequestMapping(value = "/units/id/{id}", method = RequestMethod.GET)
    public Unit getUnitWithId(@PathVariable Long id) {
        return unitDao.getUnit(id);
    }

    @RequestMapping(value = "/units/user/{id}", method = RequestMethod.GET)
    public List<Unit> getUnitsThisUserSupervises(@PathVariable Long id) {
        User user = userDao.getUser(1L);
        return unitDao.getUnits(user);
    }*/
    @RequestMapping(value = "/units", method = RequestMethod.POST)
    public Unit addUnit(@RequestBody Unit unit, HttpServletRequest request) {
        return performActionIfAdmin(unit, request, Action.SAVE_UNIT, "You do not have authorization to create this unit");
    }

    @RequestMapping(value = "/units/{id}/technicians", method = RequestMethod.GET)
    public List<User> getTechniciansInAUnit(@PathVariable long id, HttpServletRequest request) {
        UserDetails userDetails = checkRequestor(request);
        if (userDetails == null) {
            throw new RestException(403, "You do not have authorization to view these technicians");
        } else if (userDetails.getPosition() == Position.ADMIN) {
            Unit unit = unitDao.getUnit(id);
            if(unit==null)
                throw new RestException(404, "Unit not found in database");
            
            return userDao.getTechnicians(unit);
        } else if (userDetails.getPosition() == Position.SUPERVISOR) {
            User supervisor = userDao.getUser(userDetails.getId());
            if (supervisor.getSupervised_unit().getId() == id) {
                	Unit unit = unitDao.getUnit(id);
                
                	if(unit==null)
                		throw new RestException(404, "Unit not found in database");
                
                	List<User> t= userDao.getTechnicians(unit); 
                	if(t==null || t.size()==0)
                		throw new RestException(404, "No Technicians found for the given Unit");
            
                	return t;
                } 
            else {
                throw new RestException(403, "You do not have authorization to view these technicians");
            }
        } else {
            throw new RestException(403, "You do not have authorization to view these technicians");
        }
    }

    @RequestMapping(value = "/units/{id}/tickets", method = RequestMethod.GET)
    public List<Ticket> getTicketsSubmittedToAUnit(@PathVariable long id, HttpServletRequest request) {
        return performActionIfAdminForUnit(id, request, Action.GET_TICKETS_IN_UNIT, "You do not have the authorization to view these tickets");
    }

}
