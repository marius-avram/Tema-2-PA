import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ServerConnection {
	public static byte[] readMessage(DataInputStream in) {
		byte[] message = null;
		// There's no way in java to know if the socket closed on the other side
		// We can only read something and see if it throws a java.io.EOFException
		try {
			byte size = in.readByte();
			message = new byte[size];
			in.readFully(message);
		} catch (EOFException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return message;
	}
	
	public static void sendMessage(final byte[] message, DataOutputStream out) {
		byte size = (byte) message.length;
		try {
			out.writeByte(size);
			out.write(message);
		} catch (SocketException e) {
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
}
	