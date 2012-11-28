package actors;

public class TestActor extends BaseActor {

	@Override
	public void onReceive(Object data) throws Exception {
		
	
		super.onReceive(data);
		if( batchJobActor != null){
			System.out.println("TestActor - Message : " + data);
			Thread.sleep((long) ( Math.random()*10));
			markCompleted();
		} 
		
		batchJobActor = null;
		
		
	}

}
