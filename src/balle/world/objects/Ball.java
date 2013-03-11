package balle.world.objects;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static com.googlecode.javacv.cpp.opencv_core.cvScalarAll;
import static com.googlecode.javacv.cpp.opencv_core.cvSetIdentity;
import static com.googlecode.javacv.cpp.opencv_video.cvCreateKalman;
import static com.googlecode.javacv.cpp.opencv_video.cvKalmanCorrect;
import static com.googlecode.javacv.cpp.opencv_video.cvKalmanPredict;
import balle.misc.Globals;
import balle.world.Coord;
import balle.world.Velocity;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_video.CvKalman;

public class Ball extends CircularObject implements FieldObject {

	// private final BallPredictor predictor;

	private CvKalman kalman;
	private CvMat measured;
	private CvMat prediction;
	private CvMat transitionMatrix;
	private boolean useKalman = true;

	public Ball(Coord position, Velocity velocity) {

		super(position, velocity, Globals.BALL_RADIUS);

		assert position != null;
		kalman = cvCreateKalman(4, 2, 0);
		measured = CvMat.create(2, 1, CV_32FC1);
		prediction = CvMat.create(4, 1, CV_32FC1);

		setProcessNoise(1e-5);
		setMeasurementNoise(1e-4);
		setPostError(1e-1);

		setupTransitionMatrix();

		reset(position);

		// this.predictor = predictor;
	}

	// public Ball(Coord position, Velocity velocity, BallPredictor predictor) {
	// super(position, velocity, Globals.BALL_RADIUS);
	//
	// this.predictor = predictor;
	// }

	// public Coord estimatePosition(double time) {
	// return predictor.getPosition(time);
	// }

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

	public void update(Coord newPosition, double timeStep) {
		assert timeStep > 0.0;

		if (newPosition != null) {
			assert newPosition.equals(null);

			if (position.equals(null))
				reset(newPosition);
			else

				if (useKalman) {
				prediction = cvKalmanPredict(kalman, prediction);

				measured.put(0, newPosition.getX());
				measured.put(1, newPosition.getY());

				CvMat estimated = cvKalmanCorrect(kalman, measured);

				position = new Coord(estimated.get(0), estimated.get(1));
				velocity = new Velocity(prediction.get(2) / timeStep,
						prediction.get(3) / timeStep, timeStep);
			} else {
				velocity = new Velocity(newPosition.sub(position).mult(
						1.0 / timeStep), timeStep);
				position = newPosition;
			}
		} else {
			// ++undetectedFrameCount;

			if (useKalman) {
				prediction = cvKalmanPredict(kalman, prediction);
				position = new Coord(prediction.get(0), prediction.get(1));
				velocity = new Velocity(prediction.get(2) / timeStep,
						prediction.get(3) / timeStep, timeStep);

			} else {
				position = position.add(velocity.mult(timeStep));
			}
		}

		velocity = velocity.mult(10);
	}

	public void reset(Coord newPosition) {
		assert newPosition != null;

		position = newPosition;
		// velocity = new Vector2( 0.0, 0.0 );
		velocity = velocity.mult(0);

		if (!newPosition.equals(null)) {
			kalman.state_pre().put(0, newPosition.getX());
			kalman.state_pre().put(1, newPosition.getY());
			kalman.state_pre().put(2, 0.0);
			kalman.state_pre().put(3, 0.0);
			cvSetIdentity(kalman.measurement_matrix());
			System.out.println("BallState.reset()");
		}
	}

	private void setupTransitionMatrix() {
		transitionMatrix = CvMat.create(4, 4, CV_32FC1);
		transitionMatrix.put(0, 0, 1);
		transitionMatrix.put(0, 1, 0);
		transitionMatrix.put(0, 2, 1);
		transitionMatrix.put(0, 3, 0);

		transitionMatrix.put(1, 0, 0);
		transitionMatrix.put(1, 1, 1);
		transitionMatrix.put(1, 2, 0);
		transitionMatrix.put(1, 3, 1);

		transitionMatrix.put(2, 0, 0);
		transitionMatrix.put(2, 1, 0);
		transitionMatrix.put(2, 2, 1);
		transitionMatrix.put(2, 3, 0);

		transitionMatrix.put(3, 0, 0);
		transitionMatrix.put(3, 1, 0);
		transitionMatrix.put(3, 2, 0);
		transitionMatrix.put(3, 3, 1);

		kalman.transition_matrix(transitionMatrix);
	}

}
