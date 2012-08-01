package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;

import play.data.format.Formats;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="storage_objects")
public class StorageObject extends Model {
	
	@Id
	public Integer id;
	
	@Length(max=255)
	public String name;
	
	public Long filesize;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdDate;
	
	@ManyToOne
	public Bucket bucket;
	

	public static Model.Finder<Integer,StorageObject> find = new Finder<Integer, StorageObject>(Integer.class, StorageObject.class);
	
	public static StorageObject getByName(String name)
	{
		return StorageObject.find.where().eq("name", name).findUnique();
	}


	


}
