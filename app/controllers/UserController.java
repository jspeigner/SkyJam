package controllers;

import global.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;
import be.objectify.deadbolt.actions.Restrict;

import com.avaje.ebean.Ebean;

import controllers.components.Facebook;
import controllers.components.ForceHttps;

import play.*;
import play.libs.Akka;

import play.libs.F.Promise;
import play.libs.F.Function;

import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.api.libs.Crypto;
import play.data.*;
import play.data.validation.*;
import play.data.validation.Constraints.*;
import java.util.concurrent.Callable;
import models.*;


@With(ForceHttps.class)
public class UserController extends BaseController {

	public static final String AUTH_USER_COOKIE_ID = "User.id";	
	public static final int AUTH_USER_COOKIE_LIFETIME = 7*24*3600;
	
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
        	User user = loginForm.get().getUser();
        	setAuthUser( user );
        	
        	user.setLastLoginDate(new Date());
        	user.save();
            
            return  pjaxRedirect(  routes.ApplicationController.index() );
        }
    }
    
    public static Result homepage()
    {
    	return null;
    }

    /**
     * Logout and clean the User.
     */
    public static Result logout() {
    	
    	
        UserController.setAuthUser(null);
        
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
    		user.roles = new ArrayList<UserRole>();
    		user.roles.add(UserRole.findByName("user"));
    		
    		try
    		{
	    		user.save();
	    		Ebean.saveManyToManyAssociations(user,"roles");
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
    		
    		userForm = form(User.class).bind(signedRequestData, "username","email","password","facebookUserId");
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
    		User user = userForm.get();
    		
    		user.setRegisteredDate(new Date());
    		user.setLastLoginDate(null);
    		user.roles = new ArrayList<UserRole>();
    		user.roles.add(UserRole.findByName("user"));
    		
    		try
    		{
	    		user.save();
	    		Ebean.saveManyToManyAssociations(user,"roles");
	    		
	    		if( user.getFacebookUserId() != null )
	    		{
	    			// populate the image from FB	    			
	    			user.updateImageFromURL( Facebook.getUserImageUrl(user.getFacebookUserId(), Facebook.UserImageType.LARGE) );
	    		}
	    		
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
    	User u = User.find.byId(id);
    	
    	if( u == null )
    	{
    		return badRequest("User not found");
    	}
    	
    	List<Playlist> userPlaylists = Playlist.find.where().eq("user", u).eq("status", Playlist.Status.Public ).orderBy("createdDate DESC").setMaxRows(10).findList();
    	List<UserSavedPlaylist> savedPlaylists = UserSavedPlaylist.find.where().eq("user", u).orderBy("createdDate DESC").setMaxRows(50).findList();
    	
    	return ok(views.html.User.publicProfile.render(u, userPlaylists, savedPlaylists));
    }
    
    @Restrict("user")
    public static Result profile()
    {
    	User user = getAuthUser();
    	Form<User> userForm = form(User.class).fill( user );
    	
    	List<Playlist> recentPlaylists = Playlist.getRecentPlaylists(user, 5);
    	
    	List<UserSavedPlaylist> savedPlaylists = UserSavedPlaylist.find.where().eq("user",user).orderBy("createdDate DESC").setMaxRows(50).findList();
    	
    	return ok(views.html.User.profile.render(userForm, user, recentPlaylists, savedPlaylists));
    }
    
    @Restrict("user")
    public static Result profileUpdate()
    {
    	User user = getAuthUser();
    	Form<User> userForm = form(User.class).bindFromRequest();
    	DynamicForm formData = form().bindFromRequest();
    	List<Playlist> recentPlaylists = Playlist.getRecentPlaylists(user, 5);
    	List<UserSavedPlaylist> savedPlaylists = UserSavedPlaylist.find.where().eq("user",user).orderBy("createdDate DESC").setMaxRows(50).findList();
    	
    	
    	
    	MultipartFormData body = request().body().asMultipartFormData();
    	FilePart picture = body.getFile("image");
    	
    	if (picture != null) 
    	{
    	    File imageFile = picture.getFile();
    	    
    	    try
    	    {
    	        long filesizeLimit = Play.application().configuration().getInt("application.thumbnail.max_filesize");
    	    	
    	    	if(imageFile.length() > filesizeLimit)
    	    	{
    	    		flash("image_error", "Image size exceeds the "+ Utils.humanReadableByteCount(filesizeLimit, true)+" limit");
    	    	}
    	    	else
    	    	{
	    	        boolean savedSuccessfully = user.updateImage( new FileInputStream(imageFile));
	    	        if(savedSuccessfully){
	    	        	flash("image_success", "Image has been updated successfully");
	    	        } else {
	    	        	flash("image_error", "There was an error changing the image.");
	    	        }
    	    	}
    	        
    	    }
    	    catch (Exception e) 
    	    {
    	    	System.out.print(e);
    	    }
    	    
    	    imageFile.delete();
    	}
    	
    	if( !formData.get("password").isEmpty() || !formData.get("password_reset").isEmpty() )
    	{
    		if( formData.get("password").equals( formData.get("password_reset") ) )
    		{
    			user.setPassword(formData.get("password"));
    			user.save();
    			flash("password_success", "Password has been updated successfully");
    		}
    		else
    		{
    			userForm.reject(new ValidationError("password_reset", "Passwords should match", null));
    		}
    		
    	}
    	
    	return ok(views.html.User.profile.render(userForm, user, recentPlaylists, savedPlaylists));
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
    		
    		
    		Http.Cookie cookie = request().cookies().get( AUTH_USER_COOKIE_ID );
    		String value = cookie.value();
    		String[] valueParts = value.split("-");
    		
    		if( valueParts.length == 2) {
    			
    			String userId = valueParts[1];
    			
    			if( Crypto.sign(userId).equals( valueParts[0] ) ){
    			
    				user = User.find.byId( Integer.parseInt( userId ) );
    			}
    		}
    		
    		
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
    			
    			String userUID = user.getId().toString();
    			
    			String cookieValue = Crypto.sign( userUID )+"-"+userUID;
    			
    			response().setCookie(AUTH_USER_COOKIE_ID, cookieValue, AUTH_USER_COOKIE_LIFETIME);
    			
    			return true;
    		}
    		catch(Exception e)
    		{
    			
    		}
    		
    	}
    	else
    	{
    		// remote the logged user
    		response().discardCookies(AUTH_USER_COOKIE_ID);
    	}
    	
    	
    	return false;
    }
	
}
