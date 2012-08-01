package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.db.ebean.Model;

@Entity
@Table(name="playlist_songs")
public class PlaylistSong extends Model {

	@Id
	public Integer id;
	
	@ManyToOne
	public Playlist playlist;
	
	@OneToOne
	public Song song;
	
}
