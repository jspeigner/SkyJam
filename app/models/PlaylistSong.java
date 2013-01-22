package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import play.data.format.Formats;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="playlist_songs")
public class PlaylistSong extends AppModel {


	@ManyToOne
	private Playlist playlist;
	
	@ManyToOne
	private Song song;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date createdDate;	
	
	private Integer position;
	
	private Integer likesCount;
	
	private Integer dislikesCount;
	
	@OneToMany
	@OrderBy("created_date DESC")
	private List<UserPlaylistActivity> userPlaylistActivities;

	@OneToMany(cascade=CascadeType.ALL)
	private List<PlaylistSongRating> playlistSongRatings;
	
	public static Model.Finder<Integer,PlaylistSong> find = new Finder<Integer, PlaylistSong>(Integer.class, PlaylistSong.class);

	public Song getSong() {
		return song;
	}

	public void setSong(Song song) {
		this.song = song;
	}

	public Playlist getPlaylist() {
		return playlist;
	}

	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public Integer getLikesCount() {
		return likesCount;
	}

	public Integer setLikesCount(Integer likesCount) {
		this.likesCount = likesCount;
		return likesCount;
	}

	public Integer getDislikesCount() {
		return dislikesCount;
	}

	public void setDislikesCount(Integer dislikesCount) {
		this.dislikesCount = dislikesCount;
	}
	
	private boolean isDislikedByUser(User user){
		// TODO optimize / cache this call
		return PlaylistSongRating.find.where().eq("playlistSong", this).eq("user", user).eq("type", PlaylistSongRating.Type.dislike).findRowCount() > 0;
	}

	private boolean islikedByUser(User user){
		// TODO optimize / cache this call
		return PlaylistSongRating.find.where().eq("playlistSong", this).eq("user", user).eq("type", PlaylistSongRating.Type.like).findRowCount() > 0;
	}	
	
	public List<UserPlaylistActivity> getUserPlaylistActivities() {
		return userPlaylistActivities;
	}

	public void setUserPlaylistActivities(List<UserPlaylistActivity> userPlaylistActivities) {
		this.userPlaylistActivities = userPlaylistActivities;
	}

	
}
