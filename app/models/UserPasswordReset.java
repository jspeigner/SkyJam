package models;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.RandomStringUtils;

import play.data.format.Formats;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;


@Entity
@Table(name="user_password_resets")
public class UserPasswordReset extends AppModel {
	
	@ManyToOne
	private User user;
	
	@Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createdDate;	
	
	@Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
	@Nullable
	private Date resetDate;	
	
	private String code;
	
	public final static long EXPIRATION_TIMESPAN = 86400000; // in milliseconds - 1 day
	
	public static Model.Finder<Integer,UserPasswordReset> find = new Finder<Integer, UserPasswordReset>(Integer.class, UserPasswordReset.class);

	public void createNew(User user){
		
		this.setUser(user);
		this.setCreatedDate(new Date());
		this.setCode( getRandomCode(user) );
		save();
	}



	private String getRandomCode(User user) {
		return user.getId() + "-" + new Date().getTime() + "-" + RandomStringUtils.randomAlphanumeric(8).toUpperCase() + "-" + RandomStringUtils.randomAlphanumeric(8).toUpperCase();
	}
	
	public static UserPasswordReset findByCode(String code){
		
		
		return UserPasswordReset.find.where()
					.eq("code", code)
					.eq("resetDate", null)
					.gt("createdDate",  new Date(System.currentTimeMillis() - EXPIRATION_TIMESPAN ))
					.findUnique();
	}
	
	public void setAsUsed(){
		setResetDate(new Date());
		update();
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Date getResetDate() {
		return resetDate;
	}

	public void setResetDate(Date resetDate) {
		this.resetDate = resetDate;
	}
}
