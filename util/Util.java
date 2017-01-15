package nslbattlecodemaster.util;
import com.sun.xml.internal.bind.v2.model.core.ID;
import nslbattlecodemaster.robots.Scout.*
import battlecode.common.*;

/**
 * Created by morris on 1/11/17.
 * adding lockOn on 1/15/17 - ballantyne.
 */
public class Util {
        // all new alando stuff
    public static void lockOn(RobotInfo targetInfo, RobotInfo attackerInfo, MapLocation myLocation) {
        // move so that (targetPosition - selfPosition) = (maxShootingRange - (maxTargetMovement/turn))

        int targetID = targetInfo.ID;
        MapLocation targetLocation = targetInfo.location;

        float targStrideRadius = targetInfo.type.strideRadius;
        float distanceToTarget = targetLocation.distanceTo(myLocation);
        float bulletRange =

        while (true) {
            if (distanceToTarget < ) {

            }



        }
    }



}
