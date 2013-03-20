package balle.strategy;

import java.awt.Color;
import java.util.Iterator;

import org.apache.log4j.Logger;

import balle.controller.Controller;
import balle.main.drawable.Circle;
import balle.main.drawable.Dot;
import balle.main.drawable.DrawableLine;
import balle.main.drawable.DrawableVector;
import balle.misc.Globals;
import balle.strategy.executor.movement.GoToObjectPFN;
import balle.strategy.executor.movement.MovementExecutor;
import balle.strategy.executor.movement.OrientedMovementExecutor;
import balle.strategy.executor.turning.IncFaceAngle;
import balle.strategy.planner.AbstractPlanner;
import balle.world.Coord;
import balle.world.Line;
import balle.world.Orientation;
import balle.world.Snapshot;
import balle.world.Velocity;
import balle.world.objects.Ball;
import balle.world.objects.CircularBuffer;
import balle.world.objects.Goal;
import balle.world.objects.Pitch;
import balle.world.objects.Point;
import balle.world.objects.Robot;

public class Interception extends AbstractPlanner {
    private boolean ballHasMoved = false;
    private Coord   intercept          = new Coord(0, 0);
    private boolean shouldPlayGame;
	private static final double STRATEGY_STOP_DISTANCE = 0.25;
	private static final double GO_DIRECTLY_TO_BALL_DISTANCE = STRATEGY_STOP_DISTANCE * 2;
	boolean doThisStrat = true;
    protected final boolean useCpOnly;
	protected final boolean mirror;
    protected CircularBuffer<Coord> ballCoordBuffer;

    private static Logger LOG                = Logger.getLogger(Interception.class);

    private MovementExecutor movementExecutor;
    private OrientedMovementExecutor orientedMovementExecutor;
	private IncFaceAngle rotationExecutor;
    private AbstractPlanner gameStrategy;

    private boolean startGameAfterwards;
	private boolean initialTurn;

    protected void setIAmDoing(String message) {
        LOG.info(message);
    }

    public Interception(boolean useCpOnly, MovementExecutor movementExecutor,
			IncFaceAngle incFaceAngle, boolean mirror,
            boolean startGameAfterwards) {
        super();
        ballCoordBuffer = new CircularBuffer<Coord>(6);
        this.useCpOnly = useCpOnly;
		this.mirror = mirror;
        
        this.movementExecutor = movementExecutor;
		this.rotationExecutor = incFaceAngle;
        shouldPlayGame = false;
        this.gameStrategy = new Game(false);
        this.startGameAfterwards = startGameAfterwards;
		this.initialTurn = true;

        // new Game(new SimpleGoToBallFaceGoal(new BezierNav(
        // new SimplePathFinder(new CustomCHI()))), false);
    }


