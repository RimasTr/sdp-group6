package balle.strategy;

import org.apache.log4j.Logger;

import balle.controller.Controller;
import balle.strategy.executor.dribbling.DribbleStraight;
import balle.strategy.executor.movement.GoToObjectPFN;
import balle.strategy.planner.AbstractPlanner;
import balle.world.Coord;
import balle.world.Snapshot;

public class M2dribble extends AbstractPlanner {

	private static final Logger LOG = Logger.getLogger(M2dribble.class);

	DribbleStraight dribble_executor;
	GoToObjectPFN goto_executor;
	Coord startingCoordinate = null;
	Coord currentCoordinate = null;
	Boolean finished = false;
	private static final double DISTANCE_TO_TRAVEL = 0.3; // in metres

	public M2dribble() {
		dribble_executor = new DribbleStraight();
		goto_executor = new GoToObjectPFN(0);
	}


	@Override
	public void onStep(Controller controller, Snapshot snapshot)
			throws ConfusedException {

		if (finished) {
			return;
		}

		if(dribble_executor.isPossible(snapshot)){
			if(startingCoordinate == null){
				startingCoordinate = snapshot.getBalle().getPosition();
			}

			currentCoordinate = snapshot.getBalle().getPosition();
			LOG.info(currentCoordinate.dist(startingCoordinate));
			if (currentCoordinate.dist(startingCoordinate) < DISTANCE_TO_TRAVEL) {
				LOG.info("Beginning dribbling...");
				dribble_executor.step(controller, snapshot);
			} else {
				finished = true;
				LOG.info("Finished.");
				controller.stop();
			}

		}

		else {

			LOG.info("Ball is too far away, moving closer");
			goto_executor.updateTarget(snapshot.getBall());
			if(goto_executor.isPossible(snapshot)){
				goto_executor.step(controller, snapshot);
			}

			else {
				LOG.info("Fail");
				return;
			}

		}
		return;

	}

	@FactoryMethod(designator = "M2dribble", parameterNames = {})
	public static final M2dribble factory() {
		return new M2dribble();
	}

}

