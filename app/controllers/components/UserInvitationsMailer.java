package controllers.components;

import java.util.List;
import java.util.concurrent.Callable;

import controllers.BaseController;

import models.User;
import models.UserInvitationCode;
import play.mvc.Http;

public class UserInvitationsMailer implements Callable<Integer> {
	
	private List<User> users;
	private Http.Context context;
	
	public UserInvitationsMailer(List<User> users, Http.Context context) {
		this.users = users;
		this.context = context;
	}
	
	@Override
	public Integer call() throws Exception {

		Http.Context.current.set(context);
		
		int i = 0;
		for(User user : users){
	    	UserInvitationCode uic = UserInvitationCode.createNewCode(user);
	    	
	    	BaseController.email(user.getEmail(), "A private invitation to check out the SkyJam.fm", views.html.Email.text.userInvitation.render(user, uic).toString());
	    	// System.out.println("Inviation has been sent to "+ user.getEmail());
			i++;
		}		
		return i;
	}
	
}