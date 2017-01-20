package nslbattlecodemaster;

import battlecode.common.*;
import nslbattlecodemaster.robots.*;

public strictfp class RobotPlayer {

    public static void run(RobotController rc) throws GameActionException {

        NSLRobot ai = null;
        switch (rc.getType()) {
            case ARCHON:
                ai = new ArComm(rc);
                break;
            case GARDENER:
                ai = new GaScoutRush(rc);
                break;
            case SCOUT:
                ai = new ScScoutRush(rc);
                break;
            default:
                System.out.println("derp");
        }

        ai.start();
    }
}