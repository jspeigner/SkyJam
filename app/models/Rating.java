package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.db.ebean.Model;

@Entity
@Table(name="ratings")
public class Rating extends Model {
	
	@Id
	public Integer id;

}
