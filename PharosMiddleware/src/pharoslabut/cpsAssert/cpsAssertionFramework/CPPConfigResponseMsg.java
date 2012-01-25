package pharoslabut.cpsAssert.cpsAssertionFramework;

import java.net.InetAddress;

import pharoslabut.io.AckedMsg;

public class CPPConfigResponseMsg implements AckedMsg {

	private static final long serialVersionUID = -1870241912295256776L;

	private String actualSource; 
	private boolean sourceExists;
	private boolean dataTypeMatches;
	private Object logicalReference;

	public CPPConfigResponseMsg(Object logicalReference, String actualSource, boolean sourceExists, boolean dataTypeMatches) {
		this.logicalReference = logicalReference;
		this.actualSource = actualSource;	
		this.sourceExists = sourceExists;
		this.dataTypeMatches = dataTypeMatches;
	}
	
	

	public String toString() {
		return "Mapping actualSource: " + actualSource + (sourceExists ? " exists on server." : " does not exist on server. " + 
				"actualSource " + actualSource + " provides a incompatible data type with the logicalReference object.");
	}


	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}


	/**
	 * @return the actualSource
	 */
	public String getActualSource() {
		return actualSource;
	}
	
	
	public Object getLogicalReference() {
		return logicalReference;
	}
	

	/**
	 * @return the sourceExists
	 */
	public boolean doesSourceExist() {
		return sourceExists;
	}


	/**
	 * @return the dataTypeMatches
	 */
	public boolean doesDataTypeMatch() {
		return dataTypeMatches;
	}

}
