package pa3;
import java.nio.ByteBuffer;

public class MessageType {

	int sourceNode;
	int destNode;
	byte[] payload;

	public MessageType(int sourceNode, int destNode, byte[] payload) {
		this.sourceNode = sourceNode;
		this.destNode = destNode;
		this.payload = payload;
	}
	public int getSourceNode() {
		return sourceNode;
	}
	public void setSourceNode(int sourceNode) {
		this.sourceNode = sourceNode;
	}
	public int getDestNode() {
		return destNode;
	}
	public void setDestNode(int destNode) {
		this.destNode = destNode;
	}
	public byte[] getPayload() {
		return payload;
	}
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	public byte[] toBytes() {
		ByteBuffer buf = ByteBuffer.allocate(1536);
		buf.clear();
		buf.putInt(sourceNode);
		buf.putInt(destNode);
		buf.put(payload);
		return buf.array();
	}
	public static MessageType bytearray2messagetype(byte[] arr) throws NumberFormatException {
		ByteBuffer buf = ByteBuffer.allocate(arr.length);
		buf = ByteBuffer.wrap(arr);
		int sNode = buf.getInt();
		int dNode = buf.getInt();
		byte[] payl = new byte[arr.length - 8];
		buf.get(payl);
		MessageType mt = new MessageType(sNode, dNode, payl);
		return mt;
	}
	public String toString() {
		return (sourceNode + " to " + destNode + ":" + payload.length);
	}
}
