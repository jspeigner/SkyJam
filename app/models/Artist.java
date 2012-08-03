package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;

import java.util.List;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

import play.data.validation.*;
import play.data.validation.Constraints.MaxLength;


@Entity
@Table(name="artists")
public class Artist extends AppModel {


	@Constraints.Required
	@Length(max=200)
	public String name;
	
	public String description;
	
	@Length(max=200)
	public String url;
	
	
	
	@OneToMany(targetEntity=Album.class)
	public List<Album> albums;
	
	
	public static Model.Finder<Integer,Artist> find = new Finder<Integer, Artist>(Integer.class, Artist.class);
	
	
	public static Artist getByName(String name)
	{
		return getByName(name, false);
	}
	
	public static Artist getByName(String name, boolean insertOnFail)
	{
		Artist a = Artist.find.where().eq("name", name).findUnique();
		if( a == null)
		{
			if( insertOnFail )
			{
				a = new Artist();
				a.name = name;
				a.description = "";
				a.url = "";
				a.save();
			}
			
			return a;
		}
		else
		{
			return a;
		}
	}
	
	public String getName()
	{
		return name;
	}	
}
