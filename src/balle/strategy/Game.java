package balle.strategy;

import java.awt.Color;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import balle.controller.Controller;
import balle.main.drawable.Circle;
import balle.main.drawable.Drawable;
import balle.main.drawable.DrawableLine;
import balle.main.drawable.DrawableRectangularObject;
import balle.main.drawable.Label;
import balle.misc.Globals;
import balle.simulator.SnapshotPredictor;
import balle.strategy.basic.Initial;
import balle.strategy.bezierNav.BezierNav;
import balle.strategy.curve.CustomCHI;
import balle.strategy.executor.movement.GoToObjectPFN;
import balle.strategy.executor.turning.IncFaceAngle;
import balle.strategy.executor.turning.RotateToOrientationExecutor;
import balle.strategy.pathFinding.SimplePathFinder;
import balle.strategy.planner.AbstractPlanner;
import balle.strategy.planner.BackingOffStrategy;
import balle.strategy.planner.DefensiveStrategy;
import balle.strategy.planner.GoToBall;
import balle.strategy.planner.GoToBallSafeProportional;
import balle.strategy.planner.GoToObjectSafeProportional;
import balle.strategy.planner.KickFromWall;
import balle.strategy.planner.SimpleGoToBallFaceGoal;
import balle.world.Coord;
import balle.world.Line;
import balle.world.Snapshot;
import balle.world.objects.Ball;
import balle.world.objects.Goal;
import balle.world.objects.Pitch;
import balle.world.objects.RectangularObject;
import balle.world.objects.Robot;

	
public class Game extends AbstractPlanner {

    private static final Logger LOG = Logger.getLogger(Game.class);
	// Strategies that we will need make sure to call stop() for each of them
	protected final Strategy defensiveStrategy;
    protected final Strategy opponentKickDefendStrategy;
	protected final Strategy pickBallFromWallStrategy;
	protected final BackingOffStrategy backingOffStrategy;
	protected final RotateToOrientationExecutor turningExecutor;
	protected final GoToObjectSafeProportional kickingStrategy;
    protected Strategy initialStrategy;

    protected final Strategy goToBallPFN;
	protected final Strategy goToBallBezier;
    protected final Strategy goToBallPrecision;

    protected boolean initial;

    private String currentStrategy = null;

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

    @FactoryMethod(designator = "Game", parameterNames = { "init",
            "no bounce shots" })
    public static Game gameFactoryTesting2(boolean init, boolean notTriggerHappy) {
        Game g = new Game(init);
        g.setTriggerHappy(!notTriggerHappy);
        return g;
    }

	/*
	 * Commenting out strategy in simulator
	 * 
	 * @FactoryMethod(designator = "GameInitTest", parameterNames = {
	 * "angle (deg)" }) public static Game gameInitTest(double angle) { Game g =
	 * new Game(true); g.setTriggerHappy(true); g.setInitialStrategy(new
	 * InitialBezierStrategy(angle)); return g; }
	 */

    public void setTriggerHappy(boolean triggerHappy) {
		// kickingStrategy.setTriggerHappy(triggerHappy);
    }

    public Game() {
        defensiveStrategy = new GoToBallSafeProportional(0.5, 0.4, true);
        opponentKickDefendStrategy = new DefensiveStrategy(new GoToObjectPFN(0));
        pickBallFromWallStrategy = new KickFromWall(new GoToObjectPFN(0));
		backingOffStrategy = new BackingOffStrategy();
        turningExecutor = new IncFaceAngle();
		kickingStrategy = new GoToObjectSafeProportional();
		initialStrategy = new Initial();
		goToBallPFN = new GoToObjectSafeProportional(); // Was goToBallSafeProportional (2/4/12)
		goToBallBezier = new SimpleGoToBallFaceGoal(new BezierNav(
                new SimplePathFinder(new CustomCHI())));
		goToBallPrecision = new GoToBall(new GoToObjectPFN(0), false);
        initial = false;
    }

    public boolean isInitial(Snapshot snapshot) {
        if (initial == false)
            return false;

        // Check if we have ball
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
        } else if (Initial.finished) {
        	setInitial(false);
        }
        
