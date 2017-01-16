package nslbattlecodemaster.robots;

import battlecode.common.*;
import nslbattlecodemaster.util.*;

/**
 * Created by morris on 1/13/17.
 *
 * This Gardener only produces scouts, and it does it
 * as fast as possible.
 */
public strictfp class GaScoutRush extends Gardener {

    public static final int robotType = 2;

    public CommChannel channel;

    public GaScoutRush(RobotController rc) {
        super(rc);
        name = "GaScoutRush";
    }

    public void run() throws GameActionException {

        // initialize our comm channel if necessary
        if (channel == null) {
            channel = new CommChannel();
            channel.assignNewChannel(rc);
        }

        // look for nearby enemy bots
        RobotInfo[] enemyBots = rc.senseNearbyRobots(-1, enemy);
        RobotInfo enemyArchon = Util.containsArchon(enemyBots);

        // move
        if (enemyBots.length > 0) {
            // XXX run away from enemy bots
        } else
            tryMove(randomDirection());

        // build a scout if possible
        Direction dir = randomDirection();
        if (rc.canBuildRobot(RobotType.SCOUT, dir)) {
            rc.buildRobot(RobotType.SCOUT, dir);
        }

        // let the rest of the team know we are still alive
        channel.writeChannelHeader(rc,robotType,enemyBots.length > 0,enemyArchon != null);
    }
}