package playerclient.structures.opaque;

/**
 * An implementation of the player opaque data structure.
 * 
 * @author Chien-Liang Fok
 */
public class PlayerOpaqueData {
	private int dataCount;
	private byte[] data;
	
	public PlayerOpaqueData(int dataCount, byte[] data) {
		this.dataCount = dataCount;
		this.data = new byte[data.length];
		System.arraycopy(data, 0, this.data, 0, data.length);
	}
	
	public int getDataCount() {
		return dataCount;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public String toString() {
		String result = "dataCount = " + dataCount;
		if (data != null) {
			result += ", data = [" + data[0];
			for (int i=1; i < data.length; i++) {
				result += ", " + data[i];
			}
			result += "]";
		}
		return result;
	}
}
