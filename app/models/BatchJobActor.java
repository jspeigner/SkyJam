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

	public Date getStartedDate() {
		return startedDate;
	}

	public void setStartedDate(Date startedDate) {
		this.startedDate = startedDate;
	}

	public Date getEndedDate() {
		return endedDate;
	}

	public void setEndedDate(Date endedDate) {
		this.endedDate = endedDate;
	}

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
