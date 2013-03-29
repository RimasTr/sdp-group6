package balle.brick;

import java.io.DataInputStream;
import java.io.IOException;

import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

/**
 * Similar to testReliability but uses Buffered streams. Not finished yet.
 */
public class TestReliability2 {

	public static final int MESSAGES_TO_RECEIVE = 500;

	public static void main(String[] args) {

		drawMessage("Connecting...");
		BTConnection connection = Bluetooth.waitForConnection();
		drawMessage("Connected");
		DataInputStream input = connection.openDataInputStream();

		try {
			drawMessage(Integer.toString(input.available()));
		} catch (IOException e) {
			System.err.print("Failed to read available memory: " + e);
		}
	}
	            
	private static void drawMessage(String message) {
		LCD.clear();
		LCD.drawString(message, 0, 0);
		LCD.refresh();
	}
}