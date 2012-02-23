#include "RadioSignalMeter.h"

/**
 * Sends the results over the serial port.  Uses a buffer to
 * ensure all of the results are delivered.  This is important
 * because beacons may arrive when results are being sent.
 *
 * @author Chien-Liang Fok
 */
module ResultsSenderM {
  provides {
    interface ResultsSenderI;
  }
  uses {
    interface Boot;
    interface SplitControl as SerialControl;
    interface AMSend as SerialSend;
    interface Queue<message_t *> as UARTQueue;
    interface Pool<message_t> as UARTMessagePool;
    interface LedsBlinker;
    interface Timer<TMilli>; // for getting the current time stamp
  }
}
implementation {
  task void uartSendTask();
  static void fatal_problem();
  static void report_problem();
  
  uint8_t uartlen;
  message_t uartbuf;
  bool sendbusy=FALSE, uartbusy=FALSE;

  event void Boot.booted() {
    // Beginning our initialization phases:
    if (call SerialControl.start() != SUCCESS)
      fatal_problem();
  }
  
  event void SerialControl.startDone(error_t error) {
    if (error != SUCCESS)
      fatal_problem();
  }

  event void SerialControl.stopDone(error_t error) { }
  
  void saveBeacon(RadioSignalResultsMsg* resultsMsg, BeaconMsg* beaconMsg, uint8_t rssi, uint8_t lqi) {
    resultsMsg->idReceiver = TOS_NODE_ID;
    resultsMsg->idSender = beaconMsg->idSender;
    resultsMsg->seqno = beaconMsg->seqno;
    resultsMsg->rssi = rssi;
    resultsMsg->lqi = lqi;
    resultsMsg->timestamp = call Timer.getNow();
  }
  
  command void ResultsSenderI.send(BeaconMsg* beaconMsg, uint8_t rssi, uint8_t lqi) {
    RadioSignalResultsMsg* out;
    if (uartbusy == FALSE) {
      out = (RadioSignalResultsMsg*)call SerialSend.getPayload(&uartbuf, sizeof(RadioSignalResultsMsg));
      if (out == NULL) {
        return;
      } else {
        saveBeacon(out, beaconMsg, rssi, lqi);
      }
      uartlen = sizeof(RadioSignalResultsMsg);
      post uartSendTask();
    } else {
      // The UART is busy; queue up messages and service them when the
      // UART becomes free.
      message_t *newmsg = call UARTMessagePool.get();
      if (newmsg == NULL) {
        // drop the message on the floor if we run out of queue space.
        report_problem();
        return;
      }

      //Serial port busy, so enqueue.
      out = (RadioSignalResultsMsg*)call SerialSend.getPayload(newmsg, sizeof(RadioSignalResultsMsg));
      if (out == NULL) {
        // unable to get payload of message buffer, return the buffer to the pool
        // and abort
        call UARTMessagePool.put(newmsg);
        return;
      }

      saveBeacon(out, beaconMsg, rssi, lqi);

      if (call UARTQueue.enqueue(newmsg) != SUCCESS) {
        // drop the message on the floor and hang if we run out of
        // queue space without running out of queue space first (this
        // should not occur).
        call UARTMessagePool.put(newmsg);
        fatal_problem();
        return;
      }
    }
  }

  task void uartSendTask() {
    if (call SerialSend.send(0xffff, &uartbuf, uartlen) != SUCCESS) {
      report_problem();
    } else {
      uartbusy = TRUE;
    }
  }
  
  event void SerialSend.sendDone(message_t *msg, error_t error) {
    uartbusy = FALSE;
    if (call UARTQueue.empty() == FALSE) {
      // We just finished a UART send, and the uart queue is
      // non-empty.  Let's start a new one.
      message_t *queuemsg = call UARTQueue.dequeue();
      if (queuemsg == NULL) {
        fatal_problem();
        return;
      }
      memcpy(&uartbuf, queuemsg, sizeof(message_t));
      if (call UARTMessagePool.put(queuemsg) != SUCCESS) {
        fatal_problem();
        return;
      }
      post uartSendTask();
    }
  }
  
  event void Timer.fired() {}
  
  static void fatal_problem() { 
    call LedsBlinker.blink(LED_GREEN|LED_RED|LED_BLUE, 5 /*count*/, 512 /*period*/);
  }
  
  static void report_problem() {
    call LedsBlinker.blink(LED_RED, 1 /*count*/, 100 /*period*/); 
  }
  
  event void LedsBlinker.blinkDone() {}
}
