package techit.rest.controller;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import techit.model.User;
import techit.model.dao.UserDao;
import techit.rest.error.RestException;

@RestController
public class LoginController {
	  @Autowired
	    private UserDao userDao;
	  
	    @RequestMapping(value = "/login", method = RequestMethod.POST)
	    public String login(@RequestBody User user ) {
	    	 if (user.getUsername() == null || user.getPassword() == null) {
	              throw new RestException(400, "Missing username and/or password.");
	          }
	        String result = userDao.login(user.getUsername(), user.getPassword());
	        if (result == null) {
	            throw new RestException(400, "Invalid username and/or password.");
	        } else {
	            return result;
	        }
	    }
}
