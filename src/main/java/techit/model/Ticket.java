package techit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "tickets")
public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "progress", columnDefinition = "int default 0")
    private Progress currentProgress; // Current progress of the ticket
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "priority", columnDefinition = "int default 0")
    private Priority currentPriority; // Importance or level of urgency of the ticket
    @Column(nullable = false)
    private String subject;			// Subject of the ticket.
    @Column(nullable = false, columnDefinition = "varchar(3000)")
    private String details; 		// Text concerning the project.
    @Column(nullable = false)
    private Date startDate; 		// Project's starting date.
    @Column
    private Date dateAssigned;
    @Column
    private Date dateUpdated; 	
    
    @Column
    private String location;
    

    public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}


	@Column(name = "created_for_name")
    private String createdForName;


	@Column(name = "created_for_email", nullable = false)
    private String createdForEmail;

    @Column(name = "created_for_phone")
    private String createdForPhone;

    @Column(name = "created_for_department")
    private String createdForDepartment;
    
    @Column
    private Date endDate; 			// When the project was completed.
    
    public void setUpdates(List<Update> updates) {
        this.updates = updates;
    }

   
	public String getCreatedForName() {
		return createdForName;
	}

	public void setCreatedForName(String createdForName) {
		this.createdForName = createdForName;
	}

	public String getCreatedForEmail() {
		return createdForEmail;
	}

	public void setCreatedForEmail(String createdForEmail) {
		this.createdForEmail = createdForEmail;
	}

	public String getCreatedForPhone() {
		return createdForPhone;
	}

	public void setCreatedForPhone(String createdForPhone) {
		this.createdForPhone = createdForPhone;
	}

	public String getCreatedForDepartment() {
		return createdForDepartment;
	}

	public void setCreatedForDepartment(String createdForDepartment) {
		this.createdForDepartment = createdForDepartment;
	}

    public Date getDateAssigned() {
        return dateAssigned;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public void setDateAssigned(Date dateAssigned) {
        this.dateAssigned = dateAssigned;
    }
    

    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    //My class 'userid' and User class 'id'
    @JoinColumn(name = "userid", referencedColumnName = "id")
    private User reqUser;

    @JsonIgnore
    @ManyToOne(targetEntity = Unit.class)
    @JoinColumn(name = "unitid", referencedColumnName = "id")
    private Unit unit;
    
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "ticket_technicians",
        joinColumns = @JoinColumn(name = "ticket_id", referencedColumnName="id"),
        inverseJoinColumns = @JoinColumn(name = "technician_id", referencedColumnName="id"))
    private List<User> technicians;

    //In Update class, there would be a property called 'ticket'
    @JsonIgnore
    @OneToMany(targetEntity = Update.class, mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<Update> updates;

    public enum Progress {
        OPEN, INPROGRESS, ONHOLD, COMPLETED, CLOSED;
    };

    public void setId(Long id) {
        this.id = id;
    }

    public enum Priority {
        NA, LOW, MEDIUM, HIGH;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setCurrentProgress(Progress currentProgress) {
        this.currentProgress = currentProgress;
    }

    public void setCurrentPriority(Priority currentPriority) {
        this.currentPriority = currentPriority;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setReqUser(User reqUser) {
        this.reqUser = reqUser;
    }

    public void setTechnicians(List<User> technicians) {
        this.technicians = technicians;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }
    
    public Long getReqUserId() {
        return reqUser != null ? reqUser.getId() : null;
    }

    public Long getId() {
        return id;
    }

    public Progress getCurrentProgress() {
        return currentProgress;
    }

    public Priority getCurrentPriority() {
        return currentPriority;
    }

    public String getSubject() {
        return subject;
    }

    public String getDetails() {
        return details;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public User getReqUser() {
        return reqUser;
    }

    public Unit getUnit() {
        return unit;
    }

    public List<User> getTechnicians() {
        return technicians;
    }

    public List<Update> getUpdates() {
        return updates;
    }
    
}
