package nsl.robots;

import battlecode.common.*;

/**
 * Created by morris on 1/11/17.
 */
public strictfp class Soldier extends NSLRobot {

    public Soldier(RobotController rc) {
        super(rc);
        name = "Soldier";
    }

    @SuppressWarnings("unused")
    public void run() throws GameActionException {

        MapLocation myLocation = rc.getLocation();

        // See if there are any nearby enemy robots
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

        // If there are some...
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
            }
        }

        // Move randomly
        tryMove(randomDirection());
    }
}