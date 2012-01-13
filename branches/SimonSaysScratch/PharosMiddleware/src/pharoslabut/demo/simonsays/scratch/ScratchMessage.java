// ScratchMessage.java
// Andrew Davison, March 2009, ad@fivedots.coe.psu.ac.th

/* The Scratch message format is described in
        http://scratch.mit.edu/forums/viewtopic.php?id=9458 

   The two types:
      broadcast "<name-string>"
      sensor-update <name-string> <value-string>
  
   We only support one name-value pair in a sensor-update message,
   and we assume the value is a string.

   The quotes are removed from name-string, and the "Scratch-" prefix
   is deleted (if present).
*/
package pharoslabut.demo.simonsays.scratch;

public class ScratchMessage
{
  // possible message types
  public static final int BROADCAST_MSG = 0;
  public static final int SENSOR_UPDATE_MSG = 1;
  public static final int UNKNOWN_MSG = 2;

  private int msgType = UNKNOWN_MSG;   // default values
  private String msgTypeStr = "unknown";
  private String varName = null;
  private String valStr = null;  


  public ScratchMessage(int type, String typeStr, String name, String val) {
      msgType = type;
      msgTypeStr = typeStr;
      varName = name;
      valStr = val;
  }
  
  // accessor methods

  public int getMessageType()
  {  return msgType;  }

  public String getMessageTypeStr()
  {  return msgTypeStr;  }

  public String getName()
  {  return varName;  }

  public String getValue()
  {  return valStr;  }


  public String toString()
  {
    if (msgType == BROADCAST_MSG)
      return msgTypeStr + " " + valStr;
    else if (msgType ==  SENSOR_UPDATE_MSG)
      return msgTypeStr + " " + varName + " " + valStr;
    else // unknown message
      return msgTypeStr;  
  }  // end of toString()

}  // end of ScratchMessage class