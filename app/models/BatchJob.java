package models;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.persistence.Entity;
import javax.persistence.Table;

import models.BatchJobActor.Status;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Page;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import scala.Tuple2;

@Entity
@Table(name="batch_jobs")
public class BatchJob extends AppModel {

	private Date createdDate;
	
	private String name;
	
	private String actorClass;
	
	public static Model.Finder<Integer,BatchJob> find = new Finder<Integer, BatchJob>(Integer.class, BatchJob.class);

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getActorClass() {
		return actorClass;
	}

	public void setActorClass(String actorClass) {
		this.actorClass = actorClass;
	}
	
	public void createActors(int objectStartId, int objectEndId ) {
		
		Ebean.beginTransaction();  
		
		try {
			for(int id = objectStartId; id <= objectEndId; id++){
				
				BatchJobActor b = new BatchJobActor();
				b.setBatchJob(this);
				b.setObjectId(id);
				b.setResult(0);
				b.setStartedDate(null);
				b.setEndedDate(null);
				b.setStatus(BatchJobActor.Status.not_started);
				b.save();
				
			}
			Ebean.commitTransaction();
		} finally {
			Ebean.endTransaction();
		}
		
	}
	
	public int getActorCountByStatus( BatchJobActor.Status status ){
		
		return BatchJobActor.find.where().eq("batchJob", this).eq("status", status ).findRowCount();
	}
	
	public int getActorCount( ){
		return BatchJobActor.find.where().eq("batchJob", this).findRowCount();
	}
	
	public static Page<BatchJob> getPageWithSearch(int page, int pageSize, String term){
		return getPageWithSearch(page, pageSize, term, null);
	}
	
	public static Page<BatchJob> getPageWithSearch(int page, int pageSize, String term, Expression additionalConditions){
    	
    	if( ( term!=null ) && ( !term.isEmpty())){
    		try {  
    		      Integer id = Integer.parseInt( term );  
    		      
    		      Expression expr = Expr.eq("id", id);
    		      
  	    		
	  	    		if( additionalConditions != null ){
	  	    			expr = Expr.and( additionalConditions , expr );
	  	    		}    		      
    		      
    		      return BatchJob.find.where().eq("id", id).findPagingList(pageSize).getPage(page);
    		      
    		} catch( Exception e ) {
    			
    	    		String likeQueryString =  "%" + term.trim() + "%";
    	    		
    	    		Expression expr = Expr.ilike("name", likeQueryString);	
    	    		
    	    		if( additionalConditions != null ){
    	    			expr = Expr.and( additionalConditions , expr );
    	    		}
    	    		
    	    		return BatchJob.find.where( expr ).findPagingList(pageSize).getPage(page);
    		}
    		

    	} else {
    		return ( ( additionalConditions == null ) ? BatchJob.find : BatchJob.find.where(additionalConditions) ).findPagingList(pageSize).getPage(page);
    	}
	}	
	
	public void run() {
		
	}
	
	public static List<Tuple2<String, String>> getActorList(){
		
		List<Tuple2<String, String>> list = new ArrayList<Tuple2<String,String>>();
		list.add(new Tuple2<String, String>("AmazonS3ImportActor", "Import music from S3 account"));
		
		return list;
		
	}
}
