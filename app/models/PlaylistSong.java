package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.data.format.Formats;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="playlist_songs")
public class PlaylistSong extends AppModel {


	@ManyToOne
	public Playlist playlist;
	
	@OneToOne
	public Song song;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdDate;	
	
	public Integer position;
	
	public Integer likesCount;
	
	public Integer dislikesCount;
	
	public static Model.Finder<Integer,Song> find = new Finder<Integer, Song>(Integer.class, Song.class);
	
}
