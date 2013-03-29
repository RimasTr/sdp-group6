package balle.bluetooth;

import balle.controller.BluetoothController;

public class TestReliability {

	/**
	 * The message to base test on, the following is the list of each command
	 * and their integer representation in this class:
	 * 
	 * 1 -> .setWheelSpeeds(200, 200)
	 * 2 -> .backward(200)
	 * 3 -> floatWheels()
	 * 4 -> .forward(200)
	 * 5 -> .forward(200, 200)
	 * 6 -> .kick()
	 * 7 -> .penaltyKick()
	 * 8 -> .penaltyKickStraight()
	 * 9 -> .rotate(30, 75)
	 * 10 -> .stop()
	 */
	private static int MESSAGE_TO_SEND = 1;
	private static int NUMBER_OF_MESSAGES = 500;
	// Time interval between each message sent
	private int INTERVAL = 0;
	static BluetoothController controller = null;

	
	public TestReliability(BluetoothController bluetoothController) {
		controller = bluetoothController;
	}

	// Picks chosen command and executes it.
	public static void executeCommand() {

		switch (MESSAGE_TO_SEND) {

		case 1:
			controller.setWheelSpeeds(200, 200);
		case 2:
			controller.backward(200);
		case 3:
			controller.floatWheels();
		case 4:
			controller.forward(200);
		case 5:
			controller.forward(200, 200);
		case 6:
			controller.kick();
		case 7:
			controller.penaltyKick();
		case 8:
			controller.penaltyKickStraight();
		case 9:
			controller.rotate(30, 200);
		case 10:
			controller.stop();
		}
	}

	// Command chosen is random instead of pre-defined.
	public static void executeRandomCommand() {
		for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
			MESSAGE_TO_SEND = (int) Math.round(Math.random() * 10);
			executeCommand();
		}
	}

	public boolean isReady() {
		return controller.isReady();
	}

	public void send(int nOfmessages) throws InterruptedException {
		// send specified number of messages
		System.out.printf("Sending : %d messages...\n", NUMBER_OF_MESSAGES);
		for (int i = 0; i < nOfmessages; i++) {
        	executeCommand();
			Thread.sleep(INTERVAL);
        }
	}

	public static void main(String[] args) throws InterruptedException {
        TestReliability test = new TestReliability(new BluetoothController(
				new Communicator2()));
        if (test.isReady()){
        	System.out.println("Ready! Starting to send messages...");
			test.send(NUMBER_OF_MESSAGES);
        }
	}
}
