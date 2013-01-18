package actors;

import controllers.components.UserInvitationsMailer;
import akka.actor.UntypedActor;


public class UserInvitationsMailerActor extends UntypedActor {
	  
	 
	  public void onReceive(Object message) throws Exception {
	    if (message instanceof UserInvitationsMailer)
	      try{
	    	  ((UserInvitationsMailer) message).call();
	      } catch(Exception e){
	    	  e.printStackTrace();
	      }
	  }
}
