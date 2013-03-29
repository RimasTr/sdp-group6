package balle.main;
import balle.bluetooth.messages.AbstractMessage;
import balle.bluetooth.messages.InvalidArgumentException;


public class TestFourByteMessage {

	public static void main(String[] args) throws InvalidArgumentException {
		// Choose message here
		byte[] bytes = { 32, 90, 49, 120 };
		System.out.println("First element of bytes is: " + bytes[0]);
		System.out.println("Second element of bytes is: " + bytes[1]);
		System.out.println("Third element of bytes is: " + bytes[2]);
		System.out.println("Fourth element of bytes is: " + bytes[3]);

		int message = AbstractMessage.convertFourBytesToInt(bytes);
		System.out.println("The final message is: " + message);
	}
}
