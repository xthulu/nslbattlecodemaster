package nsl.robots;

import battlecode.common.*;

/**
 * Created by morris on 1/11/17.
 *
 * This AI just moves around randomly, not building anything.
 */
public strictfp class ArNervous extends Archon {

    public ArNervous(RobotController rc) {
        super(rc);
        name = "ARNervous";
    }

    public void run() throws GameActionException {

        // Move randomly
        tryMove(randomDirection());
    }
}