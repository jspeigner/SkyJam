package models;

import javax.persistence.*;

import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import models.Playlist.Status;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Page;
import com.avaje.ebean.validation.Length;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.SongParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import play.Play;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="songs")
public class Song extends AppModel {
	
	@Length(max=300)
	private String name;
	
	@ManyToOne
	private Album album;
	
	private String keywords;
	
	private Integer duration;
	
	private Integer tracknumber;
	
	@ManyToOne
	private StorageObject storageObject;
	
	public enum Status
	{
		visible,
		hidden
	};
	
	@Enumerated(EnumType.STRING)
	private Status status;
	

	@OneToOne
	private EchonestSong echonestSong;
	
	
	/*
	@ManyToMany
	@JoinTable(name = "songs_music_categories", joinColumns = { @JoinColumn(name="song_id") }, inverseJoinColumns = { @JoinColumn(name="music_category_id") } )
	public Set<MusicCategory> musicCategories;
	*/
	
	public static Model.Finder<Integer,Song> find = new Finder<Integer, Song>(Integer.class, Song.class);
	
	public static Song getByNameAndAlbumId(String name, Integer albumId) {
		 
		return Song.find.where().eq("name", name).eq("album_id", albumId).findUnique();
	}	
	
	public static List<Song> searchWideByName(String query, int maxResults) 
	{
		
		String likeQueryString =  "%" + query + "%";
		
		Expression e2 = Expr.and(
				Expr.eq("status", Status.visible),
				Expr.or(
						Expr.ilike("name", likeQueryString), 
						Expr.or(
								Expr.ilike("album.name", likeQueryString), 
								Expr.ilike("album.artist.name", likeQueryString)									
						)
				)	
		);
		
		return Song.find.where(e2).setMaxRows(maxResults).findList();
		
	}	
	
	public String getName()
	{
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public StorageObject getStorageObject() {
		return storageObject;
	}

	public void setStorageObject(StorageObject storageObject) {
		this.storageObject = storageObject;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public String getSongUrl()
	{
		if(storageObject!=null)
		{
			return storageObject.getUrl();
		}
		
		return null;
	}	
	
	public static Page<Song> getPageWithSearch(int page, int pageSize, String term){
		return getPageWithSearch(page, pageSize, term, null);
	}
	
	public static Page<Song> getPageWithSearch(int page, int pageSize, String term, Expression additionalConditions){
    	
    	if( ( term!=null ) && ( !term.isEmpty())){
    		try {  
    		      Integer id = Integer.parseInt( term );  
    		      
    		      Expression expr = Expr.eq("id", id);
    		      
  	    		
	  	    		if( additionalConditions != null ){
	  	    			expr = Expr.and( additionalConditions , expr );
	  	    		}    		      
    		      
    		      return Song.find.where().eq("id", id).findPagingList(pageSize).getPage(page);
    		      
    		} catch( Exception e ) {
    			
    	    		String likeQueryString =  "%" + term.trim() + "%";
    	    		
    	    		Expression expr = Expr.or(
	    				Expr.ilike("name", likeQueryString), 
	    					Expr.or(
	    						Expr.ilike("album.name", likeQueryString), 
	    						Expr.ilike("album.artist.name", likeQueryString)									
	    					)
    	    		);	
    	    		
    	    		if( additionalConditions != null ){
    	    			expr = Expr.and( additionalConditions , expr );
    	    		}
    	    		
    	    		return Song.find.where( expr ).findPagingList(pageSize).getPage(page);
    		}
    		

    	} else {
    		return ( ( additionalConditions == null ) ? Song.find : Song.find.where(additionalConditions) ).findPagingList(pageSize).getPage(page);
    	}
	}

	public Integer getTracknumber() {
		return tracknumber;
	}

	public void setTracknumber(Integer tracknumber) {
		this.tracknumber = tracknumber;
	}	
	
	public Tag readMetadataTags() {
		Tag tags = null;
		
		if( this.getStorageObject() != null ){
    			
			File tempFile = null;
			StorageObject s = this.getStorageObject();
    			
    			if( s.getInputStream() != null ){
    			
    				try {
    				
		    	        tempFile = readStorageObjectInFile(s);
		    			
						AudioFile a;
						
						a = AudioFileIO.read( tempFile );
		
						tags = a.getTag();

					} catch (Exception e) {
						e.printStackTrace();
					}
    			}
    			
    			if( tempFile != null ){
    				tempFile.delete();
    			}
    			
    		}
		
		return tags;
	}

	private File readStorageObjectInFile(StorageObject s) throws IOException,
			FileNotFoundException {
		File tempFile;
		InputStream in = s.getInputStream();
		tempFile = File.createTempFile("song_id_"+this.getId()+"_", "." + FilenameUtils.getExtension(s.getName()) );
		OutputStream out = new FileOutputStream(tempFile );
		
		byte[] buffer = new byte[1024*64];
		int bytesRead = 0;
		
		while ((bytesRead = in.read(buffer)) > 0) {  
			out.write(buffer, 0, bytesRead);
		}

		in.close();
		out.close();
		return tempFile;
	}	
	
	
	
	public List<com.echonest.api.v4.Song> getEchonestApiSongs()  throws EchoNestException {
		return this.getEchonestSongsCall(1);
	}
	
	
	public List<com.echonest.api.v4.Song> getEchonestSongsCall(int results) throws EchoNestException {
    	String fullName = this.getName();
    	
    	EchoNestAPI en = new EchoNestAPI( Play.application().configuration().getString("echonest.api_key") );
    	
    	SongParams p = new SongParams();
    	
    	if( ( this.getAlbum() != null ) && ( this.getAlbum().getArtist() != null ) ){

    		p.setArtist(this.getAlbum().getArtist().getName());
    		
    		fullName = this.getAlbum().getArtist().getName() + " " + fullName;
    	}
    	
    	// System.out.printf("[%s]\n", fullName);
    	
        p.includeTracks();               // the album art is in the track data
        p.setLimit(true);                // only return songs that have track data
        p.addIDSpace("7digital-US");     // include 7digital specific track data
    	
    	
    	p.setTitle(this.getName());
        
        p.setResults(results);
        
		return en.searchSongs(p);
			
		
	}
	
	public void saveEchonestSong(com.echonest.api.v4.Song echonestSong){
		EchonestSong eSong = getEchonestSong();
		if(eSong == null){
			eSong = new EchonestSong();
		}
		eSong.init(echonestSong);
		eSong.save();
		
		setEchonestSong( eSong );
		update();
	}

	public EchonestSong getEchonestSong() {
		return echonestSong;
	}

	public void setEchonestSong(EchonestSong echonestSong) {
		this.echonestSong = echonestSong;
	}
	
}
