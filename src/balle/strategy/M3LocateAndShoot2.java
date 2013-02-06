package balle.strategy;

import org.apache.log4j.Logger;

import balle.controller.Controller;
import balle.strategy.planner.AbstractPlanner;
import balle.strategy.planner.GoToBallSafeProportional;
import balle.world.Coord;
import balle.world.Snapshot;
import balle.world.objects.Goal;
import balle.world.objects.Robot;

public class M3LocateAndShoot2 extends AbstractPlanner {

	private static final Logger LOG = Logger.getLogger(M3LocateAndShoot2.class);

	Dribble dribble_executor;
	// DribbleStraight at_ball;
	GoToBallSafeProportional goto_executor;
	Coord startingCoordinate = null;
	Coord currentCoordinate = null;
	Boolean dribbling_finished = false;
	Boolean kicking_finished = false;

	// private static final double DISTANCE_TO_TRAVEL = 0.3; // in metres

	public M3LocateAndShoot2() {
		dribble_executor = new Dribble();
		// at_ball = new DribbleStraight();
		goto_executor = new GoToBallSafeProportional();
	}

	@Override
	public void onStep(Controller controller, Snapshot snapshot)
			throws ConfusedException {

		Robot ourRobot = snapshot.getBalle();
		Goal oppGoal = snapshot.getOpponentsGoal();

		if (kicking_finished) {
			controller.stop();
			return;
		}

		if (ourRobot.possessesBall(snapshot.getBall())
				&& !(ourRobot.getPosition() == null)) {

			if (ourRobot.getPosition().dist(oppGoal.getPosition()) <= 0.5) {
				controller.kick();
				LOG.info("Kicking");
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				LOG.info("Kicking finished");
				kicking_finished = true;
				return;
			}

			else {
				dribble_executor.step(controller, snapshot);
				return;
			}
		} else {
			goto_executor.step(controller, snapshot);
			return;
		}

		// goto_executor.
		//
		// if (kicking_finished) {
		// return;
		// }
		//
		// if (dribbling_finished) {
		// controller.kick();
		// LOG.info("Kicking");
		// try {
		// Thread.sleep(1000);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// LOG.info("Kicking finished");
		// kicking_finished = true;
		// return;
		// }
		//
		// if (dribble_executor.isPossible(snapshot)) {
		//
		// if (startingCoordinate == null) {
		// startingCoordinate = snapshot.getBalle().getPosition();
		// LOG.info("Beginning dribbling...");
		// }
		//
		// currentCoordinate = snapshot.getBalle().getPosition();
		// LOG.info(currentCoordinate.dist(startingCoordinate));
		// if (currentCoordinate.dist(startingCoordinate) < DISTANCE_TO_TRAVEL)
		// {
		// LOG.info("Still dribbling, "
		// + (DISTANCE_TO_TRAVEL - currentCoordinate
		// .dist(startingCoordinate)) + " to go");
		// dribble_executor.step(controller, snapshot);
		// } else {
		// dribbling_finished = true;
		// LOG.info("Finished dribbling.");
		// controller.stop();
		// }
		//
		// }
		//
		// else {
		//
		// LOG.info("Ball is too far away, moving closer");
		// goto_executor.updateTarget(snapshot.getBall());
		// if (goto_executor.isPossible(snapshot)) {
		// goto_executor.step(controller, snapshot);
		// }
		//
		// else {
		// LOG.info("Fail");
		// return;
		// }
		//
		// }
		// return;

	}

	@FactoryMethod(designator = "M3LocandShoot", parameterNames = {})
	public static final M3LocateAndShoot2 factory() {
		return new M3LocateAndShoot2();
	}

}
