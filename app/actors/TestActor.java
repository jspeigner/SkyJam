package actors;

import akka.actor.UntypedActor;

public class TestActor extends UntypedActor {

	@Override
	public void onReceive(Object arg0) throws Exception {
		// TODO Auto-generated method stub

		System.out.println("Actor Message : " + arg0);
		Thread.sleep((long) ( Math.random()*100));
	}

}
