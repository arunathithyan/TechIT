package techit.rest.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
public class UserController {

    public enum Action {
        GET_USERS, SAVE_USER, GET_UNITS, GET_TECHNICIANS, SAVE_UNIT, GET_USER, GET_TICKETS_MADE_BY_USER, GET_TICKETS_ASSIGNED_TO_TECHNICIAN;
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

    public List<User> performActionIfAdmin(HttpServletRequest request, Action action, String error_message)
    {
        UserDetails userDetails = checkRequestor(request);
        if (userDetails == null || userDetails.getPosition() != Position.ADMIN) {
            throw new RestException(403, error_message);
        }
        if (action == Action.GET_USERS) {
            return userDao.getUsers();
        } else {
            return null;
        }
    }

    public User performActionIfAuthorizedToCreate(User user, HttpServletRequest request, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);
        if (userDetails == null) {
            throw new RestException(403, error_message);
        } else if (userDetails.getPosition() == Position.ADMIN) {
            //Admin can create any type of person
            //If the created user is a supervisor and the unitId is giving,
            if (user.getPosition() == Position.SUPERVISOR && user.getUnitId() != null) {
                //make the user the supervisor for that unit
                user.setSupervised_unit(user.getUnit());
            }
            return userDao.saveUser(user);
        } else if (userDetails.getPosition() == Position.SUPERVISOR) {
            if (user.getPosition() == Position.TECHNICIAN) {
                //A supervisor can only create technicians in the unit he supervises
                User supervisor = userDao.getUser(userDetails.getId());
                Unit unit = supervisor.getSupervised_unit();
                if (user.getUnitId() == unit.getId()) {
                    return userDao.saveUser(user);
                } 
                else
                {
                    throw new RestException(403, error_message);
                }
            } 
            else {
                throw new RestException(403, error_message);
            }
        } else {
            throw new RestException(403, error_message);
        }
    }

    public User performActionIfAuthorized(User user, long id, HttpServletRequest request, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);
        if (userDetails == null) {
            throw new RestException(403, error_message);
        }

