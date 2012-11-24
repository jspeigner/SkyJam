package models;



import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

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

	private String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	private String getActorClass() {
		return actorClass;
	}

	private void setActorClass(String actorClass) {
		this.actorClass = actorClass;
	}
	
}
