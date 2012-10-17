package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import be.objectify.deadbolt.models.Role;

import java.util.List;

@Entity
@Table(name="user_roles")
public class UserRole extends AppModel implements Role
{
	public static final  String ROLE_ADMIN = "admin";
	public static final String ROLE_USER = "user";
	
	
    public String name;

    public static final Finder<Integer, UserRole> find = new Finder<Integer, UserRole>(Integer.class, UserRole.class);


    public String getName()
    {
        return name;
    }

    public static UserRole findByName(String roleName)
    {
        return find.where().eq("name", roleName).findUnique();
    }

	@Override
	public String getRoleName() {
		return name;
	}
}