package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
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

import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import scala.Tuple2;

@Entity
@Table(name="music_categories")
public class MusicCategory extends AppModel {
	
	@Length(max=200)
	@Constraints.Required
	private String name;
	
	@ManyToOne
	@JoinColumn(name="parent_id")
	private MusicCategory parent;

	@OneToOne(cascade=CascadeType.REMOVE)
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

	public StorageObject getImageStorageObject() {
		return imageStorageObject;
	}
	
	public static MusicCategory getFirstWithPlaylists(List<MusicCategory> categories){
		
		for(MusicCategory category: categories){
			if( category.getPlaylistsCount() > 0 ){
				return category;
			}
		}
		
		return null;
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
		
		int selfId = this.getId();
		MusicCategory current = m;
		while( current != null  ){
			if( current.getParent() != null){
				if( current.getParent().getId() == selfId){
					return true;
				}
			}
			
			current = current.getParent();
		}
		
		
		return false;
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

	public static List<Tuple2<String, String>> getActivitiesList( Map<Integer, MusicCategory> activities, String activityNameSeparator   ) {
		return getTreeList(activities, activityNameSeparator, Type.activity);
	}
	
	@SuppressWarnings("unchecked")
	public static List<Tuple2<String, String>> getTreeList( Map<Integer, MusicCategory> activities, String activityNameSeparator, Type type  ) {
		
		
		List<Tuple2<String, String>> results = new ArrayList<Tuple2<String,String>>();
		
		for( Object id : activities.keySet() ){

			MusicCategory activity = activities.get(id);
			
			Map<Integer, MusicCategory> subActivities = (Map<Integer, MusicCategory>) find.where().eq("type", type).eq("parent", activity).setMapKey("id").findMap();
			if( subActivities.size() > 0 ){
				List<Tuple2<String, String>> subResults = getTreeList(subActivities, activityNameSeparator, type);
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
	public static List<Tuple2<String, String>> getTreeList( String activityNameSeparator, Type type ) {
		
		Map<Integer, MusicCategory> activities = (Map<Integer, MusicCategory>) find.where().eq("type", type).eq("parent", null).setMapKey("id").findMap();
		
		return getTreeList(activities, activityNameSeparator, type);
	}
	
	@SuppressWarnings("unchecked")
	public static List<Tuple2<String, String>> getActivitiesList(String activityNameSeparator) {
		
		Map<Integer, MusicCategory> activities = (Map<Integer, MusicCategory>) find.where().eq("type", Type.activity).eq("parent", null).setMapKey("id").findMap();
		
		return getActivitiesList(activities, activityNameSeparator);
	}
	
	
	
	public static List<Tuple2<String, String>> getTypeList(){
		
		List<Tuple2<String,String>> types = new ArrayList<Tuple2<String,String>>();
		types.add(new Tuple2<String, String>(MusicCategory.Type.activity.toString(), "Activity"));
		types.add(new Tuple2<String, String>(MusicCategory.Type.popular.toString(), "Popular"));
		
		return types;
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
	
	public void delete(){
		
		List<MusicCategory> children = getChildren();
		if( children != null ){
			
			MusicCategory parent = getParent();
			// link children categories to the parent
			for (int i = 0; i < children.size(); i++) {
				children.get(i).setParent(parent);
				children.get(i).update();
			}
		}
		
		List<Playlist> playlists = Playlist.find.where().eq("musicCategories", this).findList();
		if(playlists!=null){
			for(Playlist p : playlists){
				p.getMusicCategories().remove(this);
				p.update();
			}
		}
		
		
		super.delete();
	}
	
}
