package pharoslabut.io;

//import pharoslabut.navigate.*;
import pharoslabut.navigate.motionscript.MotionScript;

public class MotionScriptMsg implements Message {
	
	private static final long serialVersionUID = -7631305555004386678L;
	
	private MotionScript script;
	
	public MotionScriptMsg(MotionScript script) {
		this.script = script;
	}
	
	public MotionScript getScript() {
		return script;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.LOAD_GPS_MOTION_SCRIPT;
	}
	
	public String toString() {
		return "MotionScriptMsg: " + script;
	}
}
