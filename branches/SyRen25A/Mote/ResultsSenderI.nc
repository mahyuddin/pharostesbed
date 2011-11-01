interface ResultsSenderI {
  command void send(BeaconMsg* beaconMsg, uint8_t rssi, uint8_t lqi);
}
