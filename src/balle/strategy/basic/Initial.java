package balle.strategy.basic;

import org.apache.log4j.Logger;

import balle.controller.Controller;
import balle.strategy.ConfusedException;
import balle.strategy.FactoryMethod;
import balle.strategy.executor.movement.GoToObject;
import balle.strategy.executor.turning.FaceAngle;
import balle.strategy.planner.AbstractPlanner;
import balle.world.Snapshot;
import balle.world.objects.Ball;
import balle.world.objects.Robot;

public class Initial extends AbstractPlanner {

	private static final Logger LOG = Logger.getLogger(Initial.class);

	GoToObject goto_executor;
	FaceAngle turning_executor;
	Boolean finished = false;
	Boolean arrived_at_ball = false;

	public Initial() {
		turning_executor = new FaceAngle();
		goto_executor = new GoToObject(turning_executor);
	}

	@Override
	public void onStep(Controller controller, Snapshot snapshot) throws ConfusedException {

		Robot ourRobot = snapshot.getBalle();
		Robot oppRobot = snapshot.getOpponent();
		Ball ball = snapshot.getBall();

		if (finished || ourRobot.getPosition() == null || oppRobot.getPosition() == null || ball.getPosition() == null) {
			return;
		}

		if (oppRobot.possessesBall(ball) || ourRobot.getPosition().dist(ball.getPosition()) < 0.4) {

			goto_executor.stop(controller);
			finished = true;

			if (oppRobot.possessesBall(ball)) {
				LOG.info("Opponent has ball: Stopping.");
			} else {
				LOG.info("Close to ball: Stopping.");
			}

			return;

		} else {
			goto_executor.step(controller, snapshot);
			return;
		}

	}

	@Override
	public void stop(Controller controller) {
		goto_executor.stop(controller);
		turning_executor.stop(controller);
	}

	@FactoryMethod(designator = "Initial", parameterNames = {})
	public static final Initial factory() {
		return new Initial();
	}

}
