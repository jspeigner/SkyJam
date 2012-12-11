package models;

import java.sql.Timestamp;
import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.echonest.api.v4.EchoNestException;

@Entity
@Table(name="echonest_songs")
public class EchonestSong extends AppModel {

	
	@Column(nullable=true)
	private String echonestSongId;
	@Column(nullable=true)
	private String title;
	@Column(nullable=true)
	private String artistName;
	@Column(nullable=true)
	private Double duration;
	@Column(nullable=true)
	private Double tempo;
	@Column(nullable=true)
	private String artistLocation;
	@Column(nullable=true)
	private String coverArt;
	@Column(nullable=true)
	private String releaseName;
	@Column(nullable=true)
	private Integer mode;
	@Column(nullable=true)
	private Double songHotttnesss;
	@Column(nullable=true)
	private Double artistHotttnesss;
	@Column(nullable=true)
	private String audio;
	
	private Date createdDate;
	
	public String getEchonestSongId() {
		return echonestSongId;
	}
	public void setEchonestSongId(String echonestSongId) {
		this.echonestSongId = echonestSongId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getArtistName() {
		return artistName;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public Double getDuration() {
		return duration;
	}
	public void setDuration(Double duration) {
		this.duration = duration;
	}
	public Double getTempo() {
		return tempo;
	}
	public void setTempo(Double tempo) {
		this.tempo = tempo;
	}
	public String getArtistLocation() {
		return artistLocation;
	}
	public void setArtistLocation(String artistLocation) {
		this.artistLocation = artistLocation;
	}
	public String getCoverArt() {
		return coverArt;
	}
	public void setCoverArt(String coverArt) {
		this.coverArt = coverArt;
	}
	public String getReleaseName() {
		return releaseName;
	}
	public void setReleaseName(String releasName) {
		this.releaseName = releasName;
	}
	
	public void init(com.echonest.api.v4.Song echonestSong) {
		// TODO Auto-generated method stub
		
		setEchonestSongId(echonestSong.getID());
		setTitle(echonestSong.getTitle());
		setArtistName(echonestSong.getArtistName());
		try {
			setDuration(echonestSong.getDuration());
		} catch (EchoNestException e) {
			// TODO Auto-generated catch block
			setDuration(0.0);
		}
		try {
			setTempo(echonestSong.getTempo());
		} catch (EchoNestException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			setTempo(0.0);
		}
		try {
			setArtistLocation(echonestSong.getArtistLocation().toString());
		} catch (EchoNestException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			setArtistLocation("");
		}
		setCoverArt(echonestSong.getString("tracks[0].release_image"));
		try {
			setMode(echonestSong.getMode());
		} catch (EchoNestException e) {
			// e.printStackTrace();
			setMode(0);
		}
		try {
			setSongHotttnesss(echonestSong.getSongHotttnesss());
		} catch (EchoNestException e) {

			// e.printStackTrace();
			setSongHotttnesss(0.0);
		}
		try {
			setArtistHotttnesss(echonestSong.getArtistHotttnesss());
		} catch (EchoNestException e) {

			// e.printStackTrace();
			setArtistHotttnesss(0.0);
		}
		setAudio(echonestSong.getAudio());
		
		setReleaseName(echonestSong.getReleaseName());

	}
	public Integer getMode() {
		return mode;
	}
	public void setMode(Integer mode) {
		this.mode = mode;
	}
	public Double getSongHotttnesss() {
		return songHotttnesss;
	}
	public void setSongHotttnesss(Double songHotttnesss) {
		this.songHotttnesss = songHotttnesss;
	}
	public Double getArtistHotttnesss() {
		return artistHotttnesss;
	}
	public void setArtistHotttnesss(Double artistHotttnesss) {
		this.artistHotttnesss = artistHotttnesss;
	}
	public String getAudio() {
		return audio;
	}
	public void setAudio(String audio) {
		this.audio = audio;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	
}
