package pharoslabut.io;

public class StopExpMsg implements Message {
	private static final long serialVersionUID = 6593878637228833693L;

	public StopExpMsg() {
		
	}
	
	@Override
	public MsgType getType() {
		return MsgType.STOPEXP;
	}
}
