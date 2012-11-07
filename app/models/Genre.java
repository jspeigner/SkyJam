package models;

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import models.MusicCategory.Type;

import com.avaje.ebean.validation.Length;

import play.db.ebean.Model;
import scala.Tuple2;

@Entity
@Table(name="genres")
public class Genre extends AppModel {

	@Length(max=100)
	private String name;

	public static Model.Finder<Integer,Genre> find = new Finder<Integer, Genre>(Integer.class, Genre.class);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
}
