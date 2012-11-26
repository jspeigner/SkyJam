package models;

import global.Global;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.persistence.*;

import models.behavior.ImageMetadata;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.imgscalr.Scalr;

import be.objectify.deadbolt.models.Permission;
import be.objectify.deadbolt.models.Role;
import be.objectify.deadbolt.models.RoleHolder;

import ch.qos.logback.core.Context;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;
import com.avaje.ebean.annotation.EnumValue;
import com.avaje.ebean.validation.Length;
import com.typesafe.config.Config;

import controllers.routes;

import play.Application;
import play.Play;
import play.db.ebean.*;
import play.db.ebean.Model.Finder;
import play.data.format.*;

import play.data.validation.*;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import views.html.tags.player;


@Entity 
@Table(name="users")
public class User extends AppModel implements RoleHolder 
{

	@Length(max=30,min=1)
	@Constraints.Required
	private String username;
	
	@Constraints.MaxLength(value=40)
	@Email(message="Valid email is required")
	private String email;

	@Constraints.MinLength(value=6)
	private String password;

	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date registeredDate;

	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date lastLoginDate;
	
	@OneToOne(cascade=CascadeType.REMOVE)
	private StorageObject imageStorageObject;
	
	@Nullable
	private String facebookUserId;
	
	public static final ImageMetadata imageMetadata = new ImageMetadata(
			64, 
			64, 
			ImageMetadata.IMAGE_TYPE_PNG, 
			"files/user/image/%d.png", 
			"files/user/image/default.png"
	);
	
	@ManyToMany
    public List<UserRole> roles;		
	
	public static Model.Finder<Integer,User> find = new Finder<Integer, User>(Integer.class, User.class);
	
	public static User authenticate(String email, String password){
        return ( ( email != null ) && ( password != null ) ) ?
        	 find.where()
            .eq("email", email)
            .eq("password" ,  User.passwordHash( password ) )
            .findUnique() :
            	
            null;
    }	
	
	public Map<String,List<ValidationError>> validate()
	{
		Map<String,List<ValidationError>> validationErrors = new HashMap<String,List<ValidationError>>(); 
		
		if( User.find.where().eq("email", email ).ne("roles", UserRole.findByName(UserRole.ROLE_AWAITING) ).findRowCount() > 0 )
		{
			List<ValidationError> emailErrors = new ArrayList<ValidationError>();
			emailErrors.add(new ValidationError( "email", "Email is already taken", null));
			
			validationErrors.put( "email", emailErrors );
		}

		if( User.find.where().eq("username", username ).ne("roles", UserRole.findByName(UserRole.ROLE_AWAITING) ).findRowCount() > 0 )
		{
			List<ValidationError> usernameErrors = new ArrayList<ValidationError>();
			
			usernameErrors.add(new ValidationError( "username", "Username is already taken", null) );
			
			validationErrors.put( "username", usernameErrors );
		}
		
		long minPasswordLength = User.getMinPasswordLength();
		if( password.length() < minPasswordLength ){
			
			List<ValidationError> passwordErrors = new ArrayList<ValidationError>();
			
			passwordErrors.add(new ValidationError("password", "Minimum length is " + minPasswordLength , null));
			
			validationErrors.put("password", passwordErrors );
		}		
		

		
		return validationErrors.size() > 0 ? validationErrors : null;
	}
	
	public static String passwordHash(String message)
	{
		
		if( message == null )
		{
			return null;
		}

		MessageDigest m;
		
		try 
		{
			m = MessageDigest.getInstance("SHA-1");
			m.reset();
			
			// System.out.println( "HASH " + message );
			
			String saltedMessage = Play.application().configuration().getString("application.secret") + message;
			
			m.update(saltedMessage.getBytes());
			
			return Global.getHex(m.digest());
			
		} 
		catch (NoSuchAlgorithmException e) 
		{
			return null;
		}

	}

	public static Page<User> getPageWithSearch(int page, int pageSize){
		return getPageWithSearch(page, pageSize, null);
	}
	
	public static Page<User> getPageWithSearch(int page, int pageSize, String term){
    	
    	if( ( term!=null ) && ( !term.isEmpty())){
    		try {  
    		      Integer id = Integer.parseInt( term );  
    		      
    		      return User.find.where().eq("id", id).findPagingList(pageSize).getPage(page);
    		      
    		} catch( Exception e ) {  
    	    		String likeQueryString =  "%" + term.trim() + "%";
    	    		return User.find.where( 
    	    			Expr.or(
    						 Expr.ilike("username", likeQueryString),
    						 Expr.ilike("email", likeQueryString)
    					)
    				).findPagingList(pageSize).getPage(page);
    		}    		
    		

    	} else {
    		return User.find.findPagingList(pageSize).getPage(page);
    	}
	}
	
    public String toString() {
        return "User( #" + id + ")";
    }



	public String getUsername() {
		return username;
	}



	public void setUsername(String username) {
		this.username = username;
	}



	public String getPassword() {
		return password;
	}


	/**
	 * Set a clean text password
	 * @param password
	 */
	public void setPassword(String password) {
		
		// this.password = User.passwordHash( password );
		
		this.password = password;
	}



	public Date getRegisteredDate() {
		return registeredDate;
	}



