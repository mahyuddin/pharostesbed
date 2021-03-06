package pharoslabut.radioMeter.cc2420;

/**
 * This class is automatically generated by mig. DO NOT EDIT THIS FILE.
 * This class implements a Java interface to the 'SendBeaconMsg'
 * message type.
 */

public class SendBeaconMsg extends net.tinyos.message.Message {

    /** The default size of this message type in bytes. */
    public static final int DEFAULT_MESSAGE_SIZE = 7;

    /** The Active Message type associated with this message. */
    public static final int AM_TYPE = 3;

    /** Create a new SendBeaconMsg of size 7. */
    public SendBeaconMsg() {
        super(DEFAULT_MESSAGE_SIZE);
        amTypeSet(AM_TYPE);
    }

    /** Create a new SendBeaconMsg of the given data_length. */
    public SendBeaconMsg(int data_length) {
        super(data_length);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new SendBeaconMsg with the given data_length
     * and base offset.
     */
    public SendBeaconMsg(int data_length, int base_offset) {
        super(data_length, base_offset);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new SendBeaconMsg using the given byte array
     * as backing store.
     */
    public SendBeaconMsg(byte[] data) {
        super(data);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new SendBeaconMsg using the given byte array
     * as backing store, with the given base offset.
     */
    public SendBeaconMsg(byte[] data, int base_offset) {
        super(data, base_offset);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new SendBeaconMsg using the given byte array
     * as backing store, with the given base offset and data length.
     */
    public SendBeaconMsg(byte[] data, int base_offset, int data_length) {
        super(data, base_offset, data_length);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new SendBeaconMsg embedded in the given message
     * at the given base offset.
     */
    public SendBeaconMsg(net.tinyos.message.Message msg, int base_offset) {
        super(msg, base_offset, DEFAULT_MESSAGE_SIZE);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new SendBeaconMsg embedded in the given message
     * at the given base offset and length.
     */
    public SendBeaconMsg(net.tinyos.message.Message msg, int base_offset, int data_length) {
        super(msg, base_offset, data_length);
        amTypeSet(AM_TYPE);
    }

    /**
    /* Return a String representation of this message. Includes the
     * message type name and the non-indexed field values.
     */
    public String toString() {
      String s = "Message <SendBeaconMsg> \n";
      try {
        s += "  [txPwr=0x"+Long.toHexString(get_txPwr())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [sndrID=0x"+Long.toHexString(get_sndrID())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [seqno=0x"+Long.toHexString(get_seqno())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      return s;
    }

    // Message-type-specific access methods appear below.

    /////////////////////////////////////////////////////////
    // Accessor methods for field: txPwr
    //   Field type: short, unsigned
    //   Offset (bits): 0
    //   Size (bits): 8
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'txPwr' is signed (false).
     */
    public static boolean isSigned_txPwr() {
        return false;
    }

    /**
     * Return whether the field 'txPwr' is an array (false).
     */
    public static boolean isArray_txPwr() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'txPwr'
     */
    public static int offset_txPwr() {
        return (0 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'txPwr'
     */
    public static int offsetBits_txPwr() {
        return 0;
    }

    /**
     * Return the value (as a short) of the field 'txPwr'
     */
    public short get_txPwr() {
        return (short)getUIntBEElement(offsetBits_txPwr(), 8);
    }

    /**
     * Set the value of the field 'txPwr'
     */
    public void set_txPwr(short value) {
        setUIntBEElement(offsetBits_txPwr(), 8, value);
    }

    /**
     * Return the size, in bytes, of the field 'txPwr'
     */
    public static int size_txPwr() {
        return (8 / 8);
    }

    /**
     * Return the size, in bits, of the field 'txPwr'
     */
    public static int sizeBits_txPwr() {
        return 8;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: sndrID
    //   Field type: int, unsigned
    //   Offset (bits): 8
    //   Size (bits): 16
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'sndrID' is signed (false).
     */
    public static boolean isSigned_sndrID() {
        return false;
    }

    /**
     * Return whether the field 'sndrID' is an array (false).
     */
    public static boolean isArray_sndrID() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'sndrID'
     */
    public static int offset_sndrID() {
        return (8 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'sndrID'
     */
    public static int offsetBits_sndrID() {
        return 8;
    }

    /**
     * Return the value (as a int) of the field 'sndrID'
     */
    public int get_sndrID() {
        return (int)getUIntBEElement(offsetBits_sndrID(), 16);
    }

    /**
     * Set the value of the field 'sndrID'
     */
    public void set_sndrID(int value) {
        setUIntBEElement(offsetBits_sndrID(), 16, value);
    }

    /**
     * Return the size, in bytes, of the field 'sndrID'
     */
    public static int size_sndrID() {
        return (16 / 8);
    }

    /**
     * Return the size, in bits, of the field 'sndrID'
     */
    public static int sizeBits_sndrID() {
        return 16;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: seqno
    //   Field type: long, unsigned
    //   Offset (bits): 24
    //   Size (bits): 32
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'seqno' is signed (false).
     */
    public static boolean isSigned_seqno() {
        return false;
    }

    /**
     * Return whether the field 'seqno' is an array (false).
     */
    public static boolean isArray_seqno() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'seqno'
     */
    public static int offset_seqno() {
        return (24 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'seqno'
     */
    public static int offsetBits_seqno() {
        return 24;
    }

    /**
     * Return the value (as a long) of the field 'seqno'
     */
    public long get_seqno() {
        return (long)getUIntBEElement(offsetBits_seqno(), 32);
    }

    /**
     * Set the value of the field 'seqno'
     */
    public void set_seqno(long value) {
        setUIntBEElement(offsetBits_seqno(), 32, value);
    }

    /**
     * Return the size, in bytes, of the field 'seqno'
     */
    public static int size_seqno() {
        return (32 / 8);
    }

    /**
     * Return the size, in bits, of the field 'seqno'
     */
    public static int sizeBits_seqno() {
        return 32;
    }

}
