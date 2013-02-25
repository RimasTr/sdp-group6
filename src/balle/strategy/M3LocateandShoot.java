package balle.strategy;

import org.apache.log4j.Logger;

import balle.controller.Controller;
import balle.strategy.planner.AbstractPlanner;
import balle.strategy.planner.GoToBallSafeProportional;
import balle.world.Coord;
import balle.world.Snapshot;
import balle.world.objects.Robot;

public class M3LocateandShoot extends AbstractPlanner {

	private static final Logger LOG = Logger.getLogger(M3LocateandShoot.class);

	Dribble dribble_executor;
	// DribbleStraight at_ball;
	GoToBallSafeProportional goto_executor;
	Coord startingCoordinate = null;
	Coord currentCoordinate = null;
	Boolean dribbling_finished = false;
	Boolean finished = false;
	Boolean arrived = true;

	// private static final double DISTANCE_TO_TRAVEL = 0.3; // in metres

	public M3LocateandShoot() {
		dribble_executor = new Dribble();
		// at_ball = new DribbleStraight();
		goto_executor = new GoToBallSafeProportional();
	}

	@Override
	public void onStep(Controller controller, Snapshot snapshot)
			throws ConfusedException {

		Robot ourRobot = snapshot.getBalle();
		// Goal oppGoal = snapshot.getOpponentsGoal();

		if (finished) {
			return;
		}

		if (ourRobot.possessesBall(snapshot.getBall())
				&& !(ourRobot.getPosition() == null)) {

			if (!arrived) {
				LOG.info("Arrived at ball, beginning dribble");
				arrived = true;
			}

			goto_executor.stop(controller);

			if (dribble_executor.hasKicked()) {
				dribble_executor.stop(controller);
				controller.stop();
				finished = true;
			}

			else {
				dribble_executor.step(controller, snapshot);
				return;
			}
		} else {
			goto_executor.step(controller, snapshot);
			return;
		}

	}

	@FactoryMethod(designator = "M3LocandShoot", parameterNames = {})
	public static final M3LocateandShoot factory() {
		return new M3LocateandShoot();
	}

}