	public void setRegisteredDate(Date registeredDate) {
		this.registeredDate = registeredDate;
	}




	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
	}	
	
	public String getImageUrl()
	{
		return imageMetadata.getImageUrlFromStorageObject( getImageStorageObject());

	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}	
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class FacebookRegistrationSignedRequestFields
	{
		public String name;
		public String email;
		public String password;
		
		public FacebookRegistrationSignedRequestFields(){};
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class FacebookRegistrationSignedRequest
	{
		public String user_id;
		
		public FacebookRegistrationSignedRequestFields registration;
		
		public FacebookRegistrationSignedRequest(){};
	}
	
	public static Map<String,String> getSignedRequestRegisterParams(String facebookSignedRequest)
	{
		
		String payload = null;
		
        //Retrieve payload (Note: encoded signature is used for internal verification and it is optional)
        payload = facebookSignedRequest.split("[.]", 2)[1];		
		
		
		payload = padBase64( payload.replace('-', '+').replace('_', '/').trim() );
		
		// Decode payload
        try 
        {
        	Base64 base64Decoder = new Base64() ;
        	byte[] decodedPayload = base64Decoder.decode(payload.getBytes());
        	
        	
            // byte[] decodedPayload = Base64.decodeBase64(payload.getBytes());
            
            // payload = new String(decodedPayload, "UTF8");
            
            
            
            ObjectMapper om = new ObjectMapper();
            
            FacebookRegistrationSignedRequest f = om.readValue(decodedPayload, FacebookRegistrationSignedRequest.class);
            
            Map<String,String> results = new HashMap<String,String>();
            
            results.put("username", f.registration.name);
            results.put("password", f.registration.password);
            results.put("email", f.registration.email);
            results.put("facebookUserId", f.user_id);
            
            
            return results;
            
        } 
        catch (Exception e) 
        {
            
            
        }		
        
        
        
        return null;
		
	}
	
	protected static String padBase64(String b64) 
	{
	    String padding = "";
	    /* FB expects the base64 decode to do this padding for you 
	     * ( as the PHP one apparently does... )
	    */
	    switch (b64.length() % 4) {
	    case 0:
	        break;
	    case 1:
	        padding = "===";
	        break;
	    case 2:
	        padding = "==";
	        break;
	    default:
	        padding = "=";
	    }
	    return b64 + padding;

	}


	@Override
	public List<? extends Permission> getPermissions() {
		return null;
	}

	@Override
	public List<UserRole> getRoles() {
		return roles;
	}

	public StorageObject getImageStorageObject() {
		return imageStorageObject;
	}

	public void setImageStorageObject(StorageObject imageStorageObject) {
		this.imageStorageObject = imageStorageObject;
		
		
		
	}	
	
	protected String getImageObjectName()
	{
		return imageMetadata.getFilename(getId());
	}
	
	public boolean updateImageFromURL(String url)
	{
		try
		{
			URL urlStream = new URL(url);
			InputStream  i = urlStream.openStream();
	        updateImage( i );
	        
	        i.close();
	        
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		
		return false;
	}
	
	public boolean updateImage(InputStream sourceImage)
	{
		StorageObject s = StorageObject.updateStorageObjectWithImage(getImageObjectName(), sourceImage, imageMetadata);
		setImageStorageObject(s);
		
		update();
		
		return s != null;
			
	}
	
	public void save(){
		
		if( password != null){
			password = User.passwordHash(password);
		}
		
		super.save();
	}

	public String getFacebookUserId() {
		return facebookUserId;
	}

	public void setFacebookUserId(String facebookUserId) {
		this.facebookUserId = facebookUserId;
	}
	
	public void delete(){
		
		try {
		
			if( getImageStorageObject() != null ){
				getImageStorageObject().delete();
			}
			
			List<PlaylistSongRating> psr = PlaylistSongRating.find.where().eq("user", this).findList();
			if(psr != null){
				Ebean.delete(psr);
			}
			
			List<UserInvitationCode> uic = UserInvitationCode.find.where().eq("user", this).findList();
			if(uic != null){
				Ebean.delete(uic);
			}
			
			List<UserPasswordReset> upr = UserPasswordReset.find.where().eq("user", this).findList();
			if(upr != null){
				Ebean.delete(upr);
			}
			
			List<UserPlaylistActivity> upa = UserPlaylistActivity.find.where().eq("user", this).findList();
			if(upa!=null){
				Ebean.delete(upa);
			}
			
			List<UserSavedPlaylist> usp = UserSavedPlaylist.find.where().eq("user", this).findList();
			if(usp!=null){
				Ebean.delete(usp);
			}
			
			List<Playlist> p = Playlist.find.where().eq("user", this).findList();
			if(p!=null){
				Ebean.delete(p);
			}
			
		} catch (Exception e) {
  
		}
		
		super.delete();
	}

	public static long getMinPasswordLength() {

		try {
			
			
			// Field field = User.class.getField("password");
			Field field = User.class.getDeclaredField("password");
			Constraints.MinLength passwordAnnotation = field.getAnnotation(Constraints.MinLength.class);
			
			
			if( passwordAnnotation != null){
				return passwordAnnotation.value();
			}
			
		} catch (Exception e) {

			
		}		
		
		return 0;
	}
	
	public int getSentInvitationsCount(){
		return UserInvitationCode.find.where().eq("sourceUser", this).findRowCount(); 
	}
	
}
