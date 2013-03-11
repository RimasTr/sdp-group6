package balle.world.objects;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static com.googlecode.javacv.cpp.opencv_core.cvScalarAll;
import static com.googlecode.javacv.cpp.opencv_core.cvSetIdentity;
import static com.googlecode.javacv.cpp.opencv_video.cvCreateKalman;
import static com.googlecode.javacv.cpp.opencv_video.cvKalmanCorrect;
import static com.googlecode.javacv.cpp.opencv_video.cvKalmanPredict;
import balle.misc.Globals;
import balle.world.AngularVelocity;
import balle.world.Coord;
import balle.world.Line;
import balle.world.Orientation;
import balle.world.Velocity;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_video.CvKalman;

public class Robot extends RectangularObject {

	private CvKalman kalman;
	private CvMat measured;
	private CvMat prediction;
	private CvMat transitionMatrix;
	private boolean useKalman = true;

	public Robot(Coord position, Velocity velocity,
			AngularVelocity angularVelocity, Orientation orientation) {
		
		super(position, velocity, angularVelocity, orientation,
				Globals.ROBOT_WIDTH, Globals.ROBOT_LENGTH);

		kalman = cvCreateKalman(6, 3, 0);
		measured = CvMat.create(3, 1, CV_32FC1);
		prediction = CvMat.create(6, 1, CV_32FC1);

		setProcessNoise(1e-4);
		setMeasurementNoise(1e-3);
		setPostError(1e-1);

		setupTransitionMatrix();

	}

	/**
	 * Set the process noise covariance used by the Kalman filter.
	 * 
	 * @param newProcessNoise
	 *            The new value for the process noise.
	 */
	public void setProcessNoise(double newProcessNoise) {
		cvSetIdentity(kalman.process_noise_cov(), cvScalarAll(newProcessNoise));
	}

	/**
	 * Set the measurement noise used by the Kalman filter.
	 * 
	 * @param newMeasurementNoise
	 *            The new value for the measurement noise.
	 */
	public void setMeasurementNoise(double newMeasurementNoise) {
		cvSetIdentity(kalman.measurement_noise_cov(),
				cvScalarAll(newMeasurementNoise));
	}

	/**
	 * Set the post error covariance used by the Kalman filter.
	 * 
	 * @param newPostError
	 *            The new post error covariance value.
	 */
	public void setPostError(double newPostError) {
		cvSetIdentity(kalman.error_cov_post(), cvScalarAll(newPostError));
	}

	private void setupTransitionMatrix() {

		transitionMatrix = CvMat.create(6, 6, CV_32FC1);
		transitionMatrix.put(0, 0, 1);
		transitionMatrix.put(0, 1, 0);
		transitionMatrix.put(0, 2, 0);
		transitionMatrix.put(0, 3, 1);
		transitionMatrix.put(0, 4, 0);
		transitionMatrix.put(0, 5, 0);

		transitionMatrix.put(1, 0, 0);
		transitionMatrix.put(1, 1, 1);
		transitionMatrix.put(1, 2, 0);
		transitionMatrix.put(1, 3, 0);
		transitionMatrix.put(1, 4, 1);
		transitionMatrix.put(1, 5, 0);

		transitionMatrix.put(2, 0, 0);
		transitionMatrix.put(2, 1, 0);
		transitionMatrix.put(2, 2, 1);
		transitionMatrix.put(2, 3, 0);
		transitionMatrix.put(2, 4, 0);
		transitionMatrix.put(2, 5, 1);

		transitionMatrix.put(3, 0, 0);
		transitionMatrix.put(3, 1, 0);
		transitionMatrix.put(3, 2, 0);
		transitionMatrix.put(3, 3, 1);
		transitionMatrix.put(3, 4, 0);
		transitionMatrix.put(3, 5, 0);

		transitionMatrix.put(4, 0, 0);
		transitionMatrix.put(4, 1, 0);
		transitionMatrix.put(4, 2, 0);
		transitionMatrix.put(4, 3, 0);
		transitionMatrix.put(4, 4, 1);
		transitionMatrix.put(4, 5, 0);

		transitionMatrix.put(5, 0, 0);
		transitionMatrix.put(5, 1, 0);
		transitionMatrix.put(5, 2, 0);
		transitionMatrix.put(5, 3, 0);
		transitionMatrix.put(5, 4, 0);
		transitionMatrix.put(5, 5, 1);

		kalman.transition_matrix(transitionMatrix);
	}

