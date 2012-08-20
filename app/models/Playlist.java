package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.validation.Length;

import play.data.format.Formats;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="playlists")
public class Playlist extends AppModel {

	
	@Length(max=200)
	private String name;
	
	public enum Status
	{
		
		Public,
		Private,
		Draft
	}
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	private String description;
	
	@ManyToOne
	private User user;
	
	@ManyToOne
	private Genre genre;
	
	@ManyToMany
	public List<MusicCategory> musicCategories;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date createdDate;	
	
	@OneToMany(targetEntity=PlaylistSong.class)
	@OrderBy("position ASC")
	private List<PlaylistSong> playlistSongs;
	
	
	
	
	public static Model.Finder<Integer,Playlist> find = new Finder<Integer, Playlist>(Integer.class, Playlist.class);
	
	
	public static int findCountByMusicCategoryId(Integer musicCategoryId)
	{
		return find.where().eq("musicCategories", MusicCategory.find.byId(musicCategoryId) ).findRowCount();
	}
	
	public List<PlaylistSong> getPlaylistSongs()
	{
		return getPlaylistSongs(-1);
	}
	
	public List<PlaylistSong> getPlaylistSongs(int limit)
	{
		return limit <= 0 ? playlistSongs : playlistSongs.subList(0, limit);
	}

	public static List<Playlist> searchWideByName(String query, int maxResults) 
	{
		
		String likeQueryString =  "%" + query + "%";
		
		Expression e2 = Expr.and(
				 Expr.eq("status", Status.Public),
				 Expr.or(
						 Expr.ilike("name", likeQueryString), 
						 Expr.or(
								 Expr.ilike("playlistSongs.song.name", likeQueryString), 
								 Expr.or(
										 Expr.ilike("playlistSongs.song.album.name", likeQueryString), 
										 Expr.ilike("playlistSongs.song.album.artist.name", likeQueryString)
								)
								 
						)
				)	
		); 		
		
		return Playlist.find.where(e2).setMaxRows(maxResults).findList();
		
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Genre getGenre() {
		return genre;
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public void setPlaylistSongs(List<PlaylistSong> playlistSongs) {
		this.playlistSongs = playlistSongs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private List<MusicCategory> getMusicCategories() {
		return musicCategories;
	}

	private void setMusicCategories(List<MusicCategory> musicCategories) {
		this.musicCategories = musicCategories;
	}
}
