package pharoslabut;

public class threadAbortTest {
	GUI pavGUI;
	
	public threadAbortTest(GUI gui){
		pavGUI = gui;
		int counter = 0;
		while(!pavGUI.abortMovement){
			counter++;
			try{
				Thread.sleep(1000);
			}catch(InterruptedException exc){
				System.exit(1);
			}
			System.out.println("Counter: "+counter);
		}
		System.out.println("Thread aborted");
	}
	
}
