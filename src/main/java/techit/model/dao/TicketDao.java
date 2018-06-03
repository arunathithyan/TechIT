package techit.model.dao;

import java.util.List;
import techit.model.Ticket;
import techit.model.Unit;

import techit.model.User;

public interface TicketDao {

    Ticket getTicket(Long id);

    List<Ticket> getTickets();

    List<Ticket> getTickets(Unit unit);

    List<Ticket> getTickets(User requestor);

    Ticket saveTicket(Ticket ticket);
    
    List<User> assignTechnicianToTicket(Ticket ticket, User technician);
    
    List<Ticket> getTicketsAssignedToTechnician(User technician);
}