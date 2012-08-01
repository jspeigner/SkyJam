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
@Table(name="albums")
public class Album extends Model {

	@Id
	public Integer id;

	@Length(max=200)
	public String name;
	
	@Length(max=20)
	public String image;
	
	// public Integer artistId;
	
	@ManyToOne
	public Artist artist;
	
	@ManyToOne
	public Genre genre;
	
	public Integer copyrightYear;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdDate;
	
	public String keywords;
	
	// public Integer genreId;
	
	public String description;
	
	
	@ManyToOne
	public StorageObject albumArtStorageObject;
	
	// public Integer albumArtStorageObjectId;
	
	public static Model.Finder<Integer,Album> find = new Finder<Integer, Album>(Integer.class, Album.class);
	
	public static Album getByName(String name)
	{
		return Album.find.where().eq("name", name).findUnique();
	}

	public static Album getByNameAndArtistId(String albumName, Integer artistId) {

		return Album.find.where().eq("name", albumName).eq("artist_id", artistId).findUnique();
	}
	
	
}
