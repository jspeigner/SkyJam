package models;

import global.Global;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

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

	@Length(max=30)
	// @Constraints.Required
	private String username;
	
	@Length(max=40)
	@Constraints.Required
	@Email
	private String email;
	
	private String password;

	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date registeredDate;

	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date lastLoginDate;
	
	
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
	
	public List<ValidationError> validate()
	{
		
		return null;
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
	

}
