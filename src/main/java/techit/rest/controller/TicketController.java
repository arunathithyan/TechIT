package techit.rest.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.servlet.http.HttpServletRequest;
import org.jboss.jandex.PositionBasedTypeTarget;
import org.json.simple.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import techit.model.Ticket;
import techit.model.Ticket.Priority;
import techit.model.Ticket.Progress;

import techit.model.User;
import techit.model.Unit;
import techit.model.Update;
import techit.model.User.Position;
import techit.model.UserDetails;
import techit.model.dao.TicketDao;
import techit.model.dao.UnitDao;
import techit.model.dao.UpdateDao;
import techit.model.dao.UserDao;
import techit.rest.error.RestException;

@RestController
public class TicketController {

    @Autowired
    private TicketDao ticketDao;

    @Autowired
    private UnitDao unitDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UpdateDao updateDao;

    public enum Action {
        GET_USERS, SAVE_USER, GET_UNITS, SAVE_TICKET, SET_PRIORITY_OF_TICKET, ADD_UPDATE_TO_TICKET, ASSIGN_TECHNICIAN_TO_TICKET, SET_STATUS_OF_TICKET, GET_TICKETS, GET_TECHNICIANS, GET_TICKETS_IN_UNIT, SAVE_UNIT, GET_USER, GET_TICKETS_MADE_BY_USER, GET_TICKETS_ASSIGNED_TO_TECHNICIAN;
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

    public List<Ticket> performActionIfAdmin(HttpServletRequest request, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);
        if (userDetails == null || userDetails.getPosition() != Position.ADMIN) {
            throw new RestException(400, error_message);
        }
        if (action == Action.GET_TICKETS) {
            return ticketDao.getTickets();
        } else {
            return null;
        }
    }

