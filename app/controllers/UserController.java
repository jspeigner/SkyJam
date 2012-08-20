package controllers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.persistence.Entity;
import play.*;
import play.mvc.*;
import play.mvc.Http.Request;
import play.data.*;
import play.data.validation.*;
import play.data.validation.Constraints.*;
import sun.org.mozilla.javascript.ObjToIntMap.Iterator;

import models.*;
import views.html.*;


public class UserController extends AppController {

	public static final String AUTH_USER_SESSION_ID = "User.id";	

	
    // Authentication
	//@Entity 
    public static class Login {
        
    	@Required
    	@Email
        protected String email;
    	
        protected String password;
        
        
        public String getEmail() { return email; }
        
        public void setEmail(String e) { email = e; }

        public String getPassword(){ return password; }
        
        public void setPassword(String p){ password = p; }      
        
        
        public String validate() {
        	
        	
            if(User.authenticate(email, password) == null) {
                return "Invalid email or password";
            }
            return null;
        }
        
        public User getUser()
        {
        	return User.authenticate(email, password);
        }
        
        public String toString()
        {
        	return "Login{ " + "email: \""+email+"\", password:\""+password + "\"}";
        }
    }

    /**
     * Login page.
     */
    public static Result login() {
        return ok( views.html.User.login.render(form(Login.class)) );
    }
    
    /**
     * Handle login form submission.
     */
    public static Result authenticate() {
    	
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        
        // System.out.println("loginForm -> " + loginForm);
        
        if(loginForm.hasErrors()) 
        {
            return ok(views.html.User.login.render(loginForm));
        }
        else 
        {
        	
        	
        	setAuthUser( loginForm.get().getUser() );	
            
            return  pjaxRedirect(  routes.ApplicationController.index() );
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
    	
    	
        session().remove(AUTH_USER_SESSION_ID);
        flash("success", "You've been logged out");
        
        return pjaxRedirect( routes.UserController.login() );
    }
    
    public static Result homepageRegister()
    {
    	Form<User> userForm = form(User.class);
    	
    	return ok(views.html.User.homepageRegister.render(userForm));
    }
    
    public static Result homepageRegisterSubmit()
    {
    	DynamicForm form = form().bindFromRequest();
    	
    	Form<User> userForm = form(User.class).bindFromRequest("email");
    	
    	
    	if(userForm.hasErrors())
    	{
    		
    		return ok (views.html.User.homepageRegister.render(userForm));
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
    			
    			return ok(views.html.User.homepageRegister.render(userForm));
    			
    		}
    		
    		setAuthUser(user);
    		
    		// return redirect( routes.UserController.homepageRegisterSuccess() );
    		
    		return pjaxRedirect( routes.UserController.homepageRegisterSuccess() );
    	}
    }
    
    public static Result homepageRegisterSuccess()
    {
    	User user = getAuthUser();
    	
    	return user != null ?  ok(views.html.User.homepageRegisterSuccess.render(user)) : badRequest("User not found");
    	
    }
    
    public static Result register()
    {
    	Form<User> userForm = form(User.class);
    	
    	return ok(views.html.User.register.render(userForm));
    }

    public static Result registerSubmit()
    {
    	Form<User> userForm;
    	// process the FB signed_request
    	DynamicForm form = form().bindFromRequest();
    	
    	if( form.field("signed_request").value() != null )
    	{
    		Map<String,String> signedRequestData = User.getSignedRequestRegisterParams( form.field("signed_request").value() );
    		userForm = form(User.class).bind(signedRequestData, "username","email","password");
    	}
    	else
    	{
    		userForm = form(User.class).bindFromRequest("username","email","password");
    	}
    	
    	
    	if(userForm.hasErrors())
    	{
    		
    		return ok (views.html.User.register.render(userForm));
    	}
    	else
    	{
    		System.out.println(userForm);    		
    		
    		User user = userForm.get();
    		
    		user.setRegisteredDate(new Date());
    		user.setType(User.UserType.user);
    		user.setLastLoginDate(null);
    		
    		try
    		{
	    		user.save();
    		}
    		catch(Exception e)
    		{
    			// email is not unique
    			userForm.reject("Exception "+e.toString());
    			
    			return ok(views.html.User.register.render(userForm));
    			
    		}
    		
    		
    		setAuthUser(user);
    		
    		
    		// return redirect( routes.UserController.homepageRegisterSuccess() );
    		
    		return pjaxRedirect( routes.UserController.registerSuccess() );
    	}
    }    

    public static Result registerSuccess()
    {
    	User user = getAuthUser();
    	
    	return user != null ?  ok(views.html.User.registerSuccess.render(user)) : badRequest("User not found");
    	
    }
    
    public static Result registerWithFacebook()
    {
    	String facebookAppId = Play.application().configuration().getString("application.facebook_app_id");
    	
    	
    	
    	return ok(views.html.User.registerWithFacebook.render(facebookAppId));
    }
    

    
    public static Result publicProfile(Integer id)
    {
    	return null;
    }
    
    public static Result profile()
    {
    	return null;
    }
    
    public static Result getAuthUserJson()
    {
    	User user = getAuthUser();
    	return ok( views.html.User.getUserJson.render(user) ).as("application/json");
    }
    
    public static User getAuthUser()
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
