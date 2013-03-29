package balle.brick;

import java.io.DataInputStream;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

public class TestReliability {

	public static final int MESSAGES_TO_RECEIVE = 500;

	public static void main(String[] args) {

		while (true) {
			// Enter button click will halt the program
			if (Button.ENTER.isDown())
				break;

			drawMessage("Connecting...");
			BTConnection connection = Bluetooth.waitForConnection();
			drawMessage("Connected");
			DataInputStream input = connection.openDataInputStream();

			int receivedMessages = 0;
			for (int i = 0; i < MESSAGES_TO_RECEIVE; i++) {
				// Try to read message, if completed, then increment
				// messages read variable. Otherwise, close program.
				try {
					int hashedMessage = input.readInt();
					// Thread.sleep(100);
				} catch (Exception e) {
					drawMessage("Received "
							+ Integer.toString(receivedMessages));
					connection.close();
				}
				receivedMessages++;
			}
			drawMessage("Received " + Integer.toString(receivedMessages));
		}
	}
	            
	private static void drawMessage(String message) {
		LCD.clear();
		LCD.drawString(message, 0, 0);
		LCD.refresh();
	}
}