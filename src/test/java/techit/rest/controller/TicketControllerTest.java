package techit.rest.controller;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import techit.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Date;

import org.hamcrest.Matchers;
import org.json.simple.JSONObject;
import techit.model.Ticket;
import techit.model.Ticket.Priority;
import techit.model.Ticket.Progress;
import techit.model.dao.UnitDao;
import techit.model.dao.UserDao;

@Test(groups = "TicketController")
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:techit-servlet.xml"})
public class TicketControllerTest extends AbstractTransactionalTestNGSpringContextTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    UserDao userDao;

    @Autowired
    UnitDao unitDao;

    @BeforeClass
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
  //Only the admin can get details of all the tickets
    @Test
    void getAllTicketsByAdmin() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
  //No one else can get details of every ticket
    @Test
    void getAllTicketsByOtherUser() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(3L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
    //The admin can create a new ticket
    @Test
    void CreateTicketByAdmin() throws Exception {

        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("details", "Details new");
        jsonObject.put("subject", "New Subject");
        jsonObject.put("unitid", 1L);
        jsonObject.put("createdForEmail", "abc@g.com");

        mockMvc.perform(post("/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }
  //A user can create a new ticket
    @Test
    void CreateTicketByUser() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(5L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("details", "Details new");
        jsonObject.put("subject", "New Subject");
        jsonObject.put("unitid", 1L);
        jsonObject.put("createdForEmail", "abc@g.com");

        mockMvc.perform(post("/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }
  //Technicians and supervisors cannot create a new ticket
    @Test
    void CreateTicketByTechnician() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(22L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("details", "Details new");
        jsonObject.put("subject", "New Subject");
        jsonObject.put("unitid", 1L);
        jsonObject.put("createdForEmail", "abc@g.com");

        mockMvc.perform(post("/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }
    
    //Creating ticket without complete data(Not entering createdforEmail data)
    @Test
    void CreateTicketWithInvalidData() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(22L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("details", "Details new");
        jsonObject.put("subject", "New Subject");
        jsonObject.put("unitid", 1L);
        //jsonObject.put("createdForEmail", "abc@g.com");

        mockMvc.perform(post("/tickets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is5xxServerError());
    }
    
    
  //The admin can get a ticket by ID
    @Test
    void getTicketByIdByAdmin() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets/{ticketId}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
  //A user can get a ticket by ID if he created it
    @Test
    void getTicketByIdByUserWhoCreatedTheTicket() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(5L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets/{ticketId}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
  //A user cannot get a ticket by ID if he did not create it
    @Test
    void getTicketByIdByUserWhoDidntCreateTheTicket() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(8L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets/{ticketId}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
  //A supervisor or technician can get a ticket by ID if the ticket was sent to his unit
    @Test
    void getTicketByIdByTechnicianInTicketUnit() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(4L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets/{ticketId}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
  //A supervisor cannot get a ticket by ID if the ticket wasn't sent to his unit
    @Test
    void getTicketByIdByTechnicianInDifferentTicketUnit() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(6L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets/{ticketId}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
  //Admin can edit the ticket
    @Test
    void editTicketByIdByAdmin() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("details", "Details new");
        jsonObject.put("subject", "New Subject");

        mockMvc.perform(put("/tickets/{ticketId}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }
  //Users can edit tickets they created
    @Test
    void editTicketByUserWhoCreatedTheTicket() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(5L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("details", "Details new");
        jsonObject.put("subject", "New Subject");

        mockMvc.perform(put("/tickets/{ticketId}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }
  //Users cannot edit tickets they did not create
    @Test
    void editTicketByUserWhoDidNotCreatedTheTicket() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(8L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("details", "Details new");
        jsonObject.put("subject", "New Subject");

        mockMvc.perform(put("/tickets/{ticketId}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is4xxClientError());
    }
  //The supervisor can edit tickets in his unit
    @Test
    void editTicketBySupervisorTicketWasSentTo() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(7L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("details", "Details new");
        jsonObject.put("subject", "New Subject");

        mockMvc.perform(put("/tickets/{ticketId}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }
  //Technicians cannot edit tickets in other units
    @Test
    void editTicketByTechnicianTicketWasNotSentTo() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(6L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("details", "Details new");
        jsonObject.put("subject", "New Subject");

        mockMvc.perform(put("/tickets/{ticketId}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is4xxClientError());
    }
    //The admin can get the technicians assigned to a ticket
    @Test
    void getTechniciansATicketIsAssignedToByAdmin() throws Exception {

        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets/{ticketId}/technicians", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
  //The technician cannot get the technicians assigned to a ticket
    @Test
    void getTechniciansATicketIsAssignedToByTechnician() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(4L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets/{ticketId}/technicians", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
  //The supervisor for that unit can get the technicians assigned to a ticket
    @Test
    void getTechniciansATicketIsAssignedToBySupervisorOfUnit() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(22L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets/{ticketId}/technicians", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    //Other supervisors cannot get the technicians assigned to a ticket
    @Test
    void getTechniciansATicketIsAssignedToBySupervisorOfOtherUnit() throws Exception {
    
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(3L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets/{ticketId}/technicians", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
  
    //A user can get the technicians assigned ,for the ticket created by him
    
    @Test
    void getTechniciansATicketIsAssignedToByUser() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(5L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets/{ticketId}/technicians", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void assignTechnicianToTicketByAdmin() throws Exception {
        //The admin can assign a technician to a ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(put("/tickets/{ticketId}/technicians/{userId}", 1L, 9L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void assignTechnicianToTicketByUser() throws Exception {
        //A user cannot assign a technician to a ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(5L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(put("/tickets/{ticketId}/technicians/{userId}", 1L, 9L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void assignTechnicianToTicketBySupervisorOfThisUnit() throws Exception {
        //A  supervisor of the unit the ticket was sent to can assign a technician to the ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(22L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(put("/tickets/{ticketId}/technicians/{userId}", 1L, 9L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void assignTechnicianToTicketBySupervisorOfOtherUnit() throws Exception {
        //Other unit supervisors cannot assign a technician to a ticket when it wasn't sent to thier unit
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(3L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(put("/tickets/{ticketId}/technicians/{userId}", 1L, 9L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void assignTechnicianToTicketByTechnicianOfThisUnit() throws Exception {
        //A technician can assign a ticket to himself if the ticket was sent to his unit
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(9L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(put("/tickets/{ticketId}/technicians/{userId}", 1L, 9L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void assignTechnicianToTicketByTechnicianOfOtherUnit() throws Exception {
        //A technician cannot assign a ticket to himself if the ticket wasn't sent to his unit
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(4L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(put("/tickets/{ticketId}/technicians/{userId}", 1L, 9L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void changeStatusOfTicketByAdmin() throws Exception {
        //An admin can change the status of a ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("updateDetails", "New Update");

        mockMvc.perform(put("/tickets/{ticketId}/status/{status}", 1L, Progress.INPROGRESS)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    void changeStatusOfTicketByUser() throws Exception {
        //A user cannot change the status of a ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(5L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("updateDetails", "New Update");

        mockMvc.perform(put("/tickets/{ticketId}/status/{status}", 1L, Progress.INPROGRESS)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void changeStatusOfTicketByTechnicianAssignedToTicket() throws Exception {
        //A technician assigned to the ticket can change the status of the ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(4L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("updateDetails", "New Update");

        mockMvc.perform(put("/tickets/{ticketId}/status/{status}", 1L, Progress.INPROGRESS)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    void changeStatusOfTicketByTechnicianNotAssignedToTicket() throws Exception {
        //Technicians who aren't working on the ticket cannot change the status of the ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(6L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("updateDetails", "New Update");

        mockMvc.perform(put("/tickets/{ticketId}/status/{status}", 1L, Progress.INPROGRESS)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void changeStatusOfTicketBySupervisorOfThisUnit() throws Exception {
        //Supervisors of the unit the ticket was sent to can change the status of the ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(22L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("updateDetails", "New Update");

        mockMvc.perform(put("/tickets/{ticketId}/status/{status}", 1L, Progress.INPROGRESS)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    void changeStatusOfTicketBySupervisorOfOtherUnit() throws Exception {
        //Supervisors of other units cannot change the status of the ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(3L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("updateDetails", "New Update");

        mockMvc.perform(put("/tickets/{ticketId}/status/{status}", 1L, Progress.INPROGRESS)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void setPriorityOfTicketByAdmin() throws Exception {
        //The admin can set the priority of a ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(put("/tickets/{ticketId}/priority/{priority}", 1L, Priority.HIGH)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void setPriorityOfTicketByUserWhoCreatedTheTicket() throws Exception {
        //A user can set the priority of a ticket he created
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(5L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(put("/tickets/{ticketId}/priority/{priority}", 1L, Priority.HIGH)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void setPriorityOfTicketByUserWhoDidNotCreateTheTicket() throws Exception {
        //A user cannot set the priority of a ticket he did not create
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(8L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(put("/tickets/{ticketId}/priority/{priority}", 1L, Priority.HIGH)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void setPriorityOfTicketByTechnician() throws Exception {
        //A technician cannot set the priority of a ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(4L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(put("/tickets/{ticketId}/priority/{priority}", 1L, Priority.HIGH)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void setPriorityOfTicketBySupervisorInUnit() throws Exception {
        //A supervisor in the unit the ticket was sent to can set the priority of a ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(22L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(put("/tickets/{ticketId}/priority/{priority}", 1L, Priority.HIGH)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void setPriorityOfTicketBySupervisorInDifferentUnit() throws Exception {
        //A supervisor of a different unit cannot set the priority of a ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(3L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(put("/tickets/{ticketId}/priority/{priority}", 1L, Priority.HIGH)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createAnUpdateForATicket() throws Exception {
        //An admin can create an update for a ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("updateDetails", "New Update");

        mockMvc.perform(post("/tickets/{ticketId}/updates", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    void createAnUpdateForATicketByAdmin() throws Exception {
        //A user cannot create an update for a ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("updateDetails", "New Update");

        mockMvc.perform(post("/tickets/{ticketId}/updates", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    void createAnUpdateForATicketBySupervisor() throws Exception {
        //A supervisor cannot create an update for a ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(3L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("updateDetails", "New Update");

        mockMvc.perform(post("/tickets/{ticketId}/updates", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createAnUpdateForATicketByTechnicianAssignedToTicket() throws Exception {
        //A technician assigned to a ticket can create an update for the ticket
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(4L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("updateDetails", "New Update");

        mockMvc.perform(post("/tickets/{ticketId}/updates", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    void createAnUpdateForATicketByTechnicianNotAssignedToTicket() throws Exception {
        //Other technicians cannot make create updates
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(6L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("updateDetails", "New Update");

        mockMvc.perform(post("/tickets/{ticketId}/updates", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is4xxClientError());
    }
  //A user who did not create the ticket cannot get the technicians assigned to a ticket
    @Test
    void getTechniciansATicketIsAssignedToByUserWhoDidntCreateTheTicket() throws Exception {
        
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(8L);
        String token = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/tickets/{ticketId}/technicians", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
