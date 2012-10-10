package controllers;

import global.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import be.objectify.deadbolt.actions.Restrict;

import com.avaje.ebean.Ebean;

import controllers.components.Facebook;

import play.*;

import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.api.libs.Crypto;
import play.data.*;
import play.data.validation.*;
import play.data.validation.Constraints.*;
import views.html.Album.view;
import models.*;


// TODO : implement correct CORS and AJAX calls to enable the secure browsing
// @With(ForceHttps.class)
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
    
    
    @Entity
    public static class InvitationUser {

    	@Constraints.MaxLength(value=40)
    	@Constraints.MinLength(value=1, message="Minimum Lenght")
    	@Email(message="Valid email is required")
    	public String email;
  	
    	
        public String validate() {
        	
        	if(email.length() < 1){
        		return "Valid email is required";
        	}
        	
            return null;
        }    	
    	
    }
    
    @Entity
    public static class ForgotPassword {

    	@Constraints.MaxLength(value=40)
    	@Constraints.MinLength(value=1, message="Minimum Lenght")
    	@Email(message="Valid email is required")
		public String email;
  	
    	
        public String validate() {
        	
       	
        	if( getEmail().length() < 1 ){
        		return "Valid email is required";
        	} else if( !userAccountFound() ){
        		return "Your user record was not found";
        	}
        	
            return null;
        }    
        
        protected boolean userAccountFound(){
        	int cnt = User.find.where().eq("email", getEmail()).findRowCount(); 
        	return  cnt > 0;
        }

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
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
        
        if(loginForm.hasErrors()) 
        {
            return ok(views.html.User.login.render(loginForm));
        }
        else 
        {
        	User user = loginForm.get().getUser();
        	setAuthUser( user );
        	
        	user.setLastLoginDate(new Date());
        	user.update();
            
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
    	
    	if( getAuthUser() != null ){
    		return pjaxRedirect( routes.ApplicationController.index() );
    	}
    	
    	Form<InvitationUser> userForm = form(InvitationUser.class);
    	
    	return ok(views.html.User.homepageRegister.render(userForm));
    }
    
    public static Result homepageRegisterSubmit()
    {
    	Form<InvitationUser> userForm = form(InvitationUser.class).bindFromRequest("email");
    	
    	if(userForm.hasErrors())
    	{
    		
    		return ok (views.html.User.homepageRegister.render(userForm));
    	}
    	else
    	{
    		InvitationUser i = userForm.get(); 
    		User user = new User();
    		user.setEmail( i.email );
    		user.setPassword("empty");
    		user.setUsername( user.getEmail() );
    		user.setRegisteredDate(new Date());
    		user.roles = new ArrayList<UserRole>();
    		user.roles.add(UserRole.findByName("awaiting"));
    		
    		try
    		{
	    		user.save();
	    		Ebean.saveManyToManyAssociations(user,"roles");
    		}
    		catch(Exception p)
    		{
    			
    			// email is not unique
    			userForm.reject( new ValidationError( "email", "Email is already used", null) );
    			
    			return ok(views.html.User.homepageRegister.render(userForm));
    			
    		}
    		
    		setAuthUser(user);
    		
    		return pjaxRedirect( routes.UserController.homepageRegisterSuccess() );
    	}
    }
    
    public static Result homepageRegisterSuccess()
    {
    	User user = getAuthUser();
    	
    	return user != null ?  ok(views.html.User.homepageRegisterSuccess.render(user)) : badRequest("User not found");
    }
    
    public static Result register(String invitationCode)
    {
    	Form<User> userForm = form(User.class);
    	userForm.data().put("invitation_code", invitationCode);
    	
    	return ok(views.html.User.register.render(userForm));
    }

    public static Result registerSubmit(String invitationCode)
    {
    	Form<User> userForm;
    	// process the FB signed_request
    	DynamicForm form = form().bindFromRequest();
    	if(invitationCode != ""){
    		form.data().put("data[invitation_code]", invitationCode);
    	}
    	
    	
    	UserInvitationCode invitation = null;
    	
    	if( form.field("signed_request").value() != null ){
    		Map<String,String> signedRequestData = User.getSignedRequestRegisterParams( form.field("signed_request").value() );
    		
    		userForm = form(User.class).bind(signedRequestData, "username","email","password","facebookUserId");
    	} else {
    		userForm = form(User.class).bindFromRequest("username","email","password");
    	}

    	
    	if( ( form.field("invitation_code").value() != null ) ){
    		
    		String formInvitationCode = form.field("invitation_code").value();
    		userForm.data().put("invitation_code", formInvitationCode );
    		
    		invitation = UserInvitationCode.isAvailable( formInvitationCode );
    		
    		if( invitation == null ){
    			// invitation code is invalid
    			userForm.reject(new ValidationError( "invitation_code", "Invitation code is wrong", null) );
    		}
    	} else {
    		// invitation code field is missing
    		userForm.reject(new ValidationError( "invitation_code", "Invitation code is wrong", null) );
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
	    		
	    		invitation.markUsedByUser(user);
	    		
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
    
    public static Result registerWithFacebook(String invitationCode)
    {
    	String facebookAppId = Play.application().configuration().getString("application.facebook_app_id");
    	return ok(views.html.User.registerWithFacebook.render(facebookAppId, invitationCode));
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
	    	        	flash("image_success", "Your photo was updated successfully");
	    	        } else {
	    	        	flash("image_error", "There was an error changing the image.");
	    	        }
    	    	}
    	        
    	    }
    	    catch (Exception e) 
    	    {
    	    }
    	    
    	    imageFile.delete();
    	}
    	
    	if( validateUserPassword(userForm)){
			user.setPassword(formData.get("password_reset"));
			user.save();

			flash("password_success", "Your password has been successfully updated");    		
    	}


    	
    	return ok(views.html.User.profile.render(userForm, user, recentPlaylists, savedPlaylists));
    }
    
    protected static boolean validateUserPassword( Form<User> userForm){
    	
    	
    	DynamicForm formData = form().bindFromRequest();  
    	
    	if( ( formData.get("password_reset").length() > 0 ) || ( formData.get("password_repeat").length() > 0 ) ){
        	
	    	if ( formData.get("password_reset").length() < User.getMinPasswordLength() ){
	    		
	    		userForm.reject(new ValidationError("password_reset", "Minimum length is " + User.getMinPasswordLength(), null));
	   			
	    	}
	    	else if( !formData.get("password_repeat").equals( formData.get("password_reset") ) ){ 
	    			
	    		userForm.reject(new ValidationError("password_repeat", "Passwords should match", null));
	    			
	    	} else {
	    			
	    		return true;
	    	}

    	}    
    	
    	return false;
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
    
    public static Result forgotPassword(){
    	
    	Form<ForgotPassword> form = form(ForgotPassword.class);
    	
    	return ok( views.html.User.forgotPassword.render(form) );
    }
    
    public static Result forgotPasswordSubmit(){
    	
    	Form<ForgotPassword> form = form(ForgotPassword.class).bindFromRequest("email");
    	
    	if(form.hasErrors()){
    	
    		return ok( views.html.User.forgotPassword.render(form) );
    		
    	} else {
    		
    		ForgotPassword fp = form.get();
    		User user = User.find.where().eq("email", fp.getEmail()).findUnique();
    		
    		UserPasswordReset upr = new UserPasswordReset();
    		upr.createNew(user);
    		
    		email( fp.getEmail() , "Your password reset instructions", views.html.Email.text.passwordResetInstructions.render(user, upr).toString() );
    		
    		
    		return ok( views.html.User.forgotPasswordEmailSent.render( user  ) );
    	}
    	
    	
    }
    
    
    public static Result resetPassword(String resetCode){
    	
    	Form<User> userForm = form(User.class).bindFromRequest();
    	DynamicForm formData = form().bindFromRequest();
    	UserPasswordReset u = UserPasswordReset.findByCode( resetCode );
    	
    	if( ( request().method().equals("POST") ) || ( request().method().equals("PUT") ) ){
    		
    		if( ( formData.get("password_reset").length() == 0 ) ){
    		
    			userForm.reject(new ValidationError("password_reset", "Minimum length is " + User.getMinPasswordLength(), null));
    			
    		} else if ( ( formData.get("password_repeat").length() == 0 ) ){
    			
    			userForm.reject(new ValidationError("password_repeat", "Minimum length is " + User.getMinPasswordLength(), null));
    			
    		} else if( validateUserPassword(userForm)){
	    		
	    		User user = u.getUser();
				user.setPassword(formData.get("password_reset"));
				user.save();
				u.setAsUsed();
	
				flash("password_success", "Your password has been successfully updated");    		
	    	}
    	}
    	
    	return ok(views.html.User.resetPassword.render(u, userForm));
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
