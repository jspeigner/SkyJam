package models;

import javax.persistence.*;

import com.avaje.ebean.validation.Length;
import java.util.Set;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="songs")
public class Song extends AppModel {
	
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
	
	
	@ManyToMany
	@JoinTable(name = "songs_music_categories", joinColumns = { @JoinColumn(name="song_id") }, inverseJoinColumns = { @JoinColumn(name="music_category_id") } )
	public Set<MusicCategory> musicCategories;
	
	public static Model.Finder<Integer,Song> find = new Finder<Integer, Song>(Integer.class, Song.class);
	
	public static Song getByNameAndAlbumId(String name, Integer albumId) {
		 
		return Song.find.where().eq("name", name).eq("album_id", albumId).findUnique();
	}	
	
}
