package models;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="music_categories")
public class MusicCategory extends AppModel {
	
	@Length(max=200)
	private String name;
	
	@ManyToOne
	@JoinColumn(name="parent_id")
	private MusicCategory parent;
	
	protected Integer parentId;

	@OneToOne
	private StorageObject imageStorageObject;
	
	/*
	@ManyToMany
	@JoinTable(name = "songs_music_categories", joinColumns = { @JoinColumn(name="music_category_id") }, inverseJoinColumns = { @JoinColumn(name="song_id") } )
	public Set<MusicCategory> musicCategories;
	*/
	
	public static Model.Finder<Integer,MusicCategory> find = new Finder<Integer, MusicCategory>(Integer.class, MusicCategory.class);
	
	public Integer getParentId()
	{
		return parentId;
	}

	protected StorageObject getImageStorageObject() {
		return imageStorageObject;
	}

	protected void setImageStorageObject(StorageObject imageStorageObject) {
		this.imageStorageObject = imageStorageObject;
	}
	
	public String getImageUrl()
	{
		if(imageStorageObject!=null)
		{
			return imageStorageObject.getUrl();
		}
		
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MusicCategory getParent() {
		return parent;
	}

	public void setParent(MusicCategory parent) {
		this.parent = parent;
	}
}
