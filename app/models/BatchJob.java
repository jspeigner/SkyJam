package models;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import models.BatchJobActor.Status;

import actors.TestActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Page;
import com.avaje.ebean.validation.Length;

import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import scala.Tuple2;

@Entity
@Table(name="batch_jobs")
public class BatchJob extends AppModel {

	private Date createdDate;
	
	@Length(max=200,min=1)
	@Constraints.Required	
	private String name;
	
	private String actorClass;
	
	@Column(nullable=true)
	private String result;
	
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
	
	public Integer getMinBatchJobObjectId(){
		BatchJobActor b = BatchJobActor.find.where().eq("batchJob", this).order("object_id ASC").setMaxRows(1).findUnique();
		return b != null ? b.getObjectId() : null;
	}
	
	public Integer getMaxBatchJobObjectId(){
		BatchJobActor b = BatchJobActor.find.where().eq("batchJob", this).order("object_id DESC").setMaxRows(1).findUnique();
		return b != null ? b.getObjectId() : null;		
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
		
		ActorRef actor = getActorRef();
		if( actor != null ){
			List<BatchJobActor> batchJobActors = BatchJobActor.find.where().eq("batchJob", this).findList();
			for( BatchJobActor batchJobActor : batchJobActors){
				
				actor.tell( batchJobActor.getId() );
				
			}
		}
		
	}
	
	public void delete(){
		
		ActorSystem actorSystem = getActorSystem();
		if( actorSystem != null){
			ActorRef a = getActorRef();
			if(a!=null){
				actorSystem.stop(a);
			}
		}
		
		
		Ebean.beginTransaction();  
		
		try {

			List<BatchJobActor> actors = BatchJobActor.find.where().eq("batchJob", this).findList();
			if(actors != null){
				Ebean.delete(actors);
			}

			Ebean.commitTransaction();
		} finally {
			Ebean.endTransaction();
		}	
		
		super.delete();
	}
	
	public static List<Tuple2<String, String>> getActorList(){
		
		List<Tuple2<String, String>> list = new ArrayList<Tuple2<String,String>>();
		list.add(new Tuple2<String, String>("AmazonS3ImportActor", "Import music from S3 account"));
		list.add(new Tuple2<String, String>("SongMetadataActor", "Read Song Metadata"));
		list.add(new Tuple2<String, String>("TestActor", "Test actor"));
		list.add(new Tuple2<String, String>("EchonestUpdateAlbumArt", "Update Album Art Image from echonest data"));
		
		
		return list;
		
	}
	
	protected ActorSystem getActorSystem(){
		return ActorSystem.create("BatchJob" + this.getId() );
	}
	
	@SuppressWarnings("unchecked")
	protected ActorRef getActorRef(){
		String actorClass = this.getActorClass();
		
		ActorSystem system = getActorSystem();
		if( system != null ){
			
			
			
			try {
				Class c = Class.forName("actors."+actorClass);
				
				if( Actor.class.isAssignableFrom(c) ){
					
					ActorRef myActor = system.actorOf(new Props(c), actorClass);
				
					return myActor;
				}
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		
			
		}
		
		return null;
		
	}

	public String getResult() {
		return result;
	}

	public void setResult(String results) {
		this.result = results;
	}
	
	public boolean isCompleted(){
		return BatchJobActor
					.find
					.where()
					.and( 
							Expr.eq("batchJob", this), 
							Expr.or( 
									Expr.eq("status", BatchJobActor.Status.not_started) , 
									Expr.eq("status", BatchJobActor.Status.running
							)
					)
				).findRowCount() == 0;
	}
}