	public void reset(Coord newPosition, Orientation newOrientation) {
		// angular = 0.0;
		// velocity = Vector2.ZERO;
		position = newPosition;
		orientation = newOrientation;

		if (!newPosition.equals(null) && !newOrientation.equals(null)) {
			kalman.state_pre().put(0, position.getX());
			kalman.state_pre().put(1, position.getY());
			kalman.state_pre().put(2, orientation.degrees());
			kalman.state_pre().put(3, 0.0);
			kalman.state_pre().put(4, 0.0);
			kalman.state_pre().put(5, 0.0);

			cvSetIdentity(kalman.measurement_matrix());
		}
	}

	public void update(Coord newPosition, Orientation newOrientation,
			double timeStep) {
		// assert !newPosition.isNaN();
		// assert !Double.isNaN(newRotation);
		assert timeStep > 0.0;

		// if (position.isNaN() || Double.isNaN(rotation)) {
		// reset(newPosition, newRotation);
		// } else
		if (useKalman) {

			cvKalmanPredict(kalman, prediction);

			// newRotation = orientation
			// + MathUtils.angleDiff(MathUtils.capAngle(newRotation),
			// MathUtils.capAngle(rotation));
			// assert Math.abs(orientation - newRotation) <= Math.PI * 2.0 :
			// "rotation = "
			// + rotation + ", newRotation = " + newRotation;

			measured.put(0, newPosition.x);
			measured.put(1, newPosition.y);
			measured.put(2, newOrientation.degrees());

			CvMat estimated = cvKalmanCorrect(kalman, measured);
			position = new Coord(estimated.get(0), estimated.get(1));
			orientation = new Orientation(estimated.get(2));
			velocity = new Velocity(prediction.get(3) / timeStep,
					prediction.get(4) / timeStep, timeStep);
			angularVelocity = new AngularVelocity(prediction.get(5) / timeStep,
					timeStep, false);

			if (angularVelocity.degrees() > Math.PI * 2.0)
				angularVelocity = new AngularVelocity(0.0, timeStep, false);

			if (velocity.getX() > 10.0 || velocity.getY() > 10.0
					|| velocity.getX() < -10.0 || velocity.getY() < -10.0)
				velocity = new Velocity(0.0, 0.0, timeStep);
		} else {
			position = newPosition;
			orientation = newOrientation;

			velocity = new Velocity(newPosition.sub(position).mult(
					1.0 / timeStep), timeStep);
			angularVelocity = new AngularVelocity(
newOrientation.sub(
					orientation).degrees()
					/ timeStep, timeStep, false);
		}
	}

    /**
     * Returns true if the robot is in possession of the ball. That is if the
     * ball is close enough to the kicker that it can kick it.
     * 
     * @param ball
     * @return true, if robot is in possession of the ball
     */
    public boolean possessesBall(Ball ball) {
        if ((ball.getPosition() == null) || (getPosition() == null))
            return false;

        double distance = getFrontSide().dist(ball.getPosition());
        return distance <= Globals.ROBOT_POSSESS_DISTANCE + ball.getRadius();
    }

    /**
     * Returns the line that would represents the path of the ball if the robot
     * kicked it
     * 
     * @param ball
     * @return
     */
    public Line getBallKickLine(Ball ball) {
        double x0, y0, x1, y1;
        x0 = ball.getPosition().getX();
        y0 = ball.getPosition().getY();

        Coord target = new Coord(Globals.ROBOT_MAX_KICK_DISTANCE, 0);
        target = target.rotate(getOrientation());

        x1 = x0 + target.getX();
        y1 = y0 + target.getY();

        return new Line(x0, y0, x1, y1);
    }

    /**
     * Returns that represents the robot's facing direction
     * 
     * @return the facing line
     */
    public Line getFacingLine() {
        double x0, y0, x1, y1;
        x0 = getPosition().getX();
        y0 = getPosition().getY();

        Coord target = new Coord(Globals.PITCH_WIDTH, 0);
        target = target.rotate(getOrientation());

        x1 = x0 + target.getX();
        y1 = y0 + target.getY();

        return new Line(x0, y0, x1, y1);
    }

