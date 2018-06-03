package techit.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import techit.model.dao.UserDao;
@Test(groups = "LoginController")
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:techit-servlet.xml"})
public class LoginControllerTest  extends AbstractTransactionalTestNGSpringContextTests {
	  private MockMvc mockMvc;

	    @Autowired
	    private WebApplicationContext wac;

	    @Autowired
	    UserDao userDao;
	    
	    @BeforeClass
	    void setup() {
	        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	    }

	
	@Test
    void loginUserWithEmptyRequestBody() throws Exception {
        //Accessing this endpoint with an no parameters should give an error message
        mockMvc.perform(post("/login"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void loginUserWithWrongParameters() throws Exception {
        //Accessing this endpoint with the wrong parameters should give an error message
        mockMvc.perform(post("/login")
        		.contentType(MediaType.APPLICATION_JSON)
        		.content("{\"username\": \"ADDDMINN\",\"password\": \"ADDDMINN\"}"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void loginUserWithRightParameters() throws Exception {
        //Accessing this endpoint with the right parameters should return a JWT
        mockMvc.perform(post("/login")
        		.contentType(MediaType.APPLICATION_JSON)
        		.content("{\"username\": \"ADMIN\",\"password\": \"ADMIN\"}"))
                .andExpect(status().isOk());
    }
}
