package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.format.Formats;
import play.db.ebean.Model;

@Entity
@Table(name="user_playlist_activities")
public class UserPlaylistActivity extends AppModel {


	@ManyToOne
	public User user;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdDate;
	
	public enum Type{
			play,
			pause,
			skip
	};
	
	@Enumerated(EnumType.STRING)
	public Type type;
	
	@ManyToOne
	public PlaylistSong playlistSong;
	
	
	
}
