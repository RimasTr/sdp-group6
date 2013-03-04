package balle.brick;

import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import balle.controller.Controller;
import balle.controller.ControllerListener;

/**
 * The Control class. Handles the actual driving and movement of the bot, once
 * BotCommunication has processed the commands.
 * 
 * That is -- defines the behaviour of the bot when it receives the command.
 * 
 * Adapted from SDP2011 groups 10 code -- original author shearn89
 * 
 * @author sauliusl
 */
public class BrickController implements Controller {
	DifferentialPilot pilot;
	public int maxPilotSpeed = 200; // 20
                                    // for
                                    // friendlies

	public final NXTRegulatedMotor LEFT_WHEEL = new NXTRegulatedMotor(
			MotorPort.B);
	public final NXTRegulatedMotor RIGHT_WHEEL = new NXTRegulatedMotor(
			MotorPort.C);
	public final NXTRegulatedMotor KICKER = new NXTRegulatedMotor(MotorPort.A);

	public final boolean INVERSE_WHEELS = false;

	public final float WHEEL_DIAMETER = 81.6f; // mm
	public final float TRACK_WIDTH = 128f; // mm

    public static final int MAXIMUM_MOTOR_SPEED = 900;

    public static final int GEAR_ERROR_RATIO = 2; // Gears cut our turns in half

    private volatile boolean isKicking = false;

    public BrickController() {

		pilot = new DifferentialPilot(WHEEL_DIAMETER, TRACK_WIDTH, LEFT_WHEEL,
				RIGHT_WHEEL, INVERSE_WHEELS);
        pilot.setTravelSpeed(maxPilotSpeed);
		pilot.setRotateSpeed(45);
		// pilot.setAcceleration(250);

    }

    /*
     * (non-Javadoc)
     * 
     * @see balle.brick.Controller#floatWheels()
     */
    @Override
    public void floatWheels() {
        LEFT_WHEEL.flt();
        RIGHT_WHEEL.flt();
    }

    /*
     * (non-Javadoc)
     * 
     * @see balle.brick.Controller#stop()
     */
    @Override
    public void stop() {
        pilot.stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see balle.brick.Controller#kick()
     */
    @Override
    public void kick() {

        if (isKicking) {
            return;
        }

		// KICKER.flt();

        isKicking = true;

		KICKER.setSpeed(900);

		// Move kicker back
		// KICKER.rotateTo(-5);
		// KICKER.waitComplete();

		// Kick
		KICKER.rotateTo(45);
		KICKER.waitComplete();

		// Reset
		KICKER.rotateTo(-45);
		KICKER.waitComplete();

		// TODO: this reduces power of the kick - only use for Milestones!
		// KICKER.flt();

		isKicking = false;
    }

    public void gentleKick(int speed, int angle) {
        KICKER.setSpeed(speed);
        KICKER.resetTachoCount();
		KICKER.rotateTo(-10);
		KICKER.rotateTo(angle);
		KICKER.rotateTo(0);
    }

    public float getTravelDistance() {
        return pilot.getMovementIncrement();
    }

    public void reset() {
        pilot.reset();
    }

	private void setMotorSpeed(NXTRegulatedMotor motor, int speed) {
        boolean forward = true;
        if (speed < 0) {
            forward = false;
            speed = -1 * speed;
        }

		motor.setSpeed(speed);
        if (forward)
			motor.forward();
        else
			motor.backward();
    }

    @Override
    public void setWheelSpeeds(int leftWheelSpeed, int rightWheelSpeed) {
        if (leftWheelSpeed > MAXIMUM_MOTOR_SPEED)
            leftWheelSpeed = MAXIMUM_MOTOR_SPEED;
        if (rightWheelSpeed > MAXIMUM_MOTOR_SPEED)
            rightWheelSpeed = MAXIMUM_MOTOR_SPEED;

        if (INVERSE_WHEELS) {
            leftWheelSpeed *= -1;
            rightWheelSpeed *= -1;
        }
        setMotorSpeed(LEFT_WHEEL, leftWheelSpeed);
        setMotorSpeed(RIGHT_WHEEL, rightWheelSpeed);
    }

    @Override
    public int getMaximumWheelSpeed() {
        return MAXIMUM_MOTOR_SPEED;
    }

    @Override
    public void backward(int speed) {
        pilot.setTravelSpeed(speed);
        pilot.backward();
    }

    @Override
    public void forward(int speed) {
        pilot.setTravelSpeed(speed);
        pilot.forward();

    }

	public void forward(int left, int right) {
		LEFT_WHEEL.setSpeed(left);
		RIGHT_WHEEL.setSpeed(right);
		pilot.forward();
	}

    @Override
    public void rotate(int deg, int speed) {
        pilot.setRotateSpeed(speed);
        pilot.rotate(deg / GEAR_ERROR_RATIO);
    }

    @Override
    public void penaltyKick() {
        int turnAmount = 27;
        if (Math.random() <= 0.5)
            turnAmount *= -1;
        rotate(turnAmount, 180);
        kick();

    }

    @Override
    public boolean isReady() {
        return true;
    }

	@Override
	public void addListener(ControllerListener cl) {
		// TODO make STUB
	}

}
