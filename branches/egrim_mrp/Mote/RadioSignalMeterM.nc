#include <Timer.h>
#include <CC2420.h>
#include "RadioSignalMeter.h"

/**
 * Periodically emits a beacon and listens for beacons to arrive.
 * Whenever a beacon arrives, it extracts the values within the beacon,
 * obtains the RSSI and LQI values with which the message was received,
 * and passes this data to the ResultsSender component.
 *
 * @author Chien-Liang Fok
 */
module RadioSignalMeterM {
  uses {
    interface Boot;
    interface LedsBlinker;
    //interface Timer<TMilli> as Timer0;
    interface Packet;
    interface AMPacket;
    interface AMSend;
    interface Receive as ReceiveBeacon;
    interface SplitControl as AMControl;
    interface ResultsSenderI;
    interface Receive as ReceiveSendBeacon;
    interface CC2420Packet;
  }
}
implementation {
  message_t pkt;
  bool busy = FALSE;
  //uint32_t seqno = 0;

  event void Boot.booted() {
    call AMControl.start();
  }

  event void AMControl.startDone(error_t err) {
    if (err != SUCCESS) {
      //call Timer0.startPeriodic(TIMER_PERIOD_MILLI);
    //} else {
      call AMControl.start();
    }
  }
  
  event void AMControl.stopDone(error_t err) {
  }
  
  /**
   * Broadcast a beacon message each time we receive a SendBeaconMsg.
   * This message is sent by the x86 each time a beacon should be sent.
   * If success, blink the green LED.
   */
  //event void Timer0.fired() {
  event message_t* ReceiveSendBeacon.receive(message_t* msg, void* payload, uint8_t len) {
    if (len == sizeof(SendBeaconMsg) && !busy) {
      SendBeaconMsg* sndBeaconMsg = (SendBeaconMsg*)payload;
      BeaconMsg* beacon = (BeaconMsg*)(call Packet.getPayload(&pkt, sizeof(BeaconMsg)));
      if (beacon == NULL) {
        return msg;
      }
      call CC2420Packet.setPower(&pkt, sndBeaconMsg->txPwr); // set the transmit power
      beacon->idSender = sndBeaconMsg->sndrID;
      beacon->seqno = sndBeaconMsg->seqno;
      if (call AMSend.send(AM_BROADCAST_ADDR, &pkt, sizeof(BeaconMsg)) == SUCCESS) {
        busy = TRUE;
        //seqno++;
        call LedsBlinker.blink(LED_GREEN, 1 /*count*/, 50 /*period*/);
      }
    }
    return msg;
  }

  event void AMSend.sendDone(message_t* msg, error_t err) {
    if (&pkt == msg)
      busy = FALSE;
  }

  /**
   * Called when a beacon from another node is received.
   * Blink the blue LED to indicate reception of beacon.
   */
  event message_t* ReceiveBeacon.receive(message_t* msg, void* payload, uint8_t len) {
    if (len == sizeof(BeaconMsg)) {
      cc2420_metadata_t *ptr = (cc2420_metadata_t*)(msg->metadata); 
      BeaconMsg* beaconMsg = (BeaconMsg*)payload;
      uint8_t rssi = ptr->rssi;
      uint8_t lqi = ptr->lqi;
      call ResultsSenderI.send(beaconMsg, rssi, lqi);
      call LedsBlinker.blink(LED_BLUE, 1 /*count*/, 50 /*period*/);
    }
    return msg;
  }
  
  event void LedsBlinker.blinkDone() {}
}
