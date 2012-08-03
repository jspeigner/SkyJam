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
@Table(name="playlist_ratings")
public class PlaylistRating extends AppModel {
	
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdDate;	
	
	@ManyToOne
	public User user;
	
	public enum Type
	{
		like,
		dislike
	};
	
	@Enumerated(EnumType.STRING)
	public Type type;
	
	@ManyToOne
	public Playlist playlist;

}
