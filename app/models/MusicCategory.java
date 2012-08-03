package models;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="music_categories")
public class MusicCategory extends AppModel {
	
	@Length(max=200)
	public String name;
	
	@ManyToOne
	@JoinColumn(name="parent_id")
	public MusicCategory parent;
	
	@ManyToMany
	@JoinTable(name = "songs_music_categories", joinColumns = { @JoinColumn(name="music_category_id") }, inverseJoinColumns = { @JoinColumn(name="song_id") } )
	public Set<MusicCategory> musicCategories;
	
	
	public static Model.Finder<Integer,MusicCategory> find = new Finder<Integer, MusicCategory>(Integer.class, MusicCategory.class);
	
	
}
