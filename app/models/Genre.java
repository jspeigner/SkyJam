package models;


import javax.persistence.Entity;

import javax.persistence.Table;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Page;
import com.avaje.ebean.validation.Length;

import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity
@Table(name="genres")
public class Genre extends AppModel {

	@Length(max=100,min=1)
	@Constraints.Required
	private String name;

	public static Model.Finder<Integer,Genre> find = new Finder<Integer, Genre>(Integer.class, Genre.class);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Page<Genre> getPageWithSearch(int page, int pageSize, String term){
		return getPageWithSearch(page, pageSize, term, null);
	}
	
	public static Page<Genre> getPageWithSearch(int page, int pageSize, String term, Expression additionalConditions){
		
    	if( ( term != null ) && ( !term.isEmpty())){
    		try {  
    		      Integer id = Integer.parseInt( term );  
    		      
    		      Expression expr = Expr.eq("id", id);
    		      
  	    		
	  	    		if( additionalConditions != null ){
	  	    			expr = Expr.and( additionalConditions , expr );
	  	    		}    		      
    		      
    		      return Genre.find.where().eq("id", id).findPagingList(pageSize).getPage(page);
    		      
    		} catch( Exception e ) {
    			
    	    		String likeQueryString =  "%" + term.trim() + "%";
    	    		
    	    		Expression expr = Expr.ilike("name", likeQueryString);	
    	    		
    	    		if( additionalConditions != null ){
    	    			expr = Expr.and( additionalConditions , expr );
    	    		}
    	    		
    	    		return Genre.find.where( expr ).findPagingList(pageSize).getPage(page);
    		}
    		

    	} else {
    		return ( ( additionalConditions == null ) ? Genre.find : Genre.find.where(additionalConditions) ).findPagingList(pageSize).getPage(page);
    	}		
		
	}	
	
}
