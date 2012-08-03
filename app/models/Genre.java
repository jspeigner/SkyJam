package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;

import play.db.ebean.Model;

@Entity
@Table(name="genres")
public class Genre extends AppModel {

	@Length(max=100)
	public String name;

	public static Model.Finder<Integer,Genre> find = new Finder<Integer, Genre>(Integer.class, Genre.class);
	
	
}
