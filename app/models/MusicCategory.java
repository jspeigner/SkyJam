package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;

import play.db.ebean.Model;

@Entity
@Table(name="music_categories")
public class MusicCategory extends Model {

	@Id
	public Integer id;
	
	@Length(max=200)
	public String title;
	
	@ManyToOne
	@JoinColumn(name="parent_id")
	public MusicCategory parent;
}