        //A technician or user can only get details about their individual selves
        if (userDetails.getPosition() == Position.ADMIN) {
            //Admin can get details about any user
            if (action == Action.GET_USER) {
            	
                 User us=userDao.getUser(id);
                 if(us==null)
                	 throw new RestException(404, "User not found in database");
                 else
                	 return us;
            } else if (action == Action.SAVE_USER) {
                return userDao.saveUser(user);
            }
        } else if (userDetails.getPosition() == Position.SUPERVISOR) {
            //A supervisor can get details about himself and the technicians in his unit            
            if (id == userDetails.getId()) {
                //A supervisor can get details about himself
                if (action == Action.GET_USER) {
                	 User us=userDao.getUser(id);
                     if(us==null)
                    	 throw new RestException(404, "User not found in database");
                     else
                    	 return us;
                } else if (action == Action.SAVE_USER) {
                    return userDao.saveUser(user);
                }
            }
            User req_user = userDao.getUser(id);
            User currentSupervisor = userDao.getUser(userDetails.getId());
            List<User> technicians_in_my_unit = userDao.getTechnicians(currentSupervisor.getSupervised_unit());
            if (technicians_in_my_unit.contains(req_user)) {
                if (action == Action.GET_USER) {
                    return req_user;
                } else if (action == Action.SAVE_USER) {
                    return userDao.saveUser(user);
                }
            } else {
                throw new RestException(403, error_message);
            }
        } else if (userDetails.getPosition() == Position.TECHNICIAN || userDetails.getPosition() == Position.USER) {
            if (id == userDetails.getId()) {
                if (action == Action.GET_USER) {
                	 User us=userDao.getUser(id);
                     if(us==null)
                    	 throw new RestException(404, "User not found in database");
                     else
                    	 return us;
                } else if (action == Action.SAVE_USER) {
                    return userDao.saveUser(user);
                }
            } else {
                throw new RestException(403, error_message);
            }
        }
        /*if (userId == -1 || userId != id) {
            throw new RestException(403, error_message);
        }
        if (action == Action.GET_USER) {
            return userDao.getUser(id);
        } else if (action == Action.SAVE_USER) {
            return userDao.saveUser(user);
        } else {
            return null;
        }*/
        throw new RestException(403, error_message);
    }

    public List<Ticket> performActionIfAuthorized(long id, HttpServletRequest request, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);
        User user = userDao.getUser(id);
       
       
        
        if (userDetails == null) 
        {
            throw new RestException(403, error_message);
        }
        else if (userDetails.getPosition() == Position.ADMIN) 
        {
        	if(user==null)
                throw new RestException(404, "User not found in database");
            
            if (action == Action.GET_TICKETS_MADE_BY_USER) 
            {
              
              List<Ticket> tickets= ticketDao.getTickets(user);
              if(tickets.size()==0)
            	  throw new RestException(404, "No tickets found for the given user");
              else
            	  return tickets;
             }
            else if (action == Action.GET_TICKETS_ASSIGNED_TO_TECHNICIAN) 
            {
            	 List<Ticket> tickets=ticketDao.getTicketsAssignedToTechnician(user);
            	 if(tickets.size()==0)
               	  throw new RestException(404, "No tickets assigned to the technician");
                 else
               	  return tickets;
            }
        }  
        else if (userDetails.getPosition() == Position.USER && userDetails.getId() != id) 
        {
            throw new RestException(403, error_message);
        } 
        else if (action == Action.GET_TICKETS_MADE_BY_USER) 
        {
            if ((userDetails.getPosition() == Position.SUPERVISOR || userDetails.getPosition() == Position.TECHNICIAN) && userDetails.getId() != id) {
                throw new RestException(403, error_message);
            }
            if(user==null)
                throw new RestException(404, "User not found in database");
            
            return ticketDao.getTickets(user);
        }
        else if (action == Action.GET_TICKETS_ASSIGNED_TO_TECHNICIAN) 
        {
            User technician = userDao.getUser(id);
            User supervisor = userDao.getUser(userDetails.getId());
            if(supervisor.getUnit()==null)
            	 throw new RestException(403, "You do not have the authorization to view these tickets");
            
            Unit supervised_unit = supervisor.getUnit();
            List<User> technicians_in_unit = userDao.getTechnicians(supervised_unit);

            if (userDetails.getPosition() == Position.SUPERVISOR && technicians_in_unit.contains(technician)) {
            	if(user==null)
                    throw new RestException(404, "User not found in database");
                
                return ticketDao.getTicketsAssignedToTechnician(user);
            }
            else if (userDetails.getPosition() == Position.TECHNICIAN && userDetails.getId() == id) {
            	if(user==null)
                    throw new RestException(404, "User not found in database");
                
                return ticketDao.getTicketsAssignedToTechnician(user);
            }
        }
        throw new RestException(403, error_message);
    }

    public User buildUserProperly(JSONObject jsonObject) {
        long unitId = 1;
        if (jsonObject.get("unitId") != null) {
            unitId = (int) jsonObject.get("unitId");
        }
        Gson gson = new GsonBuilder().create();
        User user = gson.fromJson(jsonObject.toString(), User.class);
        if (user.getUsername() == null || user.getPassword() == null) {
            throw new RestException(403, "Missing username and/or password.");
        }
        if (jsonObject.get("unitId") != null) {
            Unit unit = unitDao.getUnit(unitId);
            user.setUnit(unit);
        }
        return user;
    }

    public User buildUserProperlyWithoutPassword(JSONObject jsonObject) {
        long unitId = 1;
        if (jsonObject.get("unitId") != null) {
            unitId = (int) jsonObject.get("unitId");
        }
        Gson gson = new GsonBuilder().create();
        User user = gson.fromJson(jsonObject.toString(), User.class);
        if (jsonObject.get("unitId") != null) {
            Unit unit = unitDao.getUnit(unitId);
            user.setUnit(unit);
        }
        return user;
    }

    @Autowired
    private UserDao userDao;

    @Autowired
    private TicketDao ticketDao;

    @Autowired
    private UnitDao unitDao;

    /*@RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestParam String username, @RequestParam String password) {
        if (username == null || password == null) {
            throw new RestException(403, "Missing username and/or password.");
        }
        String result = userDao.login(username, password);
        if (result == null) {
            throw new RestException(403, "Invalid username and/or password.");
        } else {
            return result;
        }
    }
*/
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public List<User> getUsers(HttpServletRequest request) {
        return performActionIfAdmin(request, Action.GET_USERS, "You do not have the authorization to view all users");
    }

    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
    public User getUserWithId(@PathVariable Long id, HttpServletRequest request) {
        return performActionIfAuthorized(null, id, request, Action.GET_USER, "You do not have the authorization to view this user");
    }

    /*
    @RequestMapping(value = "/users/username/{username}", method = RequestMethod.GET)
    public User getUserWithUsername(@PathVariable String username) {
        return userDao.getUser(username);
    }*/
    public User changeDetailsOfUser(User oldUser, User user) {
        //Check if there is any new info in the new user object
    	if(user==null)
            throw new RestException(404, "User not found in database");
    	String username = user.getUsername() == null ? oldUser.getUsername() : user.getUsername();
        String department = user.getDepartment() == null ? oldUser.getDepartment() : user.getDepartment();
        String email = user.getEmail() == null ? oldUser.getEmail() : user.getEmail();
        String firstName = user.getFirstName() == null ? oldUser.getFirstName() : user.getFirstName();
        String lastName = user.getLastName() == null ? oldUser.getLastName() : user.getLastName();
        String phoneNumber = user.getPhoneNumber() == null ? oldUser.getPhoneNumber() : user.getPhoneNumber();
        Position position = user.getPosition() == null ? oldUser.getPosition() : user.getPosition();
        Unit unit = user.getUnit() == null ? oldUser.getUnit() : user.getUnit();

        //Exchange the old detail with the new one
        oldUser.setUsername(username);
        oldUser.setDepartment(department);
        oldUser.setEmail(email);
        oldUser.setFirstName(firstName); 
        oldUser.setLastName(lastName);
        oldUser.setPhoneNumber(phoneNumber);
        oldUser.setPosition(position);
        oldUser.setUnit(unit);
        //Save the 'new' old user
        return userDao.saveUser(oldUser);
    }

    @RequestMapping(value = "/users/{id}", method = RequestMethod.PUT)
    public User editUser(HttpServletRequest request, @PathVariable Long id, @RequestBody JSONObject jsonObject) {
        UserDetails userDetails = checkRequestor(request);
        if (userDetails == null) {
            throw new RestException(403, "You do not have authorization to make this change");
        }
        User user = buildUserProperlyWithoutPassword(jsonObject);
        User oldUser = userDao.getUser(id);
        if (userDetails.getPosition() == Position.ADMIN) {
            return changeDetailsOfUser(oldUser, user);
        } else if (userDetails.getPosition() == Position.USER || userDetails.getPosition() == Position.TECHNICIAN) {
            if (userDetails.getId() == id) {
                return changeDetailsOfUser(oldUser, user);
            } else {
                throw new RestException(403, "You do not have authorization to make this change");
            }
        } else if (userDetails.getPosition() == Position.SUPERVISOR && userDetails.getId() == id) {
            return changeDetailsOfUser(oldUser, user);
        } else if (userDetails.getPosition() == Position.SUPERVISOR) {
            User supervisor = userDao.getUser(userDetails.getId());
            Unit supervised_unit = supervisor.getSupervised_unit();
            List<User> technicians_in_this_unit = userDao.getTechnicians(supervised_unit);
            User technician = userDao.getUser(id);
            if (technicians_in_this_unit.contains(technician)) {
                return changeDetailsOfUser(oldUser, user);
            } else {
                throw new RestException(403, "You do not have authorization to make this change");
            }
        } else {
            throw new RestException(403, "You do not have authorization to make this change");
        }
    }

    @RequestMapping(value = "/users/{id}/tickets", method = RequestMethod.GET)
    public List<Ticket> getTicketsSubmittedByUser(@PathVariable Long id, HttpServletRequest request) {
        return performActionIfAuthorized(id, request, Action.GET_TICKETS_MADE_BY_USER, "You do not have the authorization to view these tickets");
    }

    @RequestMapping(value = "/technicians/{id}/tickets", method = RequestMethod.GET)
    public List<Ticket> getTicketsAssignedToATechnician(@PathVariable Long id, HttpServletRequest request) {
        return performActionIfAuthorized(id, request, Action.GET_TICKETS_ASSIGNED_TO_TECHNICIAN, "You do not have the authorization to view these tickets");
    }

    /*
    @RequestMapping(value = "/users/position/{position}", method = RequestMethod.GET)
    public List<User> getUsersInAPosition(@PathVariable String position) {
        //Converting String to enum
        Position converted_position = User.Position.valueOf(position);
        //Converting enum to int
        int integer_value_of_position = converted_position.ordinal();
        return userDao.getUsers(integer_value_of_position);
    }

    @RequestMapping(value = "/users/technicians/{id}", method = RequestMethod.GET)
    public List<User> getTechniciansWorkingOnATicket(@PathVariable long id) {
        Ticket ticket = ticketDao.getTicket(id);
        return userDao.getTechnicians(ticket);
    }

    @RequestMapping(value = "/users/unit_supervisors/{id}", method = RequestMethod.GET)
    public List<User> getSupervisorsInAUnit(@PathVariable long id) {
        Unit unit = unitDao.getUnit(id);
        return userDao.getSupervisors(unit);
    }
     */
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public User addUser(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        User user = buildUserProperly(jsonObject);
        return performActionIfAuthorizedToCreate(user, request, Action.SAVE_USER, "You do not have the authorization to create a new user");
    }
}
