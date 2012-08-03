package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.data.format.Formats;
import play.db.ebean.Model;

@Entity
@Table(name="playlist_songs")
public class PlaylistSong extends AppModel {


	@ManyToOne
	public Playlist playlist;
	
	@OneToOne
	public Song song;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdDate;	
	
	public Integer order;
	
	public Integer likesCount;
	
	public Integer dislikesCount;
	
}
