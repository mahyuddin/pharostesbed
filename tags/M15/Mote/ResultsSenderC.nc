#include <Timer.h>
#include "RadioSignalMeter.h"

configuration ResultsSenderC {
  provides {
    interface ResultsSenderI;
  }
}
implementation {
  components MainC;
  components LedsC;
  components ResultsSenderM as App;
  components new TimerMilliC() as Timer0;
  components ActiveMessageC;
  components new AMSenderC(AM_RADIOSIGNALMSG);
  components new AMReceiverC(AM_RADIOSIGNALMSG);

  App.Boot -> MainC;
  App.Leds -> LedsC;
  App.Timer0 -> Timer0;
  App.Packet -> AMSenderC;
  App.AMPacket -> AMSenderC;
  App.AMControl -> ActiveMessageC;
  App.AMSend -> AMSenderC;
  App.Receive -> AMReceiverC;
}
