package models;


import javax.persistence.*;
import com.avaje.ebean.validation.Length;
import play.db.ebean.Model;



@Entity
@Table(name="user_invitation_codes")
public class UserInvitationCode extends AppModel {

	@Length(max=32)
	private String code;

	@Column(nullable=true)
	@ManyToOne
	protected User user;
	
	@Column(nullable=true)
	@ManyToOne
	private User sourceUser;
	
	protected static String codeCharacters = "abcdefghijklmnopqrstuvwxyz01234567890";
	
	public static Model.Finder<Integer,UserInvitationCode> find = new Finder<Integer, UserInvitationCode>(Integer.class, UserInvitationCode.class);
	
	
	public boolean markUsedByUser(User user){
		
		this.user = user;
		this.save();
		
		return true;
	}
	
	public static UserInvitationCode isAvailable(String code){
		return find.where().eq("code", code).eq("user_id", null).setMaxRows(1).findUnique();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public static UserInvitationCode createNewCode(){
		return createNewCode(null);
	}
	
	public static UserInvitationCode createNewCode(User sourceUser){
		UserInvitationCode uic = new UserInvitationCode();
		uic.setSourceUser(sourceUser);
		
		String code;
		while( UserInvitationCode.isAvailable( code = org.apache.commons.lang.RandomStringUtils.random(32, UserInvitationCode.codeCharacters ) ) != null );
		uic.setCode( code );
		uic.save();
		
		return uic;
	}

	public User getSourceUser() {
		return sourceUser;
	}

	public void setSourceUser(User sourceUser) {
		this.sourceUser = sourceUser;
	}
}
