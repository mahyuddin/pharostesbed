// MakeSensor.java
// Andrew Davison, March 2009, ad@fivedots.coe.psu.ac.th

// Create a virtual sensor in Scratch using the supplied name and value.
package pharoslabut.demo.simonsays.scratch;

public class MakeSensor
{

  public static void main(String args[])
  {
    if (args.length != 2)
      System.out.println("Usage: MakeSensor <name> <value>");
    else {
      ScratchIO sio = new ScratchIO();
      sio.updateMsg(args[0], args[1]);
      sio.closeDown();
    }
  }  // end of main()


}  // end of MakeSensor class

