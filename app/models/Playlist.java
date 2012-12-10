package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Page;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.validation.Length;

import controllers.UserController;
import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;


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
		Draft,
		Deleted
	}
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Required
	private String description;
	
	@ManyToOne
	private User user;
	
	@ManyToOne
	private Genre genre;
	
	@ManyToMany(cascade=CascadeType.ALL)
	private List<MusicCategory> musicCategories;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date createdDate;
	
	@OneToMany
	@OrderBy("position ASC")
	private List<PlaylistSong> playlistSongs;


	private Integer loadedTimes;
	
	@Transient
	public static final int minSongsInList = 5;
	
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
                .eq("status", Playlist.Status.Public)
                .orderBy(sortBy + " " + order)
                .findPagingList(pageSize)
                .getPage(page);
    }	
	
	public List<PlaylistSong> getPlaylistSongs()
	{
		return getPlaylistSongs(-1);
	}
	
	public static Page<Playlist> getPageWithSearch(int page, int pageSize, String term){
		return getPageWithSearch(page, pageSize, term, null);
	}
	
	public static Page<Playlist> getPageWithSearch(int page, int pageSize, String term, Expression additionalConditions){
    	
    	if( ( term!=null ) && ( !term.isEmpty())){
    		try {  
    		      Integer id = Integer.parseInt( term );  
    		      
    		      Expression expr = Expr.eq("id", id);
    		      
  	    		
	  	    		if( additionalConditions != null ){
	  	    			expr = Expr.and( additionalConditions , expr );
	  	    		}    		      
    		      
    		      return Playlist.find.where().eq("id", id).findPagingList(pageSize).getPage(page);
    		      
    		} catch( Exception e ) {
    			
    	    		String likeQueryString =  "%" + term.trim() + "%";
    	    		
    	    		Expression expr = Expr.ilike("name", likeQueryString);	
    	    		
    	    		if( additionalConditions != null ){
    	    			expr = Expr.and( additionalConditions , expr );
    	    		}
    	    		
    	    		return Playlist.find.where( expr ).findPagingList(pageSize).getPage(page);
    		}
    		

    	} else {
    		return ( ( additionalConditions == null ) ? Playlist.find : Playlist.find.where(additionalConditions) ).findPagingList(pageSize).getPage(page);
    	}
	}	
	
	public List<PlaylistSong> getPlaylistSongs(int limit)
	{
		return limit <= 0 ? playlistSongs : playlistSongs.subList(0, limit);
	}

	public static List<Playlist> searchDeepByName(String query, int maxResults) 
	{
		
		String likeQueryString =  "%" + query.trim() + "%";
		
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
		String sql = "SELECT " +
					"t0.id, t0.name, t0.status, t0.description, t0.created_date, t0.loaded_times " +
	    		" FROM " +
	    			" playlists t0 " +
	    			" JOIN playlist_songs u1 on ( u1.playlist_id = t0.id ) " +
	    			" JOIN user_playlist_activities u2 on ( u2.playlist_song_id = u1.id ) " +
	    		" WHERE " +
	    			"( u2.user_id = "+( user.getId() ) +" ) AND " +
	    			"( t0.status = \"" +Playlist.Status.Public+ "\" ) " +
	    		" GROUP BY t0.id " +
	    		" ORDER BY u2.created_date DESC ";
	  
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
	    .setMaxRows(limit);
	  
	    return query.findList();  
 
	}
	
	public static List<Playlist> getPopularPlaylists(Date startDate, Date endDate, String order, int limit){
		
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String sql = 
			"SELECT " +
				"t0.id, t0.name, t0.status, t0.description, t0.created_date, t0.loaded_times " +
    		" FROM " +
    			" playlists t0 " +
    			" JOIN playlist_songs u1 ON ( u1.playlist_id = t0.id ) " +
    			" JOIN user_playlist_activities u2 ON ( u2.playlist_song_id = u1.id ) " +
    		" WHERE " +
    				" ( u2.type = \"play\" ) AND " +
    				" ( t0.status = \"" +Playlist.Status.Public+ "\" ) AND " +
    				" ( u2.created_date <= \"" + sdf.format(startDate) +  "\" ) AND " +
    				" ( u2.created_date >= \"" + sdf.format(endDate)  +  "\" ) " +
    		" GROUP BY t0.id " +
    		( ( ( order != "" ) && ( order != null ) ) ? ( " ORDER BY t0." + order ) : ""  );
  
		RawSql rawSql =   
	    RawSqlBuilder  
	        // let ebean parse the SQL so that it can  
	        // add expressions to the WHERE and HAVING   
	        // clauses  
	        .parse(sql)  
	        // map resultSet columns to bean properties  
	        .columnMapping("t0.id",  "id")  
	//        .columnMapping("t0.user_id",  "user")  
	        .columnMapping("t0.name",  "name")	        
	        .columnMapping("t0.status",  "status")
	        .columnMapping("t0.description",  "description")
	        .columnMapping("t0.created_date",  "createdDate")	        
	        .columnMapping("t0.loaded_times",  "loadedTimes")	        
	//        .columnMapping("t0.genre_id",  "genre_id")
	        .create();  
	  
	  
		Query<Playlist> query = Ebean.find(Playlist.class);  
	    query.setRawSql(rawSql).setMaxRows(limit);
	  
	    return query.findList();  		
		
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

	public List<MusicCategory> getMusicCategories() {
		return musicCategories;
	}
	
	public Set<String> getMusicCategoryIds() {
		List<MusicCategory> list = getMusicCategories();
		Set<String> ids = new HashSet<String>();
		for(MusicCategory category : list){
			ids.add(category.getId().toString());
		}
		
		return ids;
	}

	public void setMusicCategories(List<MusicCategory> musicCategories) {
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
		return ( user == null ) || ( UserSavedPlaylist.find.where().eq("user", user).eq("playlist", this ).findRowCount() > 0 ); 
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

	public boolean updatePlaylistSongs(List<PlaylistSong> newPlaylistSongs) {
		
		if( newPlaylistSongs == null ){
			return false;
		}
		
		Map<Integer, Integer> initialPlaylistSongIds = new HashMap<Integer, Integer>();
		
		for(PlaylistSong p : getPlaylistSongs()){
			initialPlaylistSongIds.put( p.getId(), p.getId());
		}
			
		
		
		
		for( PlaylistSong playlistSong: newPlaylistSongs){
			
			if( playlistSong.getId() != null){
				// existing playlist song - update the position
				playlistSong.update( playlistSong.getId() );
				
				initialPlaylistSongIds.remove(  playlistSong.getId() );
				
			} else {
				
			  playlistSong.setPlaylist(this);
			  playlistSong.setCreatedDate(new Date());
			  playlistSong.setLikesCount(0);
			  playlistSong.setDislikesCount(0);
			  
			  playlistSong.save();				
				
				// new playlist song
			}
			
		}
		
		// remove old playlist songs
		if( initialPlaylistSongIds.size() > 0 ){
			for( Integer key : initialPlaylistSongIds.keySet() ){
				PlaylistSong p = PlaylistSong.find.byId(key);
				if( p!= null){
					p.delete();
				}
			}
		}
		
		return true;
		
	}
	
	public boolean setActivity(MusicCategory activity){
		
		List<Integer> itemsToRemove = new ArrayList<Integer>();
		
		List<MusicCategory> list = getMusicCategories();
		
		for(int i=0; i < list.size(); i++  ){
			if( list.get(i).getType() == MusicCategory.Type.activity ){
				itemsToRemove.add(i);
			}
			
		}
		
		for(int j=0; j<itemsToRemove.size(); j++){
			list.remove( itemsToRemove.get( j ).intValue() );
		}
		
		
		
		list.add(activity);
		
		saveManyToManyAssociations("musicCategories" );
		
		return true;
	}
	
	public void saveMusicCategories( List<MusicCategory> musicCategories){
		
	}
	
	public boolean setActivity(String activityId){
		if( activityId != null){
			try{
				Integer activityIdInt = Integer.valueOf(activityId);
				
				MusicCategory activity = MusicCategory.find.byId(activityIdInt);
				if( activity != null){
					return setActivity(activity);
				}
				
			} catch(Exception e) {
  
			}
		}
		
		return false;
	}
	
	/**
	 * Read the User owned playlist
	 * @param playlistId
	 * @param user
	 * @return
	 */
	public static Playlist getUserPlaylist(Integer playlistId, User user) {
		return Playlist.find.where().eq("id", playlistId).eq("user", user ).ne("status", Playlist.Status.Deleted).findUnique();
	}	
	
	public MusicCategory getActivity(){
		
		List<MusicCategory> list = getMusicCategories();
		
		for(int i=0; i < list.size(); i++  ){
			if( list.get(i).getType() == MusicCategory.Type.activity ){
				return list.get(i);
			}
		}
		
		return null;
	}
	
	public boolean isAllowedToPublish(){
		
		int playlistSongsTotal = getPlaylistSongs().size();
		
		if( playlistSongsTotal < minSongsInList ){
			
			return false;
		}
		
		return true;
	}

	public static Page<Playlist> getUserPlaylistsPage(User user, Integer page, Integer pageSize) {
		
		return Playlist.find.where().eq("user", user).ne("status", Playlist.Status.Deleted ).orderBy("createdDate DESC").findPagingList(pageSize).getPage(page);
	}

	public void updateCategoriesFromMap(Map<String, String> data) {
		// TODO Auto-generated method stub
		Iterator<Entry<String, String>> it = data.entrySet().iterator();
		
		List<Integer> categories = new ArrayList<Integer>();
		
	    while (it.hasNext()) {
	        Entry<String, String> pairs = it.next();
	        // System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        
	        if( pairs.getKey().indexOf("musicCategoryId") > -1 ){
	        	try {
	        		Integer id = Integer.valueOf( pairs.getValue() );
	        		categories.add(id);
	        		
	        	} catch(Exception e){
	        		
	        	}
	        }
	        
	        it.remove(); // avoids a ConcurrentModificationException
	        
	        
	    }
	    
	    updateCategories(categories);
		
	}
	
	public void updateCategories(List<Integer> categories){
		
		List<MusicCategory> existingCategories = getMusicCategories();
		Map<Integer,MusicCategory> categoryMap = new HashMap<Integer,MusicCategory>();
		
		for(MusicCategory m: existingCategories){
			categoryMap.put(m.getId(), m);
		}
		
		List<Integer> addCategories = new ArrayList<Integer>();
 		
		for(Integer id : categories){
			if( categoryMap.containsKey(id) ){
				categoryMap.remove(id);
			} else {
				addCategories.add(id);
			}
		}
		
		
		
		// remove categories
		for( Integer removeId : categoryMap.keySet()){
			MusicCategory m = categoryMap.get(removeId);
			int index = existingCategories.indexOf(m);
			if( index > -1 ){
				existingCategories.remove(index);
			}
		}
		
		// add categories
		for( Integer addId : addCategories ){
			MusicCategory m = MusicCategory.find.byId(addId);
			if( m!=null){
				existingCategories.add(m);
			}
		}
		
		// System.out.println( categoryMap );
		
		update();
	}
	
}
