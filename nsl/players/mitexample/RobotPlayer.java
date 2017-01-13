package nsl.players.mitexample;
import battlecode.common.*;
import nsl.robots.*;

public strictfp class RobotPlayer {

    public static void run(RobotController rc) throws GameActionException {

        // Create the appropriate AI
        NSLRobot ai = null;
        switch (rc.getType()) {
            case ARCHON:
                ai = new Archon(rc);
                break;
            case GARDENER:
                ai = new Gardener(rc);
                break;
            case SOLDIER:
                ai = new Soldier(rc);
                break;
            case LUMBERJACK:
                ai = new LumberJack(rc);
                break;
            case SCOUT:
                ai = new Scout(rc);
                break;
            case TANK:
                ai = new Tank(rc);
                break;
            default:
                System.out.println("Unrecognized Robot Type "+rc.getType());
        }
        // and start it
        ai.start();
	}
}
