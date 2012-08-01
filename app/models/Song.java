package models;

import javax.persistence.*;

import com.avaje.ebean.validation.Length;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="songs")
public class Song extends Model {
	
	@Id
	public Integer id;
	

	@Length(max=300)
	public String name;
	
	@ManyToOne
	public Album album;
	
	public String keywords;
	
	public Integer duration;
	
	@ManyToOne
	public StorageObject storageObject;
	
	public enum Status
	{
		visible,
		hidden
	};
	
	@Enumerated(EnumType.STRING)
	public Status status;
	
	public static Model.Finder<Integer,Song> find = new Finder<Integer, Song>(Integer.class, Song.class);
	
	public static Song getByNameAndAlbumId(String name, Integer albumId) {
		 
		return Song.find.where().eq("name", name).eq("album_id", albumId).findUnique();
	}	
	
}
