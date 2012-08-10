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
public class StorageObject extends AppModel {
	
	public static String AMAZON_S3_URL = "https://s3.amazonaws.com/";
	
	@Length(max=255)
	private String name;
	
	public Long filesize;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdDate;
	
	@ManyToOne
	private Bucket bucket;
	

	public static Model.Finder<Integer,StorageObject> find = new Finder<Integer, StorageObject>(Integer.class, StorageObject.class);
	
	public static StorageObject getByName(String name)
	{
		return StorageObject.find.where().eq("name", name).findUnique();
	}

	public String getUrl()
	{
		Bucket b  = getBucket();
		
		if( ( b != null ) )
		{
			return AMAZON_S3_URL + b.getName() + "/" + getName() ;				
		}
		
		return null;
	}

	public Bucket getBucket() {
		return bucket;
	}

	public void setBucket(Bucket bucket) {
		this.bucket = bucket;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
