package techit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.google.gson.Gson;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonProperty(access = Access.WRITE_ONLY)
    @Transient
    private String password;
    
    @JsonProperty(access = Access.WRITE_ONLY)
    @Column(nullable = false)
    private String hash;
    
    /*private long unitId;

    public long getUnitId() {
        return unitId;
    }

    public void setUnitId(long unitId) {
        this.unitId = unitId;
    }*/

    public String getHash() {
        return hash;
    }
    
    @Column
    private String department;

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    public void setId(Long id) {
        this.id = id;
    }

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "position", columnDefinition = "int default 2")
    private Position position;

    //The unit the technician belongs to
    @ManyToOne(cascade = CascadeType.ALL, targetEntity = Unit.class)
    @JoinColumn(name = "unitid", referencedColumnName = "id")
    @JsonIgnore //Use this to solve the lazy loading error
    private Unit unit;
    
    public Long getUnitId() {
        return unit != null ? unit.getId() : null;
    }

    //The unit the technician supervises
    @JsonIgnore
    @ManyToOne
    @JoinTable(name = "supervisor_unit",
            joinColumns
            = @JoinColumn(name = "supervisor_id", referencedColumnName = "id"),
            inverseJoinColumns
            = @JoinColumn(name = "unit_id", referencedColumnName = "id")
    )
    private Unit supervised_unit;

    @JsonIgnore
    @OneToMany(targetEntity = Ticket.class, mappedBy = "reqUser", cascade = CascadeType.ALL)
    private List<Ticket> user_created_tickets;

    @JsonIgnore
    @OneToMany(targetEntity = Update.class, mappedBy = "modifier", cascade = CascadeType.ALL)
    private List<Update> updates_made_by_technician;

    @JsonIgnore
    @ManyToMany(mappedBy = "technicians")
    private List<Ticket> tickets_technician_is_working_on;

    public enum Position {
        ADMIN, SUPERVISOR, TECHNICIAN, USER;
    };

    /*@Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", password=" + password + ", enabled=" + enabled
                + ", firstName=" + firstName + ", lastName=" + lastName + ", phoneNumber=" + phoneNumber
                + ", department=" + department + ", email=" + email + ", position=" + position + ", unit=" + unit
                + "]";
    }*/
    
    @Override
    public String toString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public void setSupervised_unit(Unit supervised_unit) {
        this.supervised_unit = supervised_unit;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public Position getPosition() {
        return position;
    }

    public Unit getUnit() {
        return unit;
    }

    public Unit getSupervised_unit() {
        return supervised_unit;
    }

    public List<Ticket> getUser_created_tickets() {
        return user_created_tickets;
    }

    public List<Update> getUpdates_made_by_technician() {
        return updates_made_by_technician;
    }

    public List<Ticket> getTickets_technician_is_working_on() {
        return tickets_technician_is_working_on;
    }
}
