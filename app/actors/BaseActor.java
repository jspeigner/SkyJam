package actors;

import models.BatchJobActor;
import akka.actor.UntypedActor;

public class BaseActor extends UntypedActor {

	@Override
	public void onReceive(Object data) throws Exception {
		// TODO Auto-generated method stub
		
		if( data != null ){
			
			if( data instanceof BatchJobActor){
				
			}
			
		}
		
	}

}
