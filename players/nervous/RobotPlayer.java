package nslbattlecodemaster.players.nervous;

import battlecode.common.*;
import nslbattlecodemaster.robots.*;

/* The nervous player doesn't build anything, other than a nervous archon
   bot that will twitch around the battlefield.
 */

public strictfp class RobotPlayer {

    public static void run(RobotController rc) throws GameActionException {

        // Create the appropriate AI
        NSLRobot ai = null;
        switch (rc.getType()) {
            case ARCHON:
                // just create a nervous archon
                ai = new ArNervous(rc);
                break;
            default:
                System.out.println("Unrecognized Robot Type " + rc.getType());
        }
        // and start it
        ai.start();
    }
}
