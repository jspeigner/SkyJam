package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import models.Playlist.Status;
import models.behavior.ImageMetadata;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Page;
import com.avaje.ebean.validation.Length;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import scala.Tuple2;

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
	
	public enum Type
	{
		activity,
		popular
	}
	
	@Enumerated(EnumType.STRING)
	private Type type;	
	
	
	public static final ImageMetadata imageMetadata = new ImageMetadata(240, 180, ImageMetadata.IMAGE_TYPE_PNG, "files/music_category/image/%d.png", "files/music_category/image/default.png" );	
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

	public StorageObject getImageStorageObject() {
		return imageStorageObject;
	}

	public void setImageStorageObject(StorageObject imageStorageObject) {
		this.imageStorageObject = imageStorageObject;
	}
	
	public String getImageUrl()
	{
		return imageMetadata.getImageUrlFromStorageObject( getImageStorageObject() );
	}
	
	public List<MusicCategory> getChildren()
	{
		return MusicCategory.find.where().eq("parent", this).findList();
	}
	
	public boolean isParentOf(MusicCategory m)
	{
		if(m != null )
		{
			if( m.getParent().getId() == this.getId() )
			{
				return true;
			}
			else
			{
				// recursive search
				List<MusicCategory> children = MusicCategory.find.where().eq("parent", m).findList();
				for( MusicCategory child : children )
				{
					if( child.isParentOf(m))
					{
						return true;
					}
				}
				
				return false;
			}
		}
		else
		{
			return false;
		}
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

	@SuppressWarnings("unchecked")
	public static List<Tuple2<String, String>> getActivitiesList( Map<Integer, MusicCategory> activities, String activityNameSeparator  ) {
		
		
		List<Tuple2<String, String>> results = new ArrayList<Tuple2<String,String>>();
		
		for( Object id : activities.keySet() ){

			MusicCategory activity = activities.get(id);
			
			Map<Integer, MusicCategory> subActivities = (Map<Integer, MusicCategory>) find.where().eq("type", Type.activity).eq("parent", activity).setMapKey("id").findMap();
			if( subActivities.size() > 0 ){
				List<Tuple2<String, String>> subResults = getActivitiesList(subActivities, activityNameSeparator);
				if( (subResults != null) && ( subResults.size() > 0 )){
					for( Tuple2<String,String> tuple : subResults ){
						
						results.add( new Tuple2<String, String>( tuple._1, activity.getName()+ activityNameSeparator + tuple._2 ) );
						
					}
					
				}
			} else {
				
				results.add(new Tuple2<String, String>( id.toString(), activity.getName() ));
				
			}
			
		}
		
		return results; 
	}
	
	@SuppressWarnings("unchecked")
	public static List<Tuple2<String, String>> getActivitiesList(String activityNameSeparator) {
		
		Map<Integer, MusicCategory> activities = (Map<Integer, MusicCategory>) find.where().eq("type", Type.activity).eq("parent_id", 0).setMapKey("id").findMap();
		
		return getActivitiesList(activities, activityNameSeparator);
	}
	
	public static List<Tuple2<String, String>> getActivitiesList() {
		return getActivitiesList(" - ");
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public static Page<MusicCategory> getPageWithSearch(int page, int pageSize, String term){
		return getPageWithSearch(page, pageSize, term, null);
	}
	
	public static Page<MusicCategory> getPageWithSearch(int page, int pageSize, String term, Expression additionalConditions){
		
    	if( ( term != null ) && ( !term.isEmpty())){
    		try {  
    		      Integer id = Integer.parseInt( term );  
    		      
    		      Expression expr = Expr.eq("id", id);
    		      
  	    		
	  	    		if( additionalConditions != null ){
	  	    			expr = Expr.and( additionalConditions , expr );
	  	    		}    		      
    		      
    		      return MusicCategory.find.where().eq("id", id).findPagingList(pageSize).getPage(page);
    		      
    		} catch( Exception e ) {
    			
    	    		String likeQueryString =  "%" + term.trim() + "%";
    	    		
    	    		Expression expr = Expr.ilike("name", likeQueryString);	
    	    		
    	    		if( additionalConditions != null ){
    	    			expr = Expr.and( additionalConditions , expr );
    	    		}
    	    		
    	    		return MusicCategory.find.where( expr ).findPagingList(pageSize).getPage(page);
    		}
    		

    	} else {
    		return ( ( additionalConditions == null ) ? MusicCategory.find : MusicCategory.find.where(additionalConditions) ).findPagingList(pageSize).getPage(page);
    	}		
		
	}
	
	public int getPlaylistsCount(){
		return Playlist.findCountByMusicCategoryId(getId());
	}
	
}
