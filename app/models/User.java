package models;

import global.Global;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.persistence.*;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import com.avaje.ebean.annotation.EnumValue;
import com.avaje.ebean.validation.Length;

import play.db.ebean.*;
import play.data.format.*;

import play.data.validation.*;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;



@Entity 
@Table(name="users")
//@models.constraints.Constraints.Unique(id="id",fields={"email","username"})
public class User extends AppModel {

	@Length(max=30,min=1)
	@Constraints.Required
	private String username;
	
	@Length(max=40)
	@Constraints.Required
	@Email
	private String email;
	
	@Length(max=40, min=1)
	private String password;

	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date registeredDate;

	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date lastLoginDate;
	
	
	public enum UserType
	{
		admin,
		user 
	}
	
	
	@Enumerated(EnumType.STRING)
	private UserType type;
	
	@OneToOne
	private StorageObject imageStorageObject;
	
	
	public static Model.Finder<Integer,User> find = new Finder<Integer, User>(Integer.class, User.class);
	
	public static User authenticate(String email, String password) 
	{
		// System.out.println("Auth "+email+" - "+password);
		
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
		
		if( User.find.where().eq("email", email ).findRowCount() > 0 )
		{
			List<ValidationError> emailErrors = new ArrayList<ValidationError>();
			emailErrors.add(new ValidationError( "email", "Email is already taken", null));
			
			validationErrors.put( "email", emailErrors );
		}

		if( User.find.where().eq("username", username ).findRowCount() > 0 )
		{
			List<ValidationError> usernameErrors = new ArrayList<ValidationError>();
			
			usernameErrors.add(new ValidationError( "username", "Username is already taken", null) );
			
			validationErrors.put( "username", usernameErrors );
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
			
			m.update(message.getBytes());
			
			return Global.getHex(m.digest());
			
		} 
		catch (NoSuchAlgorithmException e) 
		{
			// TODO Auto-generated catch block
			return null;
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



	protected String getPassword() {
		return password;
	}


	/**
	 * Set a clean text password
	 * @param password
	 */
	public void setPassword(String password) {
		
		this.password = User.passwordHash( password );
	}



	public Date getRegisteredDate() {
		return registeredDate;
	}



	public void setRegisteredDate(Date registeredDate) {
		this.registeredDate = registeredDate;
	}



	public UserType getType() {
		return type;
	}



	public void setType(UserType type) {
		this.type = type;
	}



	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
	}	
	
	public String getImageUrl()
	{
		if(imageStorageObject!=null)
		{
			return imageStorageObject.getUrl();
		}
		
		return null;
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
		
	    int count = 0;
        
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
            results.put("facebook_user_id", f.user_id);
            
            
            return results;
            
        } 
        catch (Exception e) 
        {
            
            
        }		
        
        
        
        return null;
		
	}
	
	protected static String padBase64(String b64) {
	    String padding = "";
	    // If you are a java developer, *this* is the critical bit.. FB expects
	    // the base64 decode to do this padding for you (as the PHP one
	    // apparently
	    // does...
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
	

}
