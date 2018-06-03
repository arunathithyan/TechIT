package techit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "units")
public class Unit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    public void setUnit_tickets(List<Update> unit_tickets) {
        this.unit_tickets = unit_tickets;
    }

    public void setTechnicians_in_this_unit(List<User> technicians_in_this_unit) {
        this.technicians_in_this_unit = technicians_in_this_unit;
    }

    public void setSupervisors(List<User> supervisors) {
        this.supervisors = supervisors;
    }

    @Column(nullable = false)
    private String name; // Name of the department.

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String email;

    @Column
    private String description;

    @Column
    private String phone;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @JsonIgnore
    @OneToMany(targetEntity = Ticket.class, mappedBy = "unit", cascade = CascadeType.ALL)
    private List<Update> unit_tickets;

    //Needs a way of determining technicians in this unit
    @JsonIgnore
    @OneToMany(targetEntity = User.class, mappedBy = "unit", cascade = CascadeType.ALL)
    private List<User> technicians_in_this_unit;

    //Needs a way of determining supervisors in this unit
    @JsonIgnore
    @OneToMany(targetEntity = User.class, mappedBy = "supervised_unit", cascade = CascadeType.ALL)
    private List<User> supervisors;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getEmail() {
        return email;
    }

    public List<Update> getUnit_tickets() {
        return unit_tickets;
    }

    public List<User> getTechnicians_in_this_unit() {
        return technicians_in_this_unit;
    }

    public List<User> getSupervisors() {
        return supervisors;
    }

}
