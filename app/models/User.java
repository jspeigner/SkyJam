package models;

import java.util.Date;
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
public class User extends AppModel {
	

	@Length(max=30)
	@Constraints.Required
	public String username;
	
	@Length(max=40)
	@Constraints.Required
	@Email
	public String email;
	
	@Constraints.Required
	public String password;

	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date registeredDate;

	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date lastLoginDate;
	
	
	public enum UserType
	{
		admin,
		user 
	}
	
	@Constraints.Required
	@Enumerated(EnumType.STRING)
	public UserType type;
	

	public static Model.Finder<Integer,User> find = new Finder<Integer, User>(Integer.class, User.class);
	
	public static User authenticate(String email, String password) {
        return find.where()
            .eq("email", email)
            .eq("password", password)
            .findUnique();
    }	
	
	

    public String toString() {
        return "User( #" + id + ")";
    }	
	

}
