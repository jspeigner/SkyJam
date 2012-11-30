package models;

import java.util.Date;

import javax.persistence.Entity;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

@Entity
public class SongMetadata extends AppModel {

	
	private String album;
	private String artist;
	private String title;
	private String genre;
	private String track;
	private String musicbrainzTrackId;
	private String mood;
	private Double bpm;
	private Integer year;
	private Date createdDate;
	
	public void init(Tag tags){
		setAlbum(tags.getFirst(FieldKey.ALBUM));
		setArtist(tags.getFirst(FieldKey.ARTIST));
		setTitle(tags.getFirst(FieldKey.TITLE));
		setGenre(tags.getFirst(FieldKey.GENRE));
		setTrack(tags.getFirst(FieldKey.TRACK));
		setMusicbrainzTrackId(tags.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID));
		setMood(tags.getFirst(FieldKey.MOOD));
		
		try {
			setBpm( Double.parseDouble(tags.getFirst(FieldKey.BPM)) );
		} catch (Exception e){
			setBpm(null);
		}
	
		try {
			Integer.parseInt(tags.getFirst(FieldKey.YEAR));
		} catch (Exception e){
			setYear(null);
		}
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}

	public String getMusicbrainzTrackId() {
		return musicbrainzTrackId;
	}

	public void setMusicbrainzTrackId(String musicbrainzTrackId) {
		this.musicbrainzTrackId = musicbrainzTrackId;
	}

	public String getMood() {
		return mood;
	}

	public void setMood(String mood) {
		this.mood = mood;
	}

	public Double getBpm() {
		return bpm;
	}

	public void setBpm(Double bpm) {
		this.bpm = bpm;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
}
