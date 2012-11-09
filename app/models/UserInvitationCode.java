package models;

import javax.annotation.Nullable;
import javax.persistence.*;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.validation.Length;

import java.util.List;
import java.util.Set;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;


@Entity
@Table(name="user_invitation_codes")
public class UserInvitationCode extends AppModel {

	@Length(max=32)
	private String code;

	@Column(nullable=true)
	@ManyToOne
	protected User user;
	
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
		UserInvitationCode uic = new UserInvitationCode();
		String code;
		while( UserInvitationCode.isAvailable( code = org.apache.commons.lang.RandomStringUtils.random(32, UserInvitationCode.codeCharacters ) ) != null );
		uic.setCode( code );
		uic.save();
		
		return uic;
	}
}
