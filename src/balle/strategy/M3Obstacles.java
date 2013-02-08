package balle.strategy;

import org.apache.log4j.Logger;

import balle.controller.Controller;
import balle.strategy.executor.movement.GoToObjectPFN;
import balle.strategy.planner.AbstractPlanner;
import balle.world.Coord;
import balle.world.Snapshot;

//Use vision and touch sensors to respond appropriately to obstacles. When set
//rolling toward another robot from more than 30cm away, your robot should
//avoid the other robot. Once the start point is too close for visual avoidance,
//your robot should back off if it contacts the other robot.

public class M3Obstacles extends AbstractPlanner {

	private static final Logger LOG = Logger.getLogger(M3Obstacles.class);

	GoToObjectPFN goto_executor;
	Coord startingCoordinate = null;

	public M3Obstacles() {
		goto_executor = new GoToObjectPFN(0);
	}

	@Override
	public void onStep(Controller controller, Snapshot snapshot)
			throws ConfusedException {


		startingCoordinate = snapshot.getBalle().getPosition();
		goto_executor.updateTarget(snapshot.getOpponent());

		if (goto_executor.isPossible(snapshot)) {

			if (snapshot.getOpponent().getPosition().dist(startingCoordinate) > 30) {
				LOG.info("Rolling forward");
				goto_executor.step(controller, snapshot);
			} else {
				LOG.info("Cannot proceed any further");
				controller.stop();
			}
		}

		return;
	}

	@FactoryMethod(designator = "M3Avoid", parameterNames = {})
	public static final M3Obstacles factory() {
		return new M3Obstacles();
	}

}
