package nslbattlecodemaster.robots;

import battlecode.common.*;

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

    int gardenersBuilt = 0;

    public ArComm(RobotController rc) {
        super(rc);
        name = "ArComm";
    }

    public void run() throws GameActionException {

        // XXX to start we're just going to build 2 gardeners as soon as possible

        if (gardenersBuilt < 2) {
            // Generate a random direction
            Direction dir = randomDirection();

            // Randomly attempt to build a gardener in this direction
            if (rc.canHireGardener(dir) && Math.random() < .01) {
                rc.hireGardener(dir);
                System.out.println("hiring gardener");
                RobotInfo myInfo = getMyRobotInfo(rc);
                MapLocation myLoc = myInfo.getLocation();
                float myRadius = myInfo.getRadius();
                System.out.println("my info "+myInfo+" radius "+myRadius);
                System.out.println("BROADCAST_MAX_CHANNELS "+GameConstants.BROADCAST_MAX_CHANNELS);


                MapLocation gardenerLoc = myLoc.add(dir,myRadius + 0.5f); // .5 so we are in the child's radius
                RobotInfo gardenerInfo = rc.senseRobotAtLocation(gardenerLoc);
                System.out.println("sensed gardener " + gardenerInfo.getID());

                // XXX broadcast to gardenerInfo.getID() telling it what chanel it should write on
            }
        }

        // XXX listen for the robots we've created to learn their status

        // Move randomly
        tryMove(randomDirection());
    }

    public RobotInfo getMyRobotInfo(RobotController rc) throws GameActionException {
        /* this should be part of the RobotController interface, sigh */
        // XXX move to utilities
        MapLocation myLoc = rc.getLocation();
        return rc.senseRobotAtLocation(myLoc);
    }
}
