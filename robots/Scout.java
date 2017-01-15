package nslbattlecodemaster.robots;

import battlecode.common.*;
import nslbattlecodemaster.util.Util;

/**
 * Created by morris on 1/11/17.
 * Added enemy prioritization - ballantyne 1/14/17
 */
public strictfp class Scout extends NSLRobot {

    public Scout(RobotController rc) {
        super(rc);
        name = "Scout";
    }

    @SuppressWarnings("unused")
    public void run() throws GameActionException {

        MapLocation myLocation = rc.getLocation();

        // See if there are any nearby enemy robots
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
        // Check to see what type of enemy robot it is

        for (int i = 0; i <= robots.length; i++)
        {
            if (robots[i].getType() == RobotType.ARCHON) {
                int targetID = robots[i].getID();
                // is the below the best way to pass parameters?
                Util.lockOn(targetID, "ARCHON", myLocation);
                // TODO lock-on (don't let escape)
                // engage
                // broadcast location

            }



                    // If there are some...

        } if (robots.length > 0) {
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