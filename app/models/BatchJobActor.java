package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="batch_job_actors")
public class BatchJobActor extends AppModel {

	public enum Status
	{
		
		not_started,
		running,
		completed,
		failed
	}	
	
	@ManyToOne
	private BatchJob batchJob;
	
	private Integer objectId;
	
	private Date startedDate;
	
	private Date endedDate;
	
	private Integer result;
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	public static Model.Finder<Integer,BatchJobActor> find = new Finder<Integer, BatchJobActor>(Integer.class, BatchJobActor.class);

	public BatchJob getBatchJob() {
		return batchJob;
	}

	public void setBatchJob(BatchJob batchJob) {
		this.batchJob = batchJob;
	}

	public Integer getObjectId() {
		return objectId;
	}

	public void setObjectId(Integer objectId) {
		this.objectId = objectId;
	}

	private Date getStartedDate() {
		return startedDate;
	}

	private void setStartedDate(Date startedDate) {
		this.startedDate = startedDate;
	}

	private Date getEndedDate() {
		return endedDate;
	}

	private void setEndedDate(Date endedDate) {
		this.endedDate = endedDate;
	}

	private Integer getResult() {
		return result;
	}

	private void setResult(Integer result) {
		this.result = result;
	}
	
}
