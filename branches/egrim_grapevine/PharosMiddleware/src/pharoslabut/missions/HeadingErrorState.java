package pharoslabut.missions;

/**
 * Maps a time stamp to a heading.
 * 
 * @author Chien-Liang Fok
 */
public class HeadingErrorState extends HeadingState {
	
	public HeadingErrorState(long timeStamp, double heading) {
		super(timeStamp, heading);
	}
}