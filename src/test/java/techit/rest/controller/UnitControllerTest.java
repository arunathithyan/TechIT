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
import techit.model.dao.UnitDao;
import techit.model.dao.UserDao;

@Test(groups = "UnitController")
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:techit-servlet.xml"})
public class UnitControllerTest extends AbstractTransactionalTestNGSpringContextTests {

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
    void getAllUnitsByAdmin() throws Exception {
        //Only the admin can get details of all the units
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

        mockMvc.perform(get("/units")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    //Any user can get the Units
    @Test
    void getAllUnitsByAnyOtherPerson() throws Exception {
       
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

        mockMvc.perform(get("/units")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    //Only the admin can create a new unit
    @Test
    void createNewUnitByAdmin() throws Exception {
    
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
        jsonObject.put("name", "new Unit");
        jsonObject.put("email", "new email");
        jsonObject.put("location", "new location");

        mockMvc.perform(post("/units")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().isOk());
    }
  //No other person can create a unit
    @Test
    void createUnitByOtherPerson() throws Exception {
        
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
        jsonObject.put("name", "new Unit");
        jsonObject.put("email", "new email");
        jsonObject.put("location", "new location");

        mockMvc.perform(post("/units")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toJSONString()))
                .andExpect(status().is4xxClientError());
    }
    //The admin can get the technicians in a unit
    @Test
    void getTechniciansInAUnitByAdmin() throws Exception {
       
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

        mockMvc.perform(get("/units/{id}/technicians", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getTechniciansInAUnitByUser() throws Exception {
        //A user or technician can not access this information
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

        mockMvc.perform(get("/units/{id}/technicians", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getTechniciansInAUnitBySupervisorOfThisUnit() throws Exception {
        //A supervisor of the unit can access this information
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

        mockMvc.perform(get("/units/{id}/technicians", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getTechniciansInAUnitBySupervisorOfOtherUnit() throws Exception {
        //Supervisors of other units cannot get all tehnicians in a unit
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

        mockMvc.perform(get("/units/{id}/technicians", 2L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
    //The admin can get all tickets submitted 
    @Test
    void getTicketsByAdmin() throws Exception {
       
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

        mockMvc.perform(get("/units/{id}/tickets", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getTicketsInAUnitBySupervisorOfUnit() throws Exception {
        //The supervisor of a unit can get tickets submitted to a unit
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

        mockMvc.perform(get("/units/{id}/tickets", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getTicketsInAUnitByOtherUser() throws Exception {
        //Users and technicians cannot get tickets submitted to a unit
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

        mockMvc.perform(get("/units/{id}/tickets", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getTicketsInAUnitByOtherSupervisor() throws Exception {
        //Supervisors of other units cannot get tickets submitted to a unit
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

        mockMvc.perform(get("/units/{id}/tickets", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
