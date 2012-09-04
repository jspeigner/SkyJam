package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.persistence.Transient;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.validation.Length;

import controllers.UserController;

import play.data.format.Formats;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="playlists")
public class Playlist extends AppModel {

	
	@Length(max=200)
	@Required
	private String name;
	
	public enum Status
	{
		
		Public,
		Private,
		Draft
	}
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Required
	private String description;
	
	@ManyToOne
	private User user;
	
	@ManyToOne
	private Genre genre;
	
	@ManyToMany
	public List<MusicCategory> musicCategories;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date createdDate;
	
	@OneToMany
	@OrderBy("position ASC")
	private List<PlaylistSong> playlistSongs;


	private Integer loadedTimes;
	
	@Transient
	private int minSongsInList = 4;
	
	private static final Pattern songIdFormFieldPattern = Pattern.compile("data\\[songs(\\d+)_song_id\\]");
	
	public static Model.Finder<Integer,Playlist> find = new Finder<Integer, Playlist>(Integer.class, Playlist.class);
	
	
	public static int findCountByMusicCategoryId(Integer musicCategoryId)
	{
		return find.where().eq("musicCategories", MusicCategory.find.byId(musicCategoryId) ).findRowCount();
	}
	
	public static Page<Playlist> pageByCategory(int page, int pageSize, MusicCategory m, String sortBy, String order) {
        return 
            find.where()
                .eq("musicCategories", m)
                .orderBy(sortBy + " " + order)
                .findPagingList(pageSize)
                .getPage(page);
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
	
	public static List<Playlist> getRecentPlaylists(User user, int limit)
	{
		String sql   
	    // = " select t0.id, t0.name AS c1, t0.status  AS  c2, t0.description  AS  c3, t0.created_date  AS  c4, t0.loaded_times  AS  c5, t0.user_id, t0.genre_id  AS  c7 " +
		// = "SELECT t0.id, t0.name, t0.status, t0.description, t0.created_date, t0.loaded_times, t0.user_id, t0.genre_id " +
		= "SELECT t0.id, t0.name, t0.status, t0.description, t0.created_date, t0.loaded_times " +
	    		"from playlists t0 " +
	    		"join playlist_songs u1 on ( u1.playlist_id = t0.id ) " +
	    		"join user_playlist_activities u2 on ( u2.playlist_song_id = u1.id ) " +
	    		"WHERE u2.user_id = "+( user.getId() ) + " " +
	    		"GROUP BY t0.id " +
	    		"ORDER BY u2.created_date DESC ";  
	  
		RawSql rawSql =   
	    RawSqlBuilder  
	        // let ebean parse the SQL so that it can  
	        // add expressions to the WHERE and HAVING   
	        // clauses  
	        .parse(sql)  
	        // map resultSet columns to bean properties  
	        .columnMapping("t0.id",  "id")  
//	        .columnMapping("t0.user_id",  "user")  
	        .columnMapping("t0.name",  "name")	        
	        .columnMapping("t0.status",  "status")
	        .columnMapping("t0.description",  "description")
	        .columnMapping("t0.created_date",  "createdDate")	        
	        .columnMapping("t0.loaded_times",  "loadedTimes")	        
//	        .columnMapping("t0.genre_id",  "genre_id")
	        
	        .create();  
	  
	  
		Query<Playlist> query = Ebean.find(Playlist.class);  
	    query.setRawSql(rawSql)          
	    // add expressions to the WHERE and HAVING clauses  
	      
	    .setMaxRows(limit);
	  
	    return query.findList();  
		
		// return Playlist.find.where().eq("playlistSongs.userPlaylistActivities.user", user).setMaxRows(limit).findSet(); 
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

	public Integer getLoadedTimes() {
		return loadedTimes;
	}

	public void setLoadedTimes(Integer loadedTimes) {
		this.loadedTimes = loadedTimes;
	}

	public static List<Playlist> getSavedPlaylists(User user, int limit) {
		
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isSavedByUser()
	{
		return isSavedByUser(UserController.getAuthUser());
	}
	
	public boolean isSavedByUser(User user)
	{
		return (user==null) || ( UserSavedPlaylist.find.where().eq("user", user).eq("playlist", this ).findRowCount() > 0 ); 
	}

	/*
	public Map<String,List<ValidationError>> validate()
	{
		Map<String,List<ValidationError>> validationErrors = new HashMap<String,List<ValidationError>>(); 
		
		List<ValidationError> globalErrors = new ArrayList<ValidationError>();
		
		if( playlistSongs.size() < minSongsInList )
		{
			globalErrors.add( new ValidationError("", "Please add more songs. At least " + minSongsInList + " songs are required", new ArrayList()) );
						
		}

		if(globalErrors.size()>0){
			validationErrors.put("", globalErrors);
		}
		
		System.out.println("Songs - " + playlistSongs.size());
		
		return validationErrors.size() > 0 ? validationErrors : null;
	}
	*/	
	
	
	
	public static List<PlaylistSong> getSongsFromForm(Map<String, String> data) {
		
		
		 
		
		List<PlaylistSong> result = new ArrayList<PlaylistSong>();
		
		for(Map.Entry<String, String> formEntry : data.entrySet()  ){
			Matcher matcher = songIdFormFieldPattern.matcher(formEntry.getKey());
			
			if( matcher.matches() ){
				
				String songId = matcher.group(1);
				
				PlaylistSong p = new PlaylistSong();
				
				
				
				try {
					Song s = Song.find.byId( Integer.valueOf( formEntry.getValue() ) );
					
					if( s != null){
						
						
						p.setSong( s );
						
						String positionField = "data[playlistSongs"+songId+"_position]";
						int positionFieldValue = data.containsKey(positionField) ? Integer.valueOf(data.get(positionField)) : 0;
						
						String playlistSongIdField = "data[playlistSongs"+songId+"_id]";
						if ( data.containsKey(playlistSongIdField) && ( data.get(playlistSongIdField) != "" ) ){
							p.setId( Integer.valueOf( data.get(playlistSongIdField) ) );
						}
						
						p.setPosition( positionFieldValue );
						result.add( p );
						
					}
					
					// System.out.println(formEntry.getKey() +" = " + formEntry.getValue());
					
				}
				catch(Exception e) {
  
				}
				
				
				
			}
			
		}
		
		return result.size() > 0 ? result : null;
	}

	public void updatePlaylistSongs(List<PlaylistSong> newPlaylistSongs) {
		
		for( PlaylistSong playlistSong: newPlaylistSongs){
			
			if( playlistSong.getId() != null){
				// existing playlist song - update the position
				playlistSong.update( playlistSong.getId() );
			} else {
				
			  playlistSong.setPlaylist(this);
			  playlistSong.setCreatedDate(new Date());
			  playlistSong.setLikesCount(0);
			  playlistSong.setDislikesCount(0);
			  
			  playlistSong.save();				
				
				// new playlist song
			}
			
		}
		
	}
}
