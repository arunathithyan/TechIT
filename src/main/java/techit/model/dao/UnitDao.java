package techit.model.dao;

import java.util.List;
import techit.model.Ticket;
import techit.model.Unit;

import techit.model.User;

public interface UnitDao {

    Unit getUnit(Long id);

    List<Unit> getUnits();
    
    //get units a user supervises
    List<Unit> getUnits(User user);

    Unit saveUnit(Unit unit);
}
