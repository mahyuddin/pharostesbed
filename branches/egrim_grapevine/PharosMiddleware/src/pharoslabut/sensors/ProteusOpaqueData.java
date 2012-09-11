package pharoslabut.sensors;

/**
 * An implementation of the player opaque data structure.
 * Taken from the javaclient2 project.
 */
public class ProteusOpaqueData {
	private int dataCount;
	private byte[] data;
	
	public ProteusOpaqueData(int dataCount, byte[] data) {
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
