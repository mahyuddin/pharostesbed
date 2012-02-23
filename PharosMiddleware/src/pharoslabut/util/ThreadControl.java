package pharoslabut.util;


public class ThreadControl {
	
	public static void pause(Object lock, long duration) {
		try {
			synchronized(lock) {
				lock.wait(duration);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
