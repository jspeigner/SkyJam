package actors;

import java.util.List;

import models.AmazonAccount;

public class AmazonS3ImportActor extends BaseActor {

	
	@Override
	public void onReceive(Object data) throws Exception {
		
		super.onReceive(data);
		if( batchJobActor != null){
			// Batch Job Actor contains Amazon Account Id
			
			int amazonAccountId = batchJobActor.getObjectId();
			
			if( importSongsFromAmazonAccount(amazonAccountId)){
				markCompleted();
			} else {
				markFailed();
			}
		}
		
	}
	
	protected boolean importSongsFromAmazonAccount(Integer amazonAccountId){
		AmazonAccount amazonAccount = AmazonAccount.find.byId(amazonAccountId);
		if( amazonAccount != null ){
			List<models.Bucket> buckets = models.Bucket.find.where().eq("amazon_account_id", amazonAccount.getId()).findList(); 
			
			for(models.Bucket bucket : buckets){
				bucket.importMusicFromBucket();
			}
			
			return true;
			
		}
		
		return false;
	}
	
}
