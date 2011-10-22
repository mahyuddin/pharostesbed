package pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial;

/**
 * Records whether it is potentially safe to cross the intersection, and
 * when it is for sure going to be safe.
 * 
 * @author Chien-Liang Fok
 */
public class SafeState {

	/**
	 * Whether it may be safe to cross the intersection.
	 */
	private boolean isSafe;
	
	/**
	 * The time when safety is ensured.
	 */
	private long safeTime;
	
	/**
	 * The constructor.
	 * 
	 * @param isSafe Whether it is potentially safe to cross.
	 */
	public SafeState(boolean isSafe) {
		this(isSafe, System.currentTimeMillis() + V2VSerialClientDaemon.MIN_SAFE_DURATION);
	}
	
	/**
	 * The constructor.
	 * 
	 * @param isSafe Whether it is potentially safe to cross.
	 * @param safeTime The time when safety is ensured.
	 */
	public SafeState(boolean isSafe, long safeTime) {
		this.isSafe = isSafe;
		this.safeTime = safeTime; 
	}
	
	public boolean isSafe() {
		return isSafe;
	}
	
	public long getSafeTime() {
		return safeTime;
	}
	
	public String toString() {
		return getClass().getName() + ", isSafe=" + isSafe + ", safeTime=" + safeTime;
	}
}