    /**
     * Gets the facing line of the robot. Similar to the getFacingLine but the
     * line returned stretches both forward and backward from the robot.
     * 
     * @return the facing line both ways
     */
    public Line getFacingLineBothWays() {
        double x0, y0, x1, y1;

        Coord target = new Coord(Globals.PITCH_WIDTH, 0);
        target = target.rotate(getOrientation());

        x0 = getPosition().getX() - target.getX();
        y0 = getPosition().getY() - target.getY();
        x1 = getPosition().getX() + target.getX();
        y1 = getPosition().getY() + target.getY();

        return new Line(x0, y0, x1, y1);
    }

    /**
     * Checks if the robot can score from this position. That is if it is in
     * possession of the ball and facing the goal and other robot is is not
     * blocking the path to the goal.
     * 
     * @param ball
     * @param goal
     * @param otherRobot
     * @return true, if is in scoring position
     */
    public boolean isInScoringPosition(Ball ball, Goal goal, Robot otherRobot) {
        return possessesBall(ball) && isFacingGoal(goal)
                && ((otherRobot.getPosition() != null) && (!otherRobot
                        .intersects(getFacingLine())));
    }

    /**
     * Returns true if robot is facing the goal. Similar to isInScoringPosition
     * but does not check whether a robot has a ball and whether it is blocked
     * by other robot.
     * 
     * @param goal
     * @return
     */
    public boolean isFacingGoal(Goal goal) {

        if (getPosition() == null)
            return false;

        Line goalLine = goal.getGoalLine();
        Line facingLine = getFacingLine();

        return facingLine.intersects(goalLine);

    }

    /**
     * TODO write test
     * 
     * @return True, if robot is facing left.
     */
    public boolean isFacingLeft() {
        if (getOrientation() == null)
            return false;

        return (getOrientation().degrees() >= 90)
                && (getOrientation().degrees() <= 270);
    }

    /**
     * TODO write test
     * 
     * @return True, if the robot is facing right
     */
    public boolean isFacingRight() {
        if (getOrientation() == null)
            return false;

        return (getOrientation().degrees() <= 90)
                || (getOrientation().degrees() >= 270);
    }

    /**
     * Returns true if this robot is facing to the half of the pitch that this
     * goal is present.
     * 
     * TODO write test
     * 
     * @param goal
     * @return
     */
    public boolean isFacingGoalHalf(Goal goal) {
        return isFacingLeft() == goal.isLeftGoal();
    }

    /**
     * Returns the angle required to turn using atan2 style radians. Positive
     * angle means to turn CCW this much radians, whereas negative means turning
     * CW that amount of radians.
     * 
     * @param currentOrientation
     *            current orientation of the robot
     * @param targetOrientation
     *            target orientation
     * @return the angle to turn
     */
    public double getAngleToTurn(Orientation targetOrientation) {
        Orientation currentOr = getOrientation();
        if ((currentOr == null) || (targetOrientation == null))
            return 0;

        double angleToTarget = targetOrientation.atan2styleradians();
        double currentOrientation = currentOr.atan2styleradians();

        double turnLeftAngle, turnRightAngle;
        if (angleToTarget > currentOrientation) {
            turnLeftAngle = angleToTarget - currentOrientation;
            turnRightAngle = currentOrientation + (2 * Math.PI - angleToTarget);
        } else {
            turnLeftAngle = (2 * Math.PI) - currentOrientation + angleToTarget;
            turnRightAngle = currentOrientation - angleToTarget;
        }

        double turnAngle;

        if (turnLeftAngle < turnRightAngle)
            turnAngle = turnLeftAngle;
        else
            turnAngle = -turnRightAngle;

        return turnAngle;
    }

    /**
     * Returns the angle the robot has to turn to face the target coordinate
     * 
     * @param targetCoord
     *            the target coordinate
     * @return the angle to turn to target
     */
    public double getAngleToTurnToTarget(Coord targetCoord) {
        Coord currentPosition = getPosition();
        if ((currentPosition == null) || (targetCoord == null))
            return 0;

        return getAngleToTurn(targetCoord.sub(currentPosition).orientation());
    }

