#include <Timer.h>
#include "RadioSignalMeter.h"

configuration RadioSignalMeterAppC {
}
implementation {
  components RadioSignalMeterM, ResultsSenderM;
  components MainC, LedsBlinkerC; //, new TimerMilliC() as Timer0;
  

  RadioSignalMeterM.Boot -> MainC;
  RadioSignalMeterM.LedsBlinker -> LedsBlinkerC;
  //RadioSignalMeterM.Timer0 -> Timer0;
  
  components ActiveMessageC, new AMSenderC(AM_BEACONMSG), new AMReceiverC(AM_BEACONMSG) as BeaconMsgRcvr;
  components new SerialAMReceiverC(AM_SENDBEACONMSG) as SendBeaconMsgRcvr;
  RadioSignalMeterM.Packet -> AMSenderC;
  RadioSignalMeterM.AMPacket -> AMSenderC;
  RadioSignalMeterM.AMControl -> ActiveMessageC;
  RadioSignalMeterM.AMSend -> AMSenderC;
  RadioSignalMeterM.ReceiveBeacon -> BeaconMsgRcvr;
  RadioSignalMeterM.ReceiveSendBeacon -> SendBeaconMsgRcvr;
  
  RadioSignalMeterM.ResultsSenderI -> ResultsSenderM;

  // Allow program to set the tx power level...
  components CC2420PacketC;
  RadioSignalMeterM.CC2420Packet -> CC2420PacketC;
  
  components new TimerMilliC() as Timer1;
  ResultsSenderM.Boot -> MainC;
  ResultsSenderM.Timer -> Timer1;
  
  //
  // Serial communication component.  This is documented in TEP 113: Serial Communication.
  //
  components new SerialAMSenderC(AM_RADIOSIGNALRESULTSMSG), SerialActiveMessageC;
  ResultsSenderM.SerialControl -> SerialActiveMessageC;
  ResultsSenderM.SerialSend -> SerialAMSenderC.AMSend;
  
  components new PoolC(message_t, 10) as UARTMessagePoolP, new QueueC(message_t*, 10) as UARTQueueP;
  ResultsSenderM.UARTQueue -> UARTQueueP;
  ResultsSenderM.UARTMessagePool -> UARTMessagePoolP;
  ResultsSenderM.LedsBlinker -> LedsBlinkerC;
}
