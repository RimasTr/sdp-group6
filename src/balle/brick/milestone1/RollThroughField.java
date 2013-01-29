package balle.brick.milestone1;

import lejos.nxt.LCD;
import balle.brick.BrickController;

public class RollThroughField {
	
	private static final int ROLL_SPEED = 400;
	private static final double ROLL_DISTANCE = 1.0;

    private static void drawMessage(String message) {
        LCD.clear();
        LCD.drawString(message, 0, 0);
        LCD.refresh();
    }

    public static void main(String[] args) {

        BrickController controller = new BrickController();

        boolean movingForward = false;
        int speed = ROLL_SPEED;
		double rollDistance = ROLL_DISTANCE;
        while (true) {
            if (!movingForward) {
                drawMessage("Roll");
                movingForward = true;
                controller.reset();
                controller.forward(speed);
                
            } else {
            	float distance = controller.getTravelDistance();
				if (distance > rollDistance) {
                	controller.stop();
					break;
                } else {
                    drawMessage(Float.toString(distance));
                }
            }
        }
    }
}
