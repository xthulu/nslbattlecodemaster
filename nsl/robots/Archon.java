package nsl.robots;

import battlecode.common.*;

/**
 * Created by morris on 1/11/17.
 */
public strictfp class Archon extends NSLRobot {

    public Archon(RobotController rc) {
        super(rc);
        name = "Archon";
    }

    public void run() throws GameActionException {

        // Generate a random direction
        Direction dir = randomDirection();

        // Randomly attempt to build a gardener in this direction
        if (rc.canHireGardener(dir) && Math.random() < .01) {
            rc.hireGardener(dir);
        }

        // Move randomly
        tryMove(randomDirection());

        // Broadcast archon's location for other robots on the team to know
        MapLocation myLocation = rc.getLocation();
        rc.broadcast(0, (int) myLocation.x);
        rc.broadcast(1, (int) myLocation.y);
    }
}
