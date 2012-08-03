package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="cloudfront_distributions")
public class CloudfrontDistribution extends AppModel {


	
	@Length(max=32)
	public String distributionId;
	
	@Length(max=45)
	public String domainName;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date lastModificationDate;
	
	public enum Status {
		InProgress,
		Deployed
	};
	
	@Enumerated(EnumType.STRING)
	public Status status;
	
	@Length(max=255)
	public String filename;
	
	@Constraints.Required
	@OneToOne
	public StorageObject storageObject; 
	
	public static Model.Finder<Integer,CloudfrontDistribution> find = new Finder<Integer, CloudfrontDistribution>(Integer.class, CloudfrontDistribution.class);
	
}