    @Override
	public void onStep(Controller controller, Snapshot snapshot)
			throws ConfusedException {

		if (shouldPlayGame) {
			gameStrategy.step(controller, snapshot);
			addDrawables(gameStrategy.getDrawables());
			return;
		}

		Coord optimum = new Coord(0, 0);
		Robot ourRobot = snapshot.getBalle();
		Goal goal = snapshot.getOwnGoal();
		Ball ball = snapshot.getBall();
		Pitch pitch = snapshot.getPitch();
		if (ball.getPosition() == null)
			return;

		ballCoordBuffer.add(ball.getPosition());
		double beta = snapshot.getOpponent().getFacingLine().angle().degrees();
		double alfa = snapshot.getBalle().getFacingLine().angle().degrees();
		double angle = getAngle(alfa, beta);
		setIAmDoing("Alfa " + alfa);
		setIAmDoing("Beta " + beta);
		setIAmDoing("Angle " + Math.abs(angle - 90));
		
		if (Math.abs(angle - 90) < 30) {
			initialTurn = false;
			Line ballLine = new Line(0, ball.getPosition().getY(),
					Globals.PITCH_MAX_X, ball.getPosition().getY());
			intercept = snapshot.getBalle().getFacingLine()
					.getIntersect(ballLine);
			// .getIntersect(snapshot.getOpponent().getFacingLine());

			doThisStrat = false;

			setIAmDoing("Going to intersection line in direction I'm heading");
		} else {
			LOG.info("Update angle, has ball moved?");
			if (ballHasMoved && initialTurn) {
				LOG.info("Ball moved, update angle");

				double robotY = ourRobot.getPosition().getY();
				double topwallY = pitch.getTopWall().getA().getY();
				double bottomY = pitch.getBottomWall().getA().getY();

				double robotToTop = Math.abs(topwallY - robotY);
				double robotToBottom = Math.abs(robotY - bottomY);

				if (robotToTop > robotToBottom) { // maybe > 90?
					rotationExecutor.setTargetOrientation(new Orientation(120));
				} else { // maybe < 270?
					rotationExecutor.setTargetOrientation(new Orientation(240));
				}
				rotationExecutor.step(controller, snapshot);
				return;
			}
		}

		if (doThisStrat) {
			intercept = getPredictionCoordVelocityvector(snapshot, useCpOnly,
					mirror);
		}
		// ballHasMoved = true;
		if (ballIsMoving(ball)) {
			LOG.info("BALL IS MOVIN'");
			ballHasMoved = true;
		}
		if (!shouldPlayGame) {
			addDrawable(new Circle(snapshot.getBalle().getPosition(),
					STRATEGY_STOP_DISTANCE, Color.red));
			addDrawable(new Circle(snapshot.getBalle().getPosition(),
					STRATEGY_STOP_DISTANCE * 1.75, Color.red));
		}
		if (snapshot.getBalle().getPosition()
				.dist(snapshot.getBall().getPosition()) < STRATEGY_STOP_DISTANCE) {
			if (startGameAfterwards)
				shouldPlayGame = true;
		}

		if (shouldPlayGame) {
			setIAmDoing("GAME!");
			gameStrategy.step(controller, snapshot);
			addDrawables(gameStrategy.getDrawables());
		} else if (ballHasMoved) {

			if (intercept == null) {
				return;
			}

			setIAmDoing("Going to point - predict");

			Coord goToCoord = intercept;
			if (snapshot.getBalle().getPosition()
					.dist(snapshot.getBall().getPosition()) < GO_DIRECTLY_TO_BALL_DISTANCE) {
				goToCoord = snapshot.getBall().getPosition();
				LOG.info("Going to ball!");
			}
			addDrawable(new Dot(goToCoord, Color.BLACK));
			addDrawable(new Dot(intercept, new Color(0, 0, 0, 100)));
			if (movementExecutor != null) {
				movementExecutor.updateTarget(new Point(goToCoord));
				addDrawables(movementExecutor.getDrawables());
				movementExecutor.step(controller, snapshot);

			} else if (orientedMovementExecutor != null) {
				orientedMovementExecutor.updateTarget(new Point(goToCoord),
						snapshot.getOpponentsGoal().getGoalLine().midpoint()
								.sub(intercept).orientation());
				addDrawables(orientedMovementExecutor.getDrawables());
				orientedMovementExecutor.step(controller, snapshot);


			}
		} else {
			setIAmDoing("Waiting");
			controller.stop();
		}
	}

	/*
	 * Commenting out strategy in simulator
	 * 
	 * @FactoryMethod(designator = "InterceptsM4-CP-PFN", parameterNames = {})
	 * public static final Interception factoryCPPFN() { return new
	 * Interception(true, new GoToObjectPFN( Globals.ROBOT_LENGTH / 3), null,
	 * true, true); }
	 * 
	 * @FactoryMethod(designator = "InterceptsM4-NCP-PFN", parameterNames = {})
	 * public static final Interception factoryNCPPFN() { return new
	 * Interception(false, new GoToObjectPFN( Globals.ROBOT_LENGTH / 3), null,
	 * true, true); }
	 * 
	 * @FactoryMethod(designator = "InterceptsM4-CP-PFNF", parameterNames = {})
	 * public static final Interception factoryCPPFNF() { return new
	 * Interception(true, new GoToObjectPFN( Globals.ROBOT_LENGTH / 3, false),
	 * null, true, true); }
	 * 
	 * @FactoryMethod(designator = "InterceptsM4-CP-PFNF-NG", parameterNames =
	 * {}) public static final Interception factoryCPPFNFNG() { return new
	 * Interception(true, new GoToObjectPFN( Globals.ROBOT_LENGTH / 3, false),
	 * null, true, false); }
	 */
	@FactoryMethod(designator = "InterceptsM4-NCP-PFNF", parameterNames = {})
	public static final Interception factoryNCPPFNF() {
		return new Interception(false, new GoToObjectPFN(0.1, false),
				new IncFaceAngle(), true,
				true);
	}

	/*
	 * @FactoryMethod(designator = "InterceptsM4-CP-BZR", parameterNames = {})
	 * public static final Interception factoryCPBZR() { return new
	 * Interception(true, null, new BezierNav(new SimplePathFinder( new
	 * CustomCHI())), true, true); }
	 * 
	 * @FactoryMethod(designator = "InterceptsM4-CP-BZR-NG", parameterNames =
	 * {}) public static final Interception factoryCPBZRNG() { return new
	 * Interception(true, null, new BezierNav(new SimplePathFinder( new
	 * CustomCHI())), true, false); }
	 */


    /**
     * Checks when ball has moved since last reading
     * 
     * @return
     */
    private boolean ballIsMoving(Ball ball) {
        Iterator<Coord> it = ballCoordBuffer.iterator();
        Coord lastKnownLoc = null;
        while (it.hasNext())
            lastKnownLoc = it.next();
        
        if (lastKnownLoc == null)
            return false;
        
        if (lastKnownLoc.dist(ball.getPosition()) > 0.05) {
            return true;
        } else {
            return false;
        }
    }


