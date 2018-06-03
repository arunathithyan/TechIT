package techit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "updates")
public class Update implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @ManyToOne(targetEntity = Ticket.class)
    @JoinColumn(name = "ticketid", referencedColumnName = "id")
    private Ticket ticket;

    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "modifier", referencedColumnName = "id")
    private User modifier;

    @Column(nullable = false)
    private String updateDetails;
    
    @Column(nullable = false)
    private Date modifiedDate;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public void setModifier(User modifier) {
        this.modifier = modifier;
    }

    public void setUpdateDetails(String updateDetails) {
        this.updateDetails = updateDetails;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public User getModifier() {
        return modifier;
    }

    public String getUpdateDetails() {
        return updateDetails;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

}
