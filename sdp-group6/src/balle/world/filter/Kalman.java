package balle.world.filter;

import java.util.Random;

import Jama.Matrix;
import balle.world.Coord;
import balle.world.Snapshot;
import balle.world.Velocity;
import balle.world.objects.Robot;

public class Kalman implements Filter {

	static Matrix X;
	static Matrix Xk1;
	static Matrix P = new Matrix(new double[][] { { 5 * 5.0, 0, 0, 0 },
			{ 0, 5 * 5.0, 0, 0 }, { 0, 0, 1.0, 0 }, { 0, 0, 0, 1.0 } });
	static Matrix Pk1;
	static Matrix Zk1;
	static Matrix Vk;
	static Matrix Gain;
	static Matrix Sk1;

	double t = 0;
	double phi = Math.toRadians(-180);
	double w = Math.toRadians(20.0f);

	/* TODO: set these values appropriately */
	static double deltaT = 0.1;

	static double rangeSensorNoise = 0.3f;
	static double bearingSensorNoise = Math.toRadians(15.0f);

	static double transitionModelSTDxy = 0.03f;
	static double transitionModelSTDvxy = 0.20f;

	static Matrix F = new Matrix(new double[][] { { 1, 0, deltaT, 0 },
			{ 0, 1, 0, deltaT }, { 0, 0, 1, 0 }, { 0, 0, 0, 1 } });
	static Matrix B, U;
	static Matrix Q = new Matrix(new double[][] {
			{ transitionModelSTDxy * transitionModelSTDxy, 0, 0, 0 },
			{ 0, transitionModelSTDxy * transitionModelSTDxy, 0, 0 },
			{ 0, 0, transitionModelSTDvxy * transitionModelSTDvxy, 0 },
			{ 0, 0, 0, transitionModelSTDvxy * transitionModelSTDvxy } });
	static Matrix H;
	static Matrix R = new Matrix(new double[][]{
			 {rangeSensorNoise*rangeSensorNoise, 0},
			 {0, bearingSensorNoise*bearingSensorNoise}});

	Random sensorNoise = new Random();

	public void predict() {
		Xk1 = F.times(X).plus(B.times(U));
		Pk1 = F.times(P).times(F.transpose()).plus(Q);
	}

	public void observation(Matrix z) {
		Vk = z.minus(H.times(Xk1));
	}

	public void update() {

		// innovation covariance
		Sk1 = H.times(Pk1).times(H.transpose()).plus(R);

		// compute kalman gain
		Gain = Pk1.times(H.transpose()).times(Sk1.inverse());

		// update state estimate via Z
		X = Xk1.plus(Gain.times(Vk));

		// update error covariance
		Matrix I = Matrix.identity(Pk1.getRowDimension(),
				Pk1.getColumnDimension());
		P = (I.minus(Gain.times(H))).times(Pk1);
	}


	public Snapshot filter(Snapshot s) {

		Robot robot = s.getBalle();

		double x = robot.getPosition().x;
		double y = robot.getPosition().y;

		double vx = robot.getVelocity().x;
		double vy = robot.getVelocity().y;

		// Kalman kalman = new Kalman();
		
		X = new Matrix(new double[][] { { x }, { y }, { vx }, { vy } });
		B = new Matrix(new double[][] { { vx, vy, 0, 0 } }).transpose();

		double sqxy = (x * x + y * y);
		double term00 = -y / sqxy;
		double term01 = 1 / (x * (1 + (y / x * y / x)));
		double term10 = x / Math.sqrt(sqxy);
		double term11 = y / Math.sqrt(sqxy);

		H = new Matrix(new double[][] { { term00, term01, 0, 0 },
				{ term10, term11, 0, 0 } });

		double realBearing = Math.atan2(y, x);
		double obsBearing = realBearing + bearingSensorNoise
				* sensorNoise.nextGaussian();

		double realRange = Math.sqrt(sqxy);
		double obsRange = Math.max(0.0, realRange + rangeSensorNoise
				* sensorNoise.nextGaussian());

		Matrix z = new Matrix(new double[][] { { obsRange }, { obsBearing } });

		observation(z);
		update();

		x += vx + deltaT * (Math.cos(phi) - Math.sin(phi));
		y += vy + deltaT * (Math.cos(phi) + Math.sin(phi));
		phi += w * deltaT;

		vx += 1.0 * deltaT * Math.cos(t);
		vy += 1.0 * deltaT * Math.cos(t);
		w -= 0.1 * deltaT * Math.sin(t);

		t += deltaT;

		Robot updatedRobot = new Robot(new Coord(x, y), new Velocity(vx, vy,
				deltaT), robot.getAngularVelocity(), robot.getOrientation());
		Snapshot updatedSnapshot = new Snapshot(s.getWorld(), s.getOpponent(),
				updatedRobot, s.getBall(), s.getTimestamp(),
				s.getControllerHistory());
		return updatedSnapshot;
	}

}
