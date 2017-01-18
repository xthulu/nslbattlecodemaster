package nslbattlecodemaster.util;

import battlecode.common.*;

/**
 * Created by morris on 1/11/17.
 * adding lockOn on 1/15/17 - ballantyne.
 */
public class Util {

       /* HWM commented out so project will compile
    // all new alando stuff
    public static void lockOn(RobotInfo targetInfo, MapLocation myLocation) {
        // move so that (targetPosition - selfPosition) = (maxShootingRange - (maxTargetMovement/turn))

        int targetID = targetInfo.ID;
        MapLocation targetLocation = targetInfo.location;

        float targStrideRadius = targetInfo.type.strideRadius;
        float distanceToTarget = targetLocation.distanceTo(myLocation);

       float bulletRange =

        while (true) {
            if (distanceToTarget <) {

            }


        }

    }
        */

    public static RobotInfo containsArchon(RobotInfo[] bots) {

        // If the list contains an ARCHON, return the Archon's RobotInfo, else return null

        for (int i = 0; i < bots.length; i++) {
            if (bots[i].getType() == RobotType.ARCHON) {
                return bots[i];
            }
        }
        return null;
    }
}
