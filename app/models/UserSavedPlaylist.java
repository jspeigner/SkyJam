package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.data.format.Formats;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity 
@Table(name="user_saved_playlists")
public class UserSavedPlaylist extends AppModel {
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date createdDate;	
	
	@ManyToOne
	private Playlist playlist;
	
	@ManyToOne
	private User user;
	
	
	public static Model.Finder<Integer,UserSavedPlaylist> find = new Finder<Integer, UserSavedPlaylist>(Integer.class, UserSavedPlaylist.class);

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Playlist getPlaylist() {
		return playlist;
	}

	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
