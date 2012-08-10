package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import models.UserPlaylistActivity.Type;

import play.data.format.Formats;
import play.db.ebean.Model;

@Entity
@Table(name="playlist_song_ratings")
public class PlaylistSongRating extends AppModel {
	
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date createdDate;	
	
	@ManyToOne
	private User user;
	
	public enum Type
	{
		like,
		dislike;

		private static final Type[] copyOfValues = values();
		
		public static Type forName(String name) {
	        for (Type value : copyOfValues) {
	            if (value.name().equals(name)) {
	                return value;
	            }
	        }
	        return null;
	    }			
		
	};
	
	@Enumerated(EnumType.STRING)
	private Type type;
	
	@ManyToOne
	private PlaylistSong playlistSong;

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public PlaylistSong getPlaylistSong() {
		return playlistSong;
	}

	public void setPlaylistSong(PlaylistSong playlistSong) {
		this.playlistSong = playlistSong;
	}

}
