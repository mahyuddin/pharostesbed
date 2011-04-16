package pharoslabut.io;

import pharoslabut.navigate.*;

public class GPSMotionScriptMsg extends Message {
	
	private static final long serialVersionUID = -7631305555004386678L;
	
	private GPSMotionScript script;
	
	public GPSMotionScriptMsg(GPSMotionScript script) {
		this.script = script;
	}
	
	public GPSMotionScript getScript() {
		return script;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.LOAD_GPS_MOTION_SCRIPT;
	}
}
