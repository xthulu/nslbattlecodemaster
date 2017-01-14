package nslbattlecodemaster.robots;

import battlecode.common.*;
import nslbattlecodemaster.util.CommChannel;

import java.util.Map;

/**
 * Created by morris on 1/12/17.
 *
 * The idea behind this Archon is that it will keep track of
 * the gardeners and other robots that are in the field using
 * comms.  This will let it make efficient decisions about
 * when and what to build.
 */
public strictfp class ArComm extends Archon {

    public CommChannel channel;

    public ArComm(RobotController rc) {
        super(rc);
        name = "ArComm";
    }

    public void run() throws GameActionException {

        // initialize our comm channel if necessary
        if (channel == null) {
            // since we are only going to read from it, we don't need to assign a channel
            channel = new CommChannel();
        }

        // count how many gardeners are alive
        int livingGardeners = channel.countLivingRobots(rc,GaScoutRush.robotType,3);
        if (livingGardeners < 2) {
            // try to build a gardener
            Direction dir = randomDirection();
            if (rc.canHireGardener(dir)) {
                rc.hireGardener(dir);
                System.out.println("hiring gardener");
            }

            /* DEAD CODE, IGNORE
                RobotInfo myInfo = getMyRobotInfo(rc);
                // find my location, add the direction I built the gardener, get that robot's id
                MapLocation myLoc = myInfo.getLocation();
                float myRadius = myInfo.getRadius();
                System.out.println("my info "+myInfo+" radius "+myRadius);
                MapLocation gardenerLoc = myLoc.add(dir,myRadius + 0.5f); // .5 so we are in the child's radius
                RobotInfo gardenerInfo = rc.senseRobotAtLocation(gardenerLoc);
                System.out.println("sensed gardener " + gardenerInfo.getID());
            */
        }
        // Move randomly
        tryMove(randomDirection());

        //channel.dbgChannels(rc);
    }

    public RobotInfo getMyRobotInfo(RobotController rc) throws GameActionException {
        /* this should be part of the RobotController interface, sigh */
        // XXX move to utilities
        MapLocation myLoc = rc.getLocation();
        return rc.senseRobotAtLocation(myLoc);
    }
}
