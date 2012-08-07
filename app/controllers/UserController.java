package controllers;

import java.util.Date;

import javax.persistence.PersistenceException;

import play.*;
import play.mvc.*;
import play.data.*;
import play.data.validation.ValidationError;

import models.*;
import tyrex.resource.jca.dd.DDAuthMechanism;
import views.html.*;


public class UserController extends Controller {

	public static final String AUTH_USER_SESSION_ID = "User.id";	

	
    // Authentication
    
    public static class Login {
        
        public String email;
        public String password;
        
        public String validate() {
            if(User.authenticate(email, password) == null) {
                return "Invalid user or password";
            }
            return null;
        }
        
    }



    /**
     * Login page.
     */
    public static Result login() {
        return ok(
            User_login.render(form(Login.class))
        );
    }
    
    /**
     * Handle login form submission.
     */
    public static Result authenticate() {
    	
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        
        if(loginForm.hasErrors()) 
        {
            return badRequest(User_login.render(loginForm));
        }
        else 
        {
        	
            // session("email", loginForm.get().email);
            
            return redirect(
                routes.ApplicationController.index()
            );
        }
    }
    
    public static Result homepage()
    {
    	return null;
    }

    /**
     * Logout and clean the session.
     */
    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(
            routes.UserController.login()
        );
    }
    
    public static Result homepageRegister()
    {
    	Form<User> userForm = form(User.class);
    	
    	return ok(User_homepageRegister.render(userForm));
    }
    
    public static Result homepageRegisterSubmit()
    {
    	Form<User> userForm = form(User.class).bindFromRequest("email");
    	
    	
    	if(userForm.hasErrors())
    	{
    		
    		return badRequest(User_homepageRegister.render(userForm));
    	}
    	else
    	{

    		User user = userForm.get();
    		user.setPassword("empty");
    		user.setUsername( user.getEmail() );
    		user.setRegisteredDate(new Date());
    		user.setType(User.UserType.user);
    		
    		try
    		{
    		
	    		user.save();
    		
	    		
    		}
    		catch(PersistenceException p)
    		{
    			// email is not unique
    			userForm.reject( new ValidationError( "email", "Email is already used", null) );
    			
    			return badRequest(User_homepageRegister.render(userForm));
    			
    		}
    		
    		setAuthUser(user);
    		
    		return redirect( routes.UserController.homepageRegisterSuccess() );
    	}
    }
    
    public static Result homepageRegisterSuccess()
    {
    	User user = getAuthUser();
    	
    	return user != null ?  ok(User_homepageRegisterSuccess.render(user)) : badRequest("User not found");
    	
    }
    
    protected static User getAuthUser()
    {
    	User user = null;
    	
    	try
    	{
    		user = User.find.byId( Integer.parseInt( session(AUTH_USER_SESSION_ID) ) );
    		
    	}
    	catch( Exception e)
    	{
    		user = null;
    	}
    	
    	return user;
    }
    
    protected static boolean setAuthUser(User user)
    {
    	if( user != null )
    	{
    		try
    		{
    			session(AUTH_USER_SESSION_ID, user.getId().toString());
    			
    			return true;
    		}
    		catch(Exception e)
    		{
    			
    		}
    		
    	}
    	
    	return false;
    }
	
}
