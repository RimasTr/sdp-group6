package balle.strategy;

import java.awt.Color;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import balle.controller.Controller;
import balle.main.drawable.Drawable;
import balle.main.drawable.DrawableLine;
import balle.main.drawable.DrawableRectangularObject;
import balle.main.drawable.Label;
import balle.misc.Globals;
import balle.simulator.SnapshotPredictor;
import balle.strategy.basic.Initial;
import balle.strategy.planner.AbstractPlanner;
import balle.strategy.planner.GoToBallSafeProportional;
import balle.strategy.planner.GoToGoalSafeProportional;
import balle.world.Coord;
import balle.world.Snapshot;
import balle.world.objects.Ball;
import balle.world.objects.Goal;
import balle.world.objects.RectangularObject;
import balle.world.objects.Robot;

	
public class Game2 extends AbstractPlanner {

    private static final Logger LOG = Logger.getLogger(Game2.class);

	protected final Strategy defend;
	protected final Strategy goToGoal;
	protected final GoToGoalSafeProportional dribbleAndShoot;
	protected final Strategy goToBall;
	protected Strategy initialStrategy;

    protected boolean initial;

    private String currentStrategy = null;

	@FactoryMethod(designator = "Game2", parameterNames = { "init" })
	public static Game2 gameFactoryTesting2(boolean init) {
		Game2 game = new Game2(init);
		return game;
	}

    public String getCurrentStrategy() {
        return currentStrategy;
    }

    public Strategy getInitialStrategy() {
        return initialStrategy;
    }

    public void setInitialStrategy(Strategy initialStrategy) {
        this.initialStrategy = initialStrategy;
    }

    @Override
    public ArrayList<Drawable> getDrawables() {
        ArrayList<Drawable> drawables = super.getDrawables();
        if (currentStrategy != null)
            drawables.add(new Label(currentStrategy, new Coord(
                    Globals.PITCH_WIDTH - 0.5, Globals.PITCH_HEIGHT + 0.2),
                    Color.WHITE));
        return drawables;
    }

    public void setCurrentStrategy(String currentStrategy) {
        this.currentStrategy = currentStrategy;
    }

    public Game2() {
		initialStrategy = new Initial();

		goToBall = new GoToBallSafeProportional();
		dribbleAndShoot = new GoToGoalSafeProportional();

		defend = new GoToBallSafeProportional();
		goToGoal = new GoToGoalSafeProportional();

		initial = false;
    }

    public boolean isInitial(Snapshot snapshot) {
        if (initial == false)
            return false;

        Ball ball = snapshot.getBall();

        Coord centerOfPitch = new Coord(Globals.PITCH_WIDTH / 2,
                Globals.PITCH_HEIGHT / 2);
        Robot ourRobot = snapshot.getBalle();

        // If we have the ball, turn off initial strategy
		if (ourRobot.possessesBall(ball))
        {
            LOG.info("We possess the ball. Turning off initial strategy");
            setInitial(false);
        }
        // else If ball has moved 5 cm, turn off initial strategy
        else if (ball.getPosition().dist(centerOfPitch) > 0.05) {
            LOG.info("Ball has moved. Turning off initial strategy");
            setInitial(false);
        }
        
        return initial;

    }

    public void setInitial(boolean initial) {
        this.initial = initial;
    }

    public Game2(boolean startWithInitial) {
        this();
        initial = startWithInitial;
        LOG.info("Starting game strategy with initial strategy turned on");
    }

    @Override
    public void stop(Controller controller) {

		dribbleAndShoot.stop(controller);
		initialStrategy.stop(controller);
		goToBall.stop(controller);

	}

    @Override
    public void onStep(Controller controller, Snapshot snapshot)
            throws ConfusedException {


        if (isInitial(snapshot)) {
            setCurrentStrategy(initialStrategy.getClass().getName());

            initialStrategy.step(controller, snapshot);

            addDrawables(initialStrategy.getDrawables());
            return;
        }

		Strategy strategy = getStrategy(snapshot);
        LOG.debug("Selected strategy: " + strategy.getClass().getName());
		setCurrentStrategy(strategy.getClass().getName());
        try {
            strategy.step(controller, snapshot);
        } catch (ConfusedException e) {
            // If a strategy does not know what to do
			LOG.error("Game catch block in: " + getCurrentStrategy(), e);
			System.out.println("Error in: " + getCurrentStrategy());
			// Default to goToBall
			goToBall.step(controller, snapshot);
        }

		addDrawables(strategy.getDrawables());
    }

	private Strategy getStrategy(Snapshot snapshot) {
		Robot ourRobot = snapshot.getBalle();
		Robot oppRobot = snapshot.getOpponent();
		Goal ourGoal = snapshot.getOwnGoal();
		Goal oppGoal = snapshot.getOpponentsGoal();
		Ball ball = snapshot.getBall();

		// Get predicted snapshot
        SnapshotPredictor sp = snapshot.getSnapshotPredictor();
        Snapshot newsnap = sp.getSnapshotAfterTime(50);

        RectangularObject dribbleBox = ourRobot.getFrontSide()
                .extendBothDirections(0.01).widen(0.25);
        addDrawable(new DrawableRectangularObject(dribbleBox, Color.CYAN));
		addDrawable(new DrawableLine(newsnap.getBalle().getFrontSide(), Color.RED));

		/*
		 * Choosing Strategy:
		 */

		boolean attack = weShouldAttack(ourRobot, oppRobot, ball, ourGoal, oppGoal);

		if (attack) {
			if (ourRobot.possessesBall(ball)) {
				return dribbleAndShoot;
			} else {
				return goToBall;
			}
		} else {
			double ourDistanceToGoal = ourRobot.getPosition().dist(ourGoal.getPosition());
			double oppDistanceToGoal = oppRobot.getPosition().dist(ourGoal.getPosition());

			if (oppDistanceToGoal < ourDistanceToGoal) {
				// GoToGoal;
				return goToBall;
			} else {
				// Defend;
				return goToBall;
			}
		}

	}

	private boolean weShouldAttack(Robot ourRobot, Robot oppRobot, Ball ball, Goal ourGoal, Goal oppGoal) {

		if (ourRobot.possessesBall(ball)) {
			return true;
		}

		double ourDistanceToBall = ourRobot.getPosition().dist(ball.getPosition());
		double oppDistanceToBall = oppRobot.getPosition().dist(ball.getPosition());

		if (ourDistanceToBall < oppDistanceToBall) {
			return true;
		}

		return false;
	}


}
