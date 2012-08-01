package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

import com.amazonaws.auth.AWSCredentials;
import com.avaje.ebean.validation.Length;

@Entity 
@Table(name="amazon_accounts")
public class AmazonAccount extends Model implements AWSCredentials {




	@Id
	public Integer id;
	
	@Length(min=16, max=45)
	public String accessKeyId;

	@Length(min=16, max=64)
	public String secretAccessKey;	
	
	
	@Override
	public String getAWSAccessKeyId() {
		// TODO Auto-generated method stub
		return accessKeyId;
	}

	@Override
	public String getAWSSecretKey() {
		// TODO Auto-generated method stub
		return secretAccessKey;
	}
	
	public static Model.Finder<Integer,AmazonAccount> find = new Finder<Integer, AmazonAccount>(Integer.class, AmazonAccount.class);
	
	public static AmazonAccount getDefaultAccount()
	{
		try
		{
			AmazonAccount tmp = find.all().get(0);
			
			// find.where().orderBy("id asc").getFirstRow();
			
			return tmp;
		}
		catch(Exception e)
		{
			return null;
		}
		
	}

}
