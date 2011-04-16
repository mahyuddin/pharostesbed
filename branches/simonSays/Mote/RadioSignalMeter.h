/**
 * @author Chien-Liang Fok
 */

#ifndef RADIOSIGNALMETER_H
#define RADIOSIGNALMETER_H

// The following are valid for the TelosB platform
#define LED_RED 0x01
#define LED_GREEN 0x02
#define LED_BLUE 0x04

enum {
  AM_BEACONMSG = 1,
  AM_RADIOSIGNALRESULTSMSG = 2,
  AM_SENDBEACONMSG = 3,
  TIMER_PERIOD_MILLI = 1024
};

/**
 * Sent by the x86 to the mote prompting it to broadcast a beacon.
 * This is to enable greater control over when the beacons are emitted.
 */
typedef nx_struct SendBeaconMsg {
	nx_uint32_t seqno;
} SendBeaconMsg;

typedef nx_struct BeaconMsg {
  nx_uint16_t idSender; /* Mote id of sending mote. */
  nx_uint32_t seqno;
} BeaconMsg;

typedef nx_struct RadioSignalResultsMsg {
  nx_uint16_t idReceiver; /* Mote id of receiving mote. */
  nx_uint16_t idSender; /* Mote id of sending mote. */
  nx_uint32_t seqno;
  nx_uint8_t rssi;
  nx_uint8_t lqi;
  nx_uint32_t timestamp; /* Time at which beacon was received, see: http://www.tinyos.net/tinyos-2.x/doc/html/tep102.html*/
} RadioSignalResultsMsg;

#endif
