package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="buckets")
public class Bucket extends AppModel {


	private static final long serialVersionUID = 1854388095675477416L;


	@Length(max=1024)
	public String name;
	
	
	@ManyToOne
	public AmazonAccount amazonAccount;
	
	public static Model.Finder<Integer,Bucket> find = new Finder<Integer, Bucket>(Integer.class, Bucket.class);
	
	
	
	public static Bucket getByName(String name)
	{
		return Bucket.find.where().eq("name", name).findUnique();
	}		
	
	
}
