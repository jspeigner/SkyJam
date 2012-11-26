package actors;

import java.util.Date;

import models.BatchJobActor;
import akka.actor.UntypedActor;

public class BaseActor extends UntypedActor {

	protected BatchJobActor batchJobActor;
	
	@Override
	public void onReceive(Object data) throws Exception {
		// TODO Auto-generated method stub
		
		if( data != null ){
			
			// read the batch job actor
			if( data instanceof Integer){
				batchJobActor = BatchJobActor.find.where().eq( "id", (Integer)data ).eq("status", BatchJobActor.Status.not_started ).findUnique();
				batchJobActor.setStatus(BatchJobActor.Status.running);
				batchJobActor.setStartedDate(new Date());
				batchJobActor.update();
			} else {
				// batch id is wrong, ignoring
				batchJobActor = null;
			}
			
		}
		
	}
	
	public void markCompleted(){
		if( batchJobActor != null ){
			batchJobActor.setStatus(BatchJobActor.Status.completed );
			batchJobActor.setEndedDate(new Date());
			batchJobActor.update();
		}
	}
	
	public void markFailed(){
		markFailed(0);
	}
	
	public void markFailed(int resultCode){
		if( batchJobActor != null ){
			batchJobActor.setStatus(BatchJobActor.Status.failed );
			batchJobActor.setEndedDate(new Date());
			batchJobActor.setResult(resultCode);
			batchJobActor.update();
		}
	}
	
	

	
	
}