	protected Coord getPredictionCoordVelocityvector(Snapshot s,
			boolean useCPOnly, boolean mirror) {
        Ball ball = s.getBall();
        Robot ourRobot = s.getBalle();

		Coord ballPos, currPos;
		ballPos = ball.getPosition();
		currPos = ourRobot.getPosition();

		// double dist = (new Line(currPos, s.getOwnGoal().getPosition()))
		// .length();
		// if (mirror && dist > (Globals.PITCH_WIDTH / 2)) {
		//
		// // // Mirror X position.
		// // double dX = currPos.getX() - s.getPitch().getPosition().getX();
		// // currPos = new Coord(s.getPitch().getPosition().getX() - dX,
		// // currPos.getY());
		//
		// addDrawable(new Label("length = " + dist, new Coord(0, 0),
		// Color.ORANGE));
		//
		// return s.getOwnGoal().getPosition();
		//
		// }


        Velocity vel = ball.getVelocity();
        Coord vec = new Coord(vel.getX(), vel.getY());
        vec = vec.mult(0.5 / vec.abs());
        
		Line ballRobotLine = new Line(ballPos, currPos);
        // .extendBothDirections(Globals.PITCH_WIDTH);
        //addDrawable(new DrawableLine(ballRobotLine, Color.WHITE));

		Line ballDirectionLine = new Line(ballPos, currPos.add(vec));
        ballDirectionLine = ballDirectionLine.extend(Globals.PITCH_WIDTH);
        //addDrawable(new DrawableLine(ballDirectionLine, Color.WHITE));


        Line rotatedRobotBallLine = ballRobotLine.rotateAroundPoint(
                ballRobotLine.midpoint(), Orientation.rightAngle)
                .extendBothDirections(Globals.PITCH_WIDTH);
        //addDrawable(new DrawableLine(rotatedRobotBallLine, Color.PINK));

        Coord pivot = rotatedRobotBallLine.getIntersect(ballDirectionLine);
       
		Coord CP = ballDirectionLine.closestPoint(currPos);
        addDrawable(new DrawableLine(ballDirectionLine, Color.WHITE));
        addDrawable(new Dot(CP, Color.WHITE));
		addDrawable(new DrawableLine(new Line(CP, currPos),
                Color.CYAN));
        // addDrawable(new DrawableLine(new Line(CP, ball.getPosition()),
        // Color.ORANGE));

        
		if (useCPOnly) {
			// Coord earlier = CP.add(ball.getPosition().sub(CP).getUnitCoord()
			// .mult(0.4));
//			return earlier;
			return CP;
		}
        
        if (pivot == null)
            return CP;
        
        //addDrawable(new Dot(pivot, Color.RED));

        Coord scaler = CP.sub(pivot);
		Orientation theta = ballPos.angleBetween(currPos, CP);
		Orientation theta2 = ballPos.angleBetween(CP, currPos);
        
         double minTheta = Math.min(theta.radians(), theta2.radians());
        
         // addDrawable(new Label(String.format("Theta: %.2f", minTheta),
         // new Coord(0, -0.15), Color.CYAN));
        

        scaler = scaler.mult(Math.abs(minTheta) / (Math.PI / 2));
        
		Coord predictCoord = pivot.sub(scaler);
		Line robotPredictLine = new Line(currPos, predictCoord);
        // robotPredictLine = robotPredictLine.extend(0.5);
        addDrawable(new DrawableLine(robotPredictLine, Color.PINK));
        predictCoord = robotPredictLine.getB();

         // addDrawable(new DrawableLine(rotatedRobotBallLine, Color.PINK));
        
         // Coord predictCoord = rotatedRobotBallLine
         // .getIntersect(ballDirectionLine);
        
         // if (predictCoord == null) {
         // // if the lines do not intersect jsut get a point thats 1m away
        // from
         // // the ball
         //
         // predictCoord = ball.getPosition().add(vec);
         // }
         //
         // //addDrawable(new DrawableVector(ball.getPosition(), vec,
         // Color.WHITE));
        addDrawable(new DrawableVector(pivot, scaler, Color.BLACK));
        return predictCoord;
    }
	
	// private double normalize_angle(double angle) {
	// if (angle > 270)
	// return angle - 270;
	// else if (angle > 180)
	// return angle - 180;
	// else if (angle > 90)
	// return angle - 90;
	// else
	// return angle;
	// }

	private double getAngle(double alfa, double beta) {
		if (alfa > 180)
			alfa = alfa - 180;
		if (beta < 180 && alfa < 90)
			return alfa - beta;
		if (beta < 360 && alfa < 90)
			return beta - 180 - alfa;
		if (beta < 180 && alfa >= 90)
			return 180 - alfa - beta;
		if (beta < 360 && alfa >= 90) {
			return beta - 180 - alfa;
		}
		if (beta == 0)
			return alfa;
		return 0;
	}

}
