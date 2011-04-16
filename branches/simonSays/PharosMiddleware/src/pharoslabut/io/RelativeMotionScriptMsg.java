package pharoslabut.io;

import pharoslabut.navigate.*;

public class RelativeMotionScriptMsg extends Message {
	private static final long serialVersionUID = 8723022641241659981L;
	private RelativeMotionScript script;
	
	public RelativeMotionScriptMsg(RelativeMotionScript script) {
		this.script = script;
	}
	
	public RelativeMotionScript getScript() {
		return script;
	}

	@Override
	public MsgType getType() {
		return MsgType.LOAD_RELATIVE_MOTION_SCRIPT;
	}
}
