package models;

import java.sql.Date;
import javax.persistence.*;
import javax.validation.Constraint;

import com.avaje.ebean.annotation.EnumValue;

import play.db.ebean.*;
import play.db.ebean.Model.Finder;
import play.data.format.*;
import play.data.validation.*;


@Entity 
@Table(name="users")
public class User extends Model {
	
	@Id
	public Long id;
	
	@Constraints.Required
	public String username;
	
	@Constraints.Required
	public String email;
	
	@Constraints.Required
	public String password;

	public Date registeredDate;

	public Date lastLoginDate;
	
	
	public enum UserType
	{
		@EnumValue("admin")
		admin,
		
		@EnumValue("user")
		user; 
	}
	
	@Constraints.Required
	public UserType type;
	
	public int imageStorageObjectId;
	

	public static Model.Finder<Long,User> find = new Finder<Long, User>(Long.class, User.class);
	
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
