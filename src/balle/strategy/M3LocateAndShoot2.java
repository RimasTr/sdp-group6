package balle.strategy;

import org.apache.log4j.Logger;

import balle.controller.Controller;
import balle.strategy.planner.AbstractPlanner;
import balle.strategy.planner.GoToBallSafeProportional;
import balle.world.Coord;
import balle.world.Snapshot;
import balle.world.objects.Robot;

public class M3LocateAndShoot2 extends AbstractPlanner {

	private static final Logger LOG = Logger.getLogger(M3LocateAndShoot2.class);

	Milestone2Dribble dribble_executor;
	// DribbleStraight at_ball;
	GoToBallSafeProportional goto_executor;
	Coord startingCoordinate = null;
	Coord currentCoordinate = null;
	Boolean finished = false;
	Boolean arrived = true;

	private static final double MIN_DIST_TO_GOAL = 1.0; // in metres

	public M3LocateAndShoot2() {
		dribble_executor = new Milestone2Dribble();
		goto_executor = new GoToBallSafeProportional();
	}

	@Override
	public void onStep(Controller controller, Snapshot snapshot)
			throws ConfusedException {

		Robot ourRobot = snapshot.getBalle();

		if (finished) {
			return;
		}


		if (ourRobot.possessesBall(snapshot.getBall())
				&& !(ourRobot.getPosition() == null)) {

			LOG.info("At the ball, dribbling");
			currentCoordinate = snapshot.getBalle().getPosition();
			LOG.info(currentCoordinate.dist(snapshot.getOpponentsGoal()
					.getPosition()));

			if (currentCoordinate.dist(snapshot.getOpponentsGoal()
					.getPosition()) <= MIN_DIST_TO_GOAL) {
				controller.kick();
				LOG.info("Kicked!");
				dribble_executor.stop(controller);
				controller.stop();
				finished = true;
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

	}

	@FactoryMethod(designator = "M3LocandShoot", parameterNames = {})
	public static final M3LocateAndShoot2 factory() {
		return new M3LocateAndShoot2();
	}

}
