package models;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Page;
import com.avaje.ebean.validation.Length;

import java.util.List;

import play.db.ebean.Model;
import play.data.validation.*;


@Entity
@Table(name="artists")
public class Artist extends AppModel {


	@Constraints.Required
	@Length(max=200)
	private String name;
	
	private String description;
	
	@Length(max=200)
	private String url;
	
	
	
	@OneToMany(targetEntity=Album.class)
	private List<Album> albums;
	
	
	public static Model.Finder<Integer,Artist> find = new Finder<Integer, Artist>(Integer.class, Artist.class);
	
	
	public static Artist getByName(String name)
	{
		return getByName(name, false);
	}
	
	public static Artist getByName(String name, boolean insertOnFail)
	{
		Artist a = Artist.find.where().eq("name", name).findUnique();
		if( a == null)
		{
			if( insertOnFail )
			{
				a = new Artist();
				a.setName(name);
				a.setDescription("");
				a.setUrl("");
				a.save();
			}
			
			return a;
		}
		else
		{
			return a;
		}
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Album> getAlbums() {
		return albums;
	}

	public void setAlbums(List<Album> albums) {
		this.albums = albums;
	}

	/**
	 * Get official artist website URL 
	 * @return
	 */
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}	
	
	public static Page<Artist> getPageWithSearch(int page, int pageSize, String term){
		return getPageWithSearch(page, pageSize, term, null);
	}
	
	public static Page<Artist> getPageWithSearch(int page, int pageSize, String term, Expression additionalConditions){
    	
    	if( ( term != null ) && ( !term.isEmpty())){
    		try {  
    		      Integer id = Integer.parseInt( term );  
    		      
    		      Expression expr = Expr.eq("id", id);
    		      
  	    		
	  	    		if( additionalConditions != null ){
	  	    			expr = Expr.and( additionalConditions , expr );
	  	    		}    		      
    		      
    		      return Artist.find.where().eq("id", id).findPagingList(pageSize).getPage(page);
    		      
    		} catch( Exception e ) {
    			
    	    		String likeQueryString =  "%" + term.trim() + "%";
    	    		
    	    		Expression expr = Expr.ilike("name", likeQueryString);	
    	    		
    	    		if( additionalConditions != null ){
    	    			expr = Expr.and( additionalConditions , expr );
    	    		}
    	    		
    	    		return Artist.find.where( expr ).findPagingList(pageSize).getPage(page);
    		}
    		

    	} else {
    		return ( ( additionalConditions == null ) ? Artist.find : Artist.find.where(additionalConditions) ).findPagingList(pageSize).getPage(page);
    	}
	}	
	
}