        return initial;

    }

    public void setInitial(boolean initial) {
        this.initial = initial;
    }

    public Game(boolean startWithInitial) {
        this();
        initial = startWithInitial;
        Initial.finished = !startWithInitial;
        LOG.info("Starting game strategy with initial strategy turned on");
    }

    @Override
    public void stop(Controller controller) {
        defensiveStrategy.stop(controller);
        pickBallFromWallStrategy.stop(controller);
		backingOffStrategy.stop(controller);
		defensiveStrategy.stop(controller);
		opponentKickDefendStrategy.stop(controller);
		turningExecutor.stop(controller);
		kickingStrategy.stop(controller);
		initialStrategy.stop(controller);
		goToBallPFN.stop(controller);
		goToBallBezier.stop(controller);
		goToBallPrecision.stop(controller);

	}

    @Override
    public void onStep(Controller controller, Snapshot snapshot)
            throws ConfusedException {

        Robot ourRobot = snapshot.getBalle();
		Robot oppRobot = snapshot.getOpponent();
        Ball ball = snapshot.getBall();


		// fix made by Toms not tested
		// if (ourRobot.getPosition().dist(ball.getPosition()) <
		// Math.min(Globals.ROBOT_LENGTH / 2,
		// Globals.ROBOT_WIDTH / 2)) {
		// LOG.info("Ball uvisible/under our robot.");
		// return;
		// }
		// if (oppRobot.getPosition().dist(ball.getPosition()) <
		// Math.min(Globals.ROBOT_LENGTH / 2,
		// Globals.ROBOT_WIDTH / 2)) {
		// LOG.info("Ball uvisible/under opp robot.");
		// return;
		// }

		if (backingOffStrategy.shouldStealStep(snapshot)) {
			backingOffStrategy.step(controller, snapshot);
			return;
		}

        if (isInitial(snapshot)) {
            setCurrentStrategy(initialStrategy.getClass().getName());

            initialStrategy.step(controller, snapshot);

            addDrawables(initialStrategy.getDrawables());
            return;
        }

		// SnapshotPredictor sp = snapshot.getSnapshotPredictor();

        String oldStrategy = getCurrentStrategy();
		Strategy strategy = getStrategy(snapshot);
        LOG.debug("Selected strategy: " + strategy.getClass().getName());
		setCurrentStrategy(strategy.getClass().getName());
        if ("balle.strategy.Dribble_M4".equals(oldStrategy)
                && !oldStrategy.equals(getCurrentStrategy())) {
            LOG.info("Stopped using Dribble for " + getCurrentStrategy());
            LOG.info(ourRobot.getOrientation().degrees());
            LOG.info(ourRobot.getFrontSide().midpoint()
                    .dist(ball.getPosition()));
        }
        try {
            strategy.step(controller, snapshot);
        } catch (ConfusedException e) {
            // If a strategy does not know what to do
			LOG.error("Game catch block. " + getCurrentStrategy(), e);
			System.out.println("Error in: " + getCurrentStrategy());
            // Default to goToBallPFN
            goToBallPFN.step(controller, snapshot);
        }

		addDrawables(strategy.getDrawables());
    }

	private Strategy getStrategy(Snapshot snapshot) {
		Robot ourRobot = snapshot.getBalle();
		Robot opponent = snapshot.getOpponent();
		Ball ball = snapshot.getBall();
		Goal ownGoal = snapshot.getOwnGoal();
		Goal opponentsGoal = snapshot.getOpponentsGoal();

		Pitch pitch = snapshot.getPitch();

		// Adding drawables

        addDrawable(new Circle(ourRobot.getFrontSide().midpoint(), 0.4,
                Color.BLUE));

		// Get predicted snapshot
        SnapshotPredictor sp = snapshot.getSnapshotPredictor();
        Snapshot newsnap = sp.getSnapshotAfterTime(50);

        RectangularObject dribbleBox = ourRobot.getFrontSide()
                .extendBothDirections(0.01).widen(0.25);
        addDrawable(new DrawableRectangularObject(dribbleBox, Color.CYAN));
        

        addDrawable(new DrawableLine(newsnap.getBalle().getFrontSide(),
                Color.red));

		/*
		 * Choosing Strategy:
		 */


		// 1.
		if ((ball.getPosition().isEstimated() && ball
                .getPosition().dist(ourRobot.getPosition()) < Globals.ROBOT_LENGTH * 2)
                || (dribbleBox.containsCoord(ball.getPosition()) && !ourRobot
                        .isFacingGoalHalf(ownGoal))) {
            addDrawable(new Label("DRIBBLING", ball.getPosition().sub(
                    new Coord(0.1, 0.1)), Color.CYAN));
            return kickingStrategy;
		}

		// 2.
        if ((opponent.getPosition() != null)
                && (opponent.possessesBall(ball) && (opponent
                        .isFacingGoal(ownGoal)))
                && (!ourRobot.intersects(opponent.getBallKickLine(ball)))) {
            return opponentKickDefendStrategy;
        }

		// Could the opponent be in the way? use bezier if so
		// 3.
		// Drawing line between ourselves and ball
		RectangularObject corridor = new Line(ourRobot.getPosition(),
				ball.getPosition()).widen(0.5);
        addDrawable(new DrawableRectangularObject(corridor, Color.BLACK));

        if ((corridor.containsCoord(opponent.getPosition()))
                && !(opponent.possessesBall(ball) && (opponent
                        .isFacingGoalHalf(ownGoal)))) {
			// return goToBallBezier;
			return goToBallPFN;
		}
		
		// 4.
		// Draw line to ball and it's estimated position in 40 frames.
        Line ballMovementLine = new Line(ball.getPosition(), snapshot
                .getBallEstimator().estimatePosition(40));
        addDrawable(new DrawableLine(ballMovementLine, Color.MAGENTA));

        if (ballMovementLine.intersects(snapshot.getOwnGoal().getGoalLine()
                .extendBothDirections(0.5))) {
            return defensiveStrategy;
        }
		    
		// 5.
		if (!ourRobot.isApproachingTargetFromCorrectSide(ball, opponentsGoal,
				25) || (ourRobot.getPosition().dist(ball.getPosition()) > 1)) {
			return goToBallPFN;
		}

		// 6.
		if (ourRobot.getPosition().dist(ball.getPosition()) > 1) {
			return goToBallPFN;
		}

		// Bezier can have trouble next to walls
		// 7.
		// DO WE USE BEZZIER?
		// if (ourRobot.isNearWall(pitch)
		// && (!ball.isNearWall(pitch) || ourRobot.getPosition().dist(
		// ball.getPosition()) > 0.5)) {
		// return goToBallPFN;
		// }

		// 8.
        if ((!ourRobot.isNearWall(snapshot.getPitch()))
                && (!ball.isNearWall(snapshot.getPitch()))
                && (ourRobot.getFrontSide().midpoint().dist(ball.getPosition()) < 0.2)) {
            return goToBallPrecision;
        }

		// 9.
		if (ourRobot.isNearWall(snapshot.getPitch()) && ball.isNearWall(snapshot.getPitch())) {
			return pickBallFromWallStrategy;
		}

		// return goToBallBezier;
		return goToBallPFN;
	}

}
