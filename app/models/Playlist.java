package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;

import play.data.format.Formats;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="playlists")
public class Playlist extends AppModel {

	
	@Length(max=200)
	public String name;
	
	public enum Status
	{
		
		Public,
		Private,
		Draft
	}
	
	@Enumerated(EnumType.STRING)
	public Status status;
	
	public String description;
	
	@ManyToOne
	public User user;
	
	@ManyToOne
	public Genre genre;
	
	@ManyToOne
	public MusicCategory musicCategory;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdDate;	
	
	
	public static Model.Finder<Integer,Playlist> find = new Finder<Integer, Playlist>(Integer.class, Playlist.class);
	
	
	public static int findCountByMusicCategoryId(Integer musicCategoryId)
	{
		return find.where().eq("music_category_id", musicCategoryId).findRowCount();
	}
}
