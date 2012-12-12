package models;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import javax.persistence.Table;

import models.behavior.ImageMetadata;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Page;
import com.avaje.ebean.validation.Length;

import play.data.format.Formats;
import play.db.ebean.Model;


@Entity 
@Table(name="albums")
public class Album extends AppModel {

	@Length(max=200)
	private String name;
	
	@ManyToOne
	private Artist artist;
	
	@ManyToOne
	private Genre genre;
	
	private Integer copyrightYear;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date createdDate;
	
	private String keywords;
	
	private String description;
	
	@OneToMany(targetEntity=Song.class)
	private List<Song> songs;	
	
	public static final ImageMetadata imageMetadata = new ImageMetadata(240, 240, ImageMetadata.IMAGE_TYPE_PNG, "files/album/image/%d.png", "files/album/image/default.png" );
	
	@OneToOne(cascade=CascadeType.REMOVE)
	private StorageObject albumArtStorageObject;
	
	public static Model.Finder<Integer,Album> find = new Finder<Integer, Album>(Integer.class, Album.class);
	
	public static Album getByName(String name)
	{
		return Album.find.where().eq("name", name).findUnique();
	}

	public static Album getByNameAndArtistId(String albumName, Integer artistId) {

		return Album.find.where().eq("name", albumName).eq("artist_id", artistId).findUnique();
	}
	
	public String getName()
	{
		return name;
	}

	public StorageObject getAlbumArtStorageObject() {
		return albumArtStorageObject;
	}

	public void setAlbumArtStorageObject(StorageObject albumArtStorageObject) {
		this.albumArtStorageObject = albumArtStorageObject;
	}
	
	public String getAlbumArtUrl()
	{
		return imageMetadata.getImageUrlFromStorageObject(  getAlbumArtStorageObject());

	}

	public void setName(String name) {
		this.name = name;
	}

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public Genre getGenre() {
		return genre;
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}

	public Integer getCopyrightYear() {
		return copyrightYear;
	}

	public void setCopyrightYear(Integer copyrightYear) {
		this.copyrightYear = copyrightYear;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}		
	
	public static Page<Album> getPageWithSearch(int page, int pageSize, String term, Expression additionalConditions){
    	
    	if( ( term!=null ) && ( !term.isEmpty())){
    		try {  
    		      Integer id = Integer.parseInt( term );  
    		      
    		      Expression expr = Expr.eq("id", id);
    		      
  	    		
	  	    		if( additionalConditions != null ){
	  	    			expr = Expr.and( additionalConditions , expr );
	  	    		}    		      
    		      
    		      return Album.find.where().eq("id", id).findPagingList(pageSize).getPage(page);
    		      
    		} catch( Exception e ) {
    			
    	    		String likeQueryString =  "%" + term.trim() + "%";
    	    		
    	    		Expression expr = Expr.or(
	    				Expr.ilike("name", likeQueryString), 
   						Expr.ilike("artist.name", likeQueryString)									

    	    		);	
    	    		
    	    		if( additionalConditions != null ){
    	    			expr = Expr.and( additionalConditions , expr );
    	    		}
    	    		
    	    		return Album.find.where( expr ).findPagingList(pageSize).getPage(page);
    		}
    		

    	} else {
    		return ( ( additionalConditions == null ) ? Album.find : Album.find.where(additionalConditions) ).findPagingList(pageSize).getPage(page);
    	}
	}	
	
	public boolean updateImage(InputStream sourceImage)
	{
		StorageObject s = StorageObject.updateStorageObjectWithImage( imageMetadata.getFilename(getId()), sourceImage, imageMetadata);
		setAlbumArtStorageObject(s);
		
		update();
		
		return s != null;
			
	}

	public List<Song> getAlbums() {
		return songs;
	}

	public void setAlbums(List<Song> albums) {
		this.songs = albums;
	}	
	
}