    public Coord getFrontLeftCornerCoord() {
        Coord leftSide = new Coord(getHeight() / 2, -getWidth() / 2);
        leftSide = leftSide.rotate(getOrientation());
        return getPosition().add(leftSide);
    }

    public Coord getFrontRightCornerCoord() {
        Coord rightSide = new Coord(getHeight() / 2, getWidth() / 2);
        rightSide = rightSide.rotate(getOrientation());
        return getPosition().add(rightSide);
    }

    public boolean canReachTargetInStraightLine(FieldObject target, FieldObject obstacle) {
        if (getPosition() == null)
            return false;

        Line pathToTarget1 = new Line(getPosition(), target.getPosition());
        Line pathToTarget2 = new Line(getFrontLeftCornerCoord(),
                target.getPosition());
        Line pathToTarget3 = new Line(getFrontRightCornerCoord(),
                target.getPosition());

        // Check if it is blocking our path
        return (!obstacle.intersects(pathToTarget1)
                && !obstacle.intersects(pathToTarget2) && !obstacle
                .intersects(pathToTarget3));
    }

	public boolean isApproachingTargetFromCorrectSide(FieldObject target,
			Goal opponentsGoal) {
		return isApproachingTargetFromCorrectSide(target, opponentsGoal,
				Globals.OVERSHOOT_ANGLE_EPSILON);
	}

    public boolean isApproachingTargetFromCorrectSide(FieldObject target,
			Goal opponentsGoal, double overshootAngleEpsilon) {

        if (getPosition() == null)
            return false;
        if (target.getPosition() == null)
            return true;

        Orientation robotToTargetOrientation = target.getPosition()
                .sub(getPosition()).orientation();

        if (opponentsGoal.isLeftGoal()
				&& (robotToTargetOrientation.degrees() > 90 + overshootAngleEpsilon)
				&& (robotToTargetOrientation.degrees() < 270 - overshootAngleEpsilon)) {
            return true;
        } else if ((opponentsGoal.isRightGoal())
				&& ((robotToTargetOrientation.degrees() < 90 - overshootAngleEpsilon) || (robotToTargetOrientation
						.degrees() > 270 + overshootAngleEpsilon))) {
            return true;
        } else
            return false;

    }

    /**
     * TODO TEST!!!!!!
     * 
     * @param ourRobot
     * @param ball
     * @param cw
     *            Clock-wise rotation if true, CCW if false.
     * @return
     */
    public Orientation findMaxRotationMaintaintingPossession(Ball ball,
            boolean cw) {
        Coord fl = new Coord(10, 0);
        fl = fl.rotate(getOrientation()).add(getPosition());
        Orientation max, o = getPosition().angleBetween(fl, ball.getPosition());

        if (cw) {
            max = (new Coord(0, 0)).angleBetween(new Coord(10, 0), new Coord(
                    Globals.ROBOT_LENGTH, Globals.ROBOT_WIDTH));
        } else {
            max = (new Coord(0, 0)).angleBetween(new Coord(10, 0), new Coord(
                    Globals.ROBOT_LENGTH, -Globals.ROBOT_WIDTH));
        }
        // System.out.println("max = " + max + ",\to = " + o + ",\tm-o = "
        // + max.sub(o));

        return max.sub(o);

    }

	public static final Coord relL = new Coord(-Globals.ROBOT_TRACK_WIDTH / 2,
			0);
	public static final Coord relR = new Coord(Globals.ROBOT_TRACK_WIDTH / 2, 0);

	private double helperWheelSpeed(Coord rel) {
		Orientation dAng = new Orientation( getAngularVelocity().radians() );
		
		Coord s, f, delta;
		s = rel;
		f = getVelocity().add(rel.rotate(dAng));
		delta = f.sub(s);

		double cst = Math.PI - 2;
		double var = dAng.radians() / Math.PI;
		double abs = delta.abs();

		double out = (abs + abs * var * cst) / Globals.MAX_WHEEL_SPEED;

		if (delta.getY() < 0)
			return out;
		else
			return out;
	}

	public double getLeftWheelSpeed() {
		return helperWheelSpeed(relL);
	}

	public double getRightWheelSpeed() {
		return helperWheelSpeed(relR);
	}
}
