package balle.strategy;

import org.apache.log4j.Logger;

import balle.controller.Controller;
import balle.strategy.executor.movement.GoToObjectPFN;
import balle.strategy.executor.turning.IncFaceAngle;
import balle.strategy.executor.turning.RotateToOrientationExecutor;
import balle.strategy.planner.AbstractPlanner;
import balle.world.Coord;
import balle.world.Orientation;
import balle.world.Snapshot;
import balle.world.objects.Ball;
import balle.world.objects.Goal;
import balle.world.objects.Robot;


public class M3locateandshoot extends AbstractPlanner {

	private static final Logger LOG = Logger.getLogger(M3locateandshoot.class);

	GoToObjectPFN goto_executor;
	RotateToOrientationExecutor turning_executor;
	Coord startingCoordinate = null;
	Coord currentCoordinate = null;
	Boolean finished = false;

	public M3locateandshoot() {
		goto_executor = new GoToObjectPFN(0);
		turning_executor = new IncFaceAngle();
	}

	@Override
	public void onStep(Controller controller, Snapshot snapshot)
			throws ConfusedException {

		if (finished) {
			return;
		}

		startingCoordinate = snapshot.getBalle().getPosition();
		goto_executor.updateTarget(snapshot.getBall());

		if (goto_executor.isPossible(snapshot)) {

			if (snapshot.getBall().getPosition().dist(startingCoordinate) > 0.17) {
				LOG.info("Navigate to ball");
				goto_executor.step(controller, snapshot);
			} else {
				finished = true;
				if (finished) {
					Robot ourRobot = snapshot.getBalle();
					Ball ball = snapshot.getBall();
					Goal ownGoal = snapshot.getOwnGoal();

						LOG.info("TEST");
						// Kick if we are facing opponents goal
						if (!ourRobot.isFacingGoalHalf(ownGoal)) {
							LOG.info("Kicking the ball");
							controller.kick();
							// Slowly move forward as well in case we're not so
							// close
							controller.setWheelSpeeds(200, 200);
						} else {
							Coord r, b, g;
							r = ourRobot.getPosition();
							b = ball.getPosition();
							g = ownGoal.getPosition();

							if (r.angleBetween(g, b).atan2styleradians() < 0) {
								// Clockwise.
								Orientation orien = ourRobot
										.findMaxRotationMaintaintingPossession(
												ball, true);
								System.out.println(orien);
								turning_executor.setTargetOrientation(orien);
								turning_executor.step(controller, snapshot);

								if (ourRobot
										.findMaxRotationMaintaintingPossession(
												ball, true).degrees() < 10)
									controller.kick();
							} else {
								// Anti-Clockwise
								Orientation orien = ourRobot
										.findMaxRotationMaintaintingPossession(
												ball, false);
								System.out.println(orien);
								turning_executor.setTargetOrientation(orien);

								turning_executor.step(controller, snapshot);

								if (ourRobot
										.findMaxRotationMaintaintingPossession(
												ball, false).degrees() > -10)
									controller.kick();
							}
						}
				} else {
					LOG.info("Not kicking");
				}
			}
		}
		return;

	}

	@FactoryMethod(designator = "M3locateandshoot", parameterNames = {})
	public static final M3locateandshoot factory() {
		return new M3locateandshoot();
	}

}