    public List<User> performActionIfAdminOrUnitSupervisor(long id, HttpServletRequest request, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);
        //Only the admin or the supervisor for this unit can view these technicians
        Ticket ticket = ticketDao.getTicket(id);
        if (userDetails == null) {
            throw new RestException(403, error_message);
        } else if (userDetails.getPosition() == Position.ADMIN) {
            if (action == Action.GET_TICKETS_ASSIGNED_TO_TECHNICIAN) {
            	 if (ticket == null) {
                     throw new RestException(404, "That ticket does not exist");
                 }
            	 List<User> u=userDao.getTechnicians(ticket);
            	 if(u==null || u.size()==0)
            		 throw new RestException(404, "No Technicians found for the given ticket");
                return u;
            }
        } else {
            if (action == Action.GET_TICKETS_ASSIGNED_TO_TECHNICIAN) {
                User user = userDao.getUser(userDetails.getId());
                if (ticket == null) {
                    throw new RestException(404, "That ticket does not exist");
                }
                Unit tickets_unit = ticket.getUnit();
                List<User> supervisors_of_this_unit = userDao.getSupervisors(tickets_unit);
                if (supervisors_of_this_unit.contains(user)) {
                	
               	 List<User> u=userDao.getTechnicians(ticket);
               	 if(u==null || u.size()==0)
               		 throw new RestException(404, "No Technicians found for the given ticket");
                 return u;
                    
                }
                else if(ticket.getReqUserId() == userDetails.getId())
                {
                	
                  	 List<User> u=userDao.getTechnicians(ticket);
                  	 if(u==null || u.size()==0)
                  		 throw new RestException(404, "No Technicians found for the given ticket");
                    return u;
                }
            }
        }
        throw new RestException(403, error_message);
    }

    public List<User> performActionIfAdminOrUnitSupervisor(long ticketId, long techId, HttpServletRequest request, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);
        //Only the admin or the supervisor for this unit can assign a technician
        Ticket ticket = ticketDao.getTicket(ticketId);
        User user = userDao.getUser(techId);
        if (userDetails == null) {
            throw new RestException(403, error_message);
        } else if (userDetails.getPosition() == Position.ADMIN) {
            if (action == Action.ASSIGN_TECHNICIAN_TO_TICKET) {
           	 if (ticket == null || user==null) {
                 throw new RestException(404, "Ticket/User does not exist");
             }
                return ticketDao.assignTechnicianToTicket(ticket, user);
            }
        } else {
            if (action == Action.ASSIGN_TECHNICIAN_TO_TICKET) {
                Unit tickets_unit = ticket.getUnit();
                List<User> supervisors_of_this_unit = userDao.getSupervisors(tickets_unit);
                User user_making_request = userDao.getUser(userDetails.getId());
                if (supervisors_of_this_unit.contains(user_making_request)) {
                	 if (ticket == null || user==null) {
                         throw new RestException(404, "That Ticket/User does not exist");
                     }
                    return ticketDao.assignTechnicianToTicket(ticket, user);
                } else if (userDetails.getId() == techId && ticket.getUnit().getId() == user_making_request.getUnitId()) {
                	 if (ticket == null || user==null) {
                         throw new RestException(404, "That Ticket/User does not exist");
                     }
                    return ticketDao.assignTechnicianToTicket(ticket, user);
                }
            }
        }
        throw new RestException(403, error_message);
    }

    public Ticket performActionIfAdminOrUnitSupervisor(Update update, long ticketId, HttpServletRequest request, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);
        //Only the admin or the supervisor for this unit can assign a technician
        Ticket ticket = ticketDao.getTicket(ticketId);
        
        if (userDetails == null) {
            throw new RestException(403, error_message);
        }
        User user = userDao.getUser(userDetails.getId());
        if (userDetails.getPosition() == Position.ADMIN) {
            if (action == Action.ADD_UPDATE_TO_TICKET) {
                update.setModifier(user);
                update.setTicket(ticket);
                updateDao.saveUpdate(update);
                ticket.setDateUpdated(new Date());
                return ticketDao.saveTicket(ticket);
            }
        } else if (userDetails.getPosition() == Position.SUPERVISOR) {
            throw new RestException(403, error_message);
        } else if (userDetails.getPosition() == Position.TECHNICIAN) {
            List<User> techincians_assigned_to_ticket = userDao.getTechnicians(ticket);
            if (!techincians_assigned_to_ticket.contains(user)) {
                throw new RestException(403, error_message);
            }
        } else {
            if (action == Action.ADD_UPDATE_TO_TICKET) {
                update.setModifier(user);
                update.setTicket(ticket);
                updateDao.saveUpdate(update);
                ticket.setDateUpdated(new Date());
                return ticketDao.saveTicket(ticket);
            }
        }
        return null;
    }

    public Ticket performActionIfAdminOrUnitSupervisor(Update update, long ticketId, Progress status, HttpServletRequest request, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);
        //Only the admin or the supervisor for this unit can assign a technician
        Ticket ticket = ticketDao.getTicket(ticketId);
        
        if (userDetails == null) {
            throw new RestException(403, error_message);
        }
        User user = userDao.getUser(userDetails.getId());
        if (userDetails.getPosition() == Position.ADMIN) {
            if (action == Action.SET_STATUS_OF_TICKET) {
                //Change status
                ticket.setCurrentProgress(status);
                ticket.setDateUpdated(new Date());
                ticketDao.saveTicket(ticket);
                //Create update
                update.setModifier(user);
                update.setTicket(ticket);
                updateDao.saveUpdate(update);
                return ticket;
            }
        } else if (userDetails.getPosition() == Position.USER) {
            throw new RestException(403, error_message);
        } else if (userDetails.getPosition() == Position.TECHNICIAN) {
            List<User> technicians_assigned_to_this_ticket = userDao.getTechnicians(ticket);
            if (!technicians_assigned_to_this_ticket.contains(user)) {
                throw new RestException(403, error_message);
            }
        } else if (userDetails.getPosition() == Position.SUPERVISOR) {
            List<User> supervisors_in_tickets_unit = userDao.getSupervisors(ticket.getUnit());
            if (!supervisors_in_tickets_unit.contains(user)) {
                throw new RestException(403, error_message);
            }
        } else {
            if (action == Action.SET_STATUS_OF_TICKET) {
                //Change status
                ticket.setCurrentProgress(status);
                ticket.setDateUpdated(new Date());
                ticketDao.saveTicket(ticket);
                //Create update
                update.setModifier(user);
                update.setTicket(ticket);
                updateDao.saveUpdate(update);
                return ticket;
            }
        }
        return null;
    }

    public Ticket performActionIfAdminOrUnitSupervisor(long ticketId, Priority priority, HttpServletRequest request, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);
        //Only the admin or the supervisor for this unit can assign a technician
        Ticket ticket = ticketDao.getTicket(ticketId);
        if (userDetails == null) {
            throw new RestException(403, error_message);
        } else if (userDetails.getPosition() == Position.ADMIN) {
            if (action == Action.SET_PRIORITY_OF_TICKET) {
                ticket.setCurrentPriority(priority);
                return ticketDao.saveTicket(ticket);
            }
        } else if (userDetails.getPosition() == Position.USER) {
            if (ticket.getReqUserId() != userDetails.getId()) {
                throw new RestException(403, error_message);
            }
        } else if (userDetails.getPosition() == Position.TECHNICIAN) {
            throw new RestException(403, error_message);
        } else if (userDetails.getPosition() == Position.SUPERVISOR) {
            List<User> supervisors_in_tickets_unit = userDao.getSupervisors(ticket.getUnit());
            if (!supervisors_in_tickets_unit.contains(userDao.getUser(userDetails.getId()))) {
                throw new RestException(403, error_message);
            }
        } else {
            if (action == Action.SET_PRIORITY_OF_TICKET) {
                ticket.setCurrentPriority(priority);
                return ticketDao.saveTicket(ticket);
            }
        }
        return null;
    }

    public Ticket performActionIfJWTIsValid(HttpServletRequest request, JSONObject jsonObject, Action action, String error_message) {
        UserDetails userDetails = checkRequestor(request);


        
        if (userDetails == null) 
        {
            throw new RestException(403, error_message);
        }
        Ticket ticket = buildTicketProperly(jsonObject, userDetails.getId());
        if (action == Action.SAVE_TICKET) 
        {
            return ticketDao.saveTicket(ticket);
        } 
        else
        {
            return null;
        }
    }

    public Ticket buildTicketProperly(JSONObject jsonObject, long userId) {
        long unitId = (int) jsonObject.get("unitid");
        Gson gson = new GsonBuilder().create();
        Ticket ticket = gson.fromJson(jsonObject.toString(), Ticket.class);
        Unit unit = unitDao.getUnit(unitId);
        User user = userDao.getUser(userId);
        ticket.setUnit(unit);
        ticket.setReqUser(user);
        return ticket;
    }

    @RequestMapping(value = "/tickets", method = RequestMethod.GET)
    public List<Ticket> getTickets(HttpServletRequest request) {
        return performActionIfAdmin(request, Action.GET_TICKETS, "You do not have proper authorization");
    }

    @RequestMapping(value = "/tickets/{id}", method = RequestMethod.GET)
    public Ticket getTicketWithId(@PathVariable Long id, HttpServletRequest request) {
        UserDetails userDetails = checkRequestor(request);
        Ticket ticket = ticketDao.getTicket(id);
        if (userDetails == null) {
            throw new RestException(403, "You do not have proper authorization");
        }  
        if (ticket == null) {
            throw new RestException(404, "That ticket does not exist");
        }
        if (userDetails.getPosition() == Position.ADMIN) {
        	
            return ticket;
        }
        
        
        //Admin and people in the unit the ticket was passed to can view the ticket
        //The person who made the ticket can view it too
        
        User user = userDao.getUser(userDetails.getId());
        if (ticket.getReqUserId() == userDetails.getId()) {
            return ticket;
        } else if (userDetails.getPosition() == Position.USER && ticket.getReqUserId() != userDetails.getId()) {
            throw new RestException(403, "You do not have proper authorization");
        } else if (userDetails.getPosition() == Position.TECHNICIAN && ticket.getUnit().getId() != user.getUnitId()) {
            throw new RestException(403, "You do not have proper authorization");
        }
        if (user.getUnit().getId() == ticket.getUnit().getId() || user.getId() == ticket.getReqUserId()) {
            return ticket;
        } else {
            throw new RestException(403, "You do not have proper authorization");
        }
    }

    @RequestMapping(value = "/tickets/{id}", method = RequestMethod.PUT)
    public Ticket editTicketWithId(@PathVariable Long id, HttpServletRequest request, @RequestBody Ticket ticket) {
        UserDetails userDetails = checkRequestor(request);
        Ticket oldTicket = ticketDao.getTicket(id);
        //copy new details from new ticket into old ticket
        if (userDetails == null) {
            throw new RestException(403, "You do not have proper authorization");
        } else if (oldTicket == null) {
            throw new RestException(404, "That ticket does not exist");
        }
        Priority priority = ticket.getCurrentPriority() == null ? oldTicket.getCurrentPriority() : ticket.getCurrentPriority();
        Progress progress = ticket.getCurrentProgress() == null ? oldTicket.getCurrentProgress() : ticket.getCurrentProgress();
        Date date_assigned = ticket.getDateAssigned() == null ? oldTicket.getDateAssigned() : ticket.getDateAssigned();
        Date date_updated = ticket.getDateUpdated() == null ? oldTicket.getDateUpdated() : ticket.getDateUpdated();
        String details = ticket.getDetails() == null ? oldTicket.getDetails() : ticket.getDetails();
        Date date_ended = ticket.getEndDate() == null ? oldTicket.getEndDate() : ticket.getEndDate();
        Date date_started = ticket.getStartDate() == null ? oldTicket.getStartDate() : ticket.getStartDate();
        String subject = ticket.getSubject() == null ? oldTicket.getSubject() : ticket.getSubject();
        String createdForName=ticket.getCreatedForName() == null ? oldTicket.getCreatedForName() : ticket.getCreatedForName();
        String createdForEmail=ticket.getCreatedForEmail() == null ? oldTicket.getCreatedForEmail() : ticket.getCreatedForEmail();
        String createdForPhone=ticket.getCreatedForPhone() == null ? oldTicket.getCreatedForPhone() : ticket.getCreatedForPhone();
        String createdForDepartment=ticket.getCreatedForDepartment() == null ? oldTicket.getCreatedForDepartment() : ticket.getCreatedForDepartment();
        String location=ticket.getLocation()==null?oldTicket.getLocation():ticket.getLocation();
        
        
        
        oldTicket.setCurrentPriority(priority);
        oldTicket.setCurrentProgress(progress);
        oldTicket.setDateAssigned(date_assigned);
        oldTicket.setDateUpdated(date_updated);
        oldTicket.setDetails(details);
        oldTicket.setEndDate(date_ended);
        oldTicket.setStartDate(date_started);
        oldTicket.setSubject(subject);
        oldTicket.setCreatedForName(createdForName);
        oldTicket.setCreatedForEmail(createdForEmail);
        oldTicket.setCreatedForPhone(createdForPhone);
        oldTicket.setCreatedForDepartment(createdForDepartment);
        oldTicket.setLocation(location);

      
        //Admin and people in the unit the ticket was passed to can view the ticket
        //The person who made the ticket can view it too
        User user = userDao.getUser(userDetails.getId());
        if (oldTicket.getReqUserId() == userDetails.getId()) {
            return ticketDao.saveTicket(oldTicket);
        }
        if (userDetails.getPosition() == Position.USER && oldTicket.getReqUserId() != userDetails.getId()) {
            throw new RestException(403, "You do not have proper authorization");
        }
        if (userDetails.getPosition() == Position.ADMIN || user.getUnit().getId() == oldTicket.getUnit().getId() || user.getId() == oldTicket.getReqUserId()) {
            return ticketDao.saveTicket(oldTicket);
        } else {
            throw new RestException(403, "You do not have proper authorization");
        }
    }
    /*

    @RequestMapping(value = "/tickets/unitid/{unitid}", method = RequestMethod.GET)
    public List<Ticket> getTicketsInUnit(@PathVariable Long unitid) {
        Unit unit = new Unit();
        unit.setId(unitid);
        return ticketDao.getTickets(unit);
    }

    @RequestMapping(value = "/tickets/user/{id}", method = RequestMethod.GET)
    public List<Ticket> getTicketsMadeByUser(@PathVariable Long id) {
        User user = new User();
        user.setId(id);
        return ticketDao.getTickets(user);
    }*/

    @RequestMapping(value = "/tickets/{id}/technicians", method = RequestMethod.GET)
    public List<User> getTicketsAssignedToTechnician(@PathVariable Long id, HttpServletRequest request) {
        return performActionIfAdminOrUnitSupervisor(id, request, Action.GET_TICKETS_ASSIGNED_TO_TECHNICIAN, "You are not authorized to perform this action");
    }

    @RequestMapping(value = "/tickets/{ticketId}/technicians/{userId}", method = RequestMethod.PUT)
    public List<User> assignTechnicianToATicket(@PathVariable Long ticketId, @PathVariable Long userId, HttpServletRequest request) {
        return performActionIfAdminOrUnitSupervisor(ticketId, userId, request, Action.ASSIGN_TECHNICIAN_TO_TICKET, "You are not authorized to perform this action");
    }

    @RequestMapping(value = "/tickets/{ticketId}/status/{status}", method = RequestMethod.PUT)
    public Ticket setStatusOfTicket(@RequestBody Update update, @PathVariable Long ticketId, @PathVariable Progress status, HttpServletRequest request) {
        return performActionIfAdminOrUnitSupervisor(update, ticketId, status, request, Action.SET_STATUS_OF_TICKET, "You are not authorized to perform this action");
    }

    @RequestMapping(value = "/tickets/{ticketId}/priority/{priority}", method = RequestMethod.PUT)
    public Ticket setPriorityOfTicket(@PathVariable Long ticketId, @PathVariable Priority priority, HttpServletRequest request) {
        return performActionIfAdminOrUnitSupervisor(ticketId, priority, request, Action.SET_PRIORITY_OF_TICKET, "You are not authorized to perform this action");
    }

    @RequestMapping(value = "/tickets/{ticketId}/updates", method = RequestMethod.POST)
    public Ticket AddUpdateToTicket(@RequestBody Update update, @PathVariable Long ticketId, HttpServletRequest request) {
        return performActionIfAdminOrUnitSupervisor(update, ticketId, request, Action.ADD_UPDATE_TO_TICKET, "You are not authorized to perform this action");
    }

    @RequestMapping(value = "/tickets", method = RequestMethod.POST)
    public Ticket addTicket(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        return performActionIfJWTIsValid(request, jsonObject, Action.SAVE_TICKET, "You do not have proper authorization");
    }
}
