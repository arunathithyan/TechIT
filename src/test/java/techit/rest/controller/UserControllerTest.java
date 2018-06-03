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
import techit.model.User.Position;
import techit.model.dao.UnitDao;
import techit.model.dao.UserDao;

@Test(groups = "UserController")
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:techit-servlet.xml"})
public class UserControllerTest extends AbstractTransactionalTestNGSpringContextTests {

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

    

    @Test
    void getAllUsersWithoutAuthorization() throws Exception {
        //If anyone other than an admin accesses this endpoint, an error should be returned
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(3L);
        String otherUserToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/users").header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllUsersWithAuthorization() throws Exception {
        //If the admin accesses this endpoint, All users should be returned
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String adminToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void createAUserByUser() throws Exception {
        //A user cannot create a user
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(5L);
        String userToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        User newUser = new User();
        newUser.setDepartment("Computer Science");
        newUser.setEmail("test@email.com");
        newUser.setFirstName("First");
        newUser.setLastName("Last");
        newUser.setPassword("password");
        newUser.setPhoneNumber("123456789");
        newUser.setPosition(User.Position.USER);
        newUser.setUsername("newUser");
        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUser.toString()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createAUserByTechnician() throws Exception {
        //A technician cannot create a user
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(4L);
        String technicianToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        User newUser = new User();
        newUser.setDepartment("Computer Science");
        newUser.setEmail("test@email.com");
        newUser.setFirstName("First");
        newUser.setLastName("Last");
        newUser.setPassword("password");
        newUser.setPhoneNumber("123456789");
        newUser.setPosition(User.Position.USER);
        newUser.setUsername("newUser");

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + technicianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUser.toString()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createAUserBySupervisor() throws Exception {
        //A supervisor cannot create a user
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(3L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("department", "r");
        jsonObject.put("email", "r");
        jsonObject.put("firstName", "r");
        jsonObject.put("lastName", "r");
        jsonObject.put("password", "r");
        jsonObject.put("phoneNumber", "r");
        jsonObject.put("position", "USER");
        jsonObject.put("username", "r");

        /*User newUser = new User();
        newUser.setDepartment("Computer Science");
        newUser.setEmail("test@email.com");
        newUser.setFirstName("First");
        newUser.setLastName("Last");
        newUser.setPassword("password");
        newUser.setPhoneNumber("123456789");
        newUser.setPosition(User.Position.USER);
        newUser.setUsername("newUser");
        newUser.setUnit(unitDao.getUnit(2L));*/

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createATechnicianBySupervisorInDIfferentUnit() throws Exception {
        //A supervisor cannot create a technician in a different unit
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(3L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("department", "r");
        jsonObject.put("email", "r");
        jsonObject.put("firstName", "r");
        jsonObject.put("lastName", "r");
        jsonObject.put("password", "r");
        jsonObject.put("phoneNumber", "r");
        jsonObject.put("position", "TECHNICIAN");
        jsonObject.put("username", "r");
        jsonObject.put("unitId", 1);

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                //.andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createATechnicianBySupervisorInSameUnit() throws Exception {
        //A supervisor can only create a technician in the same unit
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(22L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", "newUser");
        jsonObject.put("password", "password");
        jsonObject.put("department", "Computer Science");
        jsonObject.put("firstName", "firstName");
        jsonObject.put("lastName", "lastName");
        jsonObject.put("phoneNumber", "phoneNumber");
        jsonObject.put("email", "email");
        jsonObject.put("position", "TECHNICIAN");
        jsonObject.put("unitId", 1);

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    void getDetailsByUserOfDifferentUser() throws Exception {
        //A user cannot retrieve details of someone else
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(5L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/users/{id}", 2L)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getDetailsByUserOfSameUser() throws Exception {
        //A user can only retrieve details of himself
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(5L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/users/{id}", 5L)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getDetailsByTechnicianOfDifferentTechnician() throws Exception {
        //A technician cannot retrieve details of someone else
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(4L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/users/{id}", 2L)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getDetailsByTechnicianOfSameTechnician() throws Exception {
        //A technician can only retrieve details of himself
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(4L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/users/{id}", 4L)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getDetailsBySupervisorOfTechnicianInDifferentUnit() throws Exception {
        //A supervisor cannot retrieve details of a technician in a different unit
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(22L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/users/{id}", 6L)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getDetailsBySupervisorOfSameSupervisor() throws Exception {
        //A supervisor can retrieve details of himself
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(22L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/users/{id}", 22L)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getDetailsBySupervisorOfTechnicians() throws Exception {
        //A supervisor can retrieve details of technicians in his unit
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(22L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/users/{id}", 4L)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getDetailsByAdminOfAnyone() throws Exception {
        ////An admin can get details about anyone
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        mockMvc.perform(get("/users/{id}", 7L)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void editDetailsByAdminOfAnyone() throws Exception {
        //Admin can edit anyone
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(2L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("department", "Changed Department");

        mockMvc.perform(put("/users/{id}", 7L)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    void editDetailsByUserForHimself() throws Exception {
        //A user can edit himself
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(5L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("department", "Changed Department");

        mockMvc.perform(put("/users/{id}", 5L)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    void editDetailsByUserForOtherUser() throws Exception {
        //A user cannot edit another user's details
        String secret = "SECRET";
        long EXPIRATION_TIME = 864_000_000; // 10 days

        User user = userDao.getUser(5L);
        String supervisorToken = Jwts.builder()
                .setSubject("user")
                .claim("id", user.getId())
                .claim("type", user.getPosition())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact(); // Sign and build the JWT

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("department", "Changed Department");

        mockMvc.perform(put("/users/{id}", 6L)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void editDetailsByTechnicianForHimself() throws Exception {
        //A technician can edit himself
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
        jsonObject.put("department", "Changed Department");

        mockMvc.perform(put("/users/{id}", 6L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    void editDetailsByTechnicianForOthers() throws Exception {
        //A technician cannot edit other people
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
        jsonObject.put("department", "Changed Department");

        mockMvc.perform(put("/users/{id}", 5L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void editDetailsBySupervisorForHimself() throws Exception {
        //A supervisor can edit himself
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
        jsonObject.put("department", "Changed Department");

        mockMvc.perform(put("/users/{id}", 22L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    void editDetailsBySupervisorForTechnicianInHisUnit() throws Exception {
        //A supervisor can edit technicians in his unit
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
        jsonObject.put("department", "Changed Department");

        mockMvc.perform(put("/users/{id}", 4L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    void editDetailsBySupervisorForTechnicianInAnotherUnit() throws Exception {
        //A supervisor cannot edit technicians in other units
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
        jsonObject.put("department", "Changed Department");

        mockMvc.perform(put("/users/{id}", 6L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getTicketsByAdmin() throws Exception {
        //Admin can get tickets submitted by any user
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

        mockMvc.perform(get("/users/{id}/tickets", 5L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getTicketsByUserForHimself() throws Exception {
        //The user who created the ticket can view the ticket
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

        mockMvc.perform(get("/users/{id}/tickets", 5L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getTicketsByUserForOtherUsers() throws Exception {
        //Users who did not create the ticket cannot view the ticket
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

        mockMvc.perform(get("/users/{id}/tickets", 5L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getTicketsBySupervisorOrTechnicianForUser() throws Exception {
        //The supervisors and technicians cannot see all tickets made by the user
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

        mockMvc.perform(get("/users/{id}/tickets", 5L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getTicketsAssignedToATechnicianByAdmin() throws Exception {
        //The admin can get tickets assigned to a technician
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

        mockMvc.perform(get("/technicians/{id}/tickets", 4L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getTicketsAssignedToATechnicianBySupervisorOfUnit() throws Exception {
        //The supervisor of the unit can get tickets assigned to the technicians in his unit
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

        mockMvc.perform(get("/technicians/{id}/tickets", 4L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getTicketsAssignedToATechnicianBySupervisorOfDifferentUnit() throws Exception {
        //The supervisor of a unit cannot get tickets assigned to technicians in other units
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

        mockMvc.perform(get("/technicians/{id}/tickets", 6L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getTicketsAssignedToATechnicianBySameTechnician() throws Exception {
        //The technician the tickets were assigned to can get the tickets
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

        mockMvc.perform(get("/technicians/{id}/tickets", 4L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getTicketsAssignedToATechnicianByOtherTechnician() throws Exception {
        //Other technicians cannot get the tickets
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

        mockMvc.perform(get("/technicians/{id}/tickets", 6L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getTicketsAssignedToATechnicianByUser() throws Exception {
        //A user cannot get the tickets assigned to a technician
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

        mockMvc.perform(get("/technicians/{id}/tickets", 6L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
