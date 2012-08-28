package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import javax.persistence.Table;

import models.behavior.ImageMetadata;

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
	
	// public Integer genreId;
	
	private String description;
	
	
	public static final ImageMetadata imageMetadata = new ImageMetadata(240, 240, ImageMetadata.IMAGE_TYPE_PNG, "files/album/image/%d.png", "files/album/image/default.png" );
	
	@ManyToOne
	private StorageObject albumArtStorageObject;
	
	// public Integer albumArtStorageObjectId;
	
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
	
}
