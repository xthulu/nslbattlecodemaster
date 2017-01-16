package nslbattlecodemaster.robots;

import battlecode.common.*;
import nslbattlecodemaster.util.*;

import java.util.Map;

/*  TODO
    -bug why are there 4 scouts in a squadron instead of 3
    -need to broadcast location of enemy archons as we find them
    -when you are at your target location, and don't see any archons, head off to known locations, or start a search pattern
    -need more than 3 scouts to take down an archon
 */

/**
 * Created by morris on 1/15/17.
 * <p>
 * Scouts are formed into SQUADRON_SIZE squadrons.
 * <p>
 * The first squadron (squadron 1) goes north, the 2nd goes south, the 3rd goes north etc.
 * <p>
 * The global com buffer is used to organize squadrons.  SQUADRON_BASE contains the squadron number of
 * the currently mustering squadron.  The next SQUADRON_SIZE positions contain the channel numbers.
 */
public strictfp class ScScoutRush extends Scout {

    public static final int robotType = 3;
    public static final int squadronNumPositionInChannel = 5; // right after the header
    public static final int SQUADRON_SIZE = 3; // number of scouts in a squadron
    public static final int SQUADRON_BASE = 2; // where we start writing in the global comm buffer

    public CommChannel channel;
    public int squadronNum = 0; // 0 == unassigned
    public int[] squadronMates; // their channels
    MapLocation target = null;

    public boolean mustering = true; // false = launch attack

    public ScScoutRush(RobotController rc) {
        super(rc);
        name = "ScScoutRush";
        squadronMates = new int[SQUADRON_SIZE]; // note, includes ourselves to make code cleaner
    }

    public void run() throws GameActionException {

        // initialize our comm channel if necessary
        if (channel == null) {
            channel = new CommChannel();
            channel.assignNewChannel(rc);
        }

        // assign myself to a squadron
        if (squadronNum == 0) {
            // first see if there is a squadron already mustering that I can join
            squadronNum = rc.readBroadcast(SQUADRON_BASE);
            int i = 0;
            for (; i < SQUADRON_SIZE; i++) {
                int matesChannel = rc.readBroadcast(SQUADRON_BASE + 1 + i);
                if (matesChannel == 0) {
                    // good an empty slot, put our channel here
                    rc.broadcast(SQUADRON_BASE + 1 + i, channel.myChannel);
                    break;
                }
            }
            if (i == SQUADRON_SIZE) {
                // oops all the slots are taken for this squadron, We need to start a new squadron
                squadronNum++;
                rc.broadcast(SQUADRON_BASE, squadronNum); // increment the currently mustering squadron number
                for (i = 0; i < SQUADRON_SIZE; i++) {
                    // and zero out the new squadron's channels
                    rc.broadcast(SQUADRON_BASE + 1 + i, 0);
                }
            }
            // now that we have our squadron, lets get our squadron's target
            MapLocation[] enemyArchons = rc.getInitialArchonLocations(enemy);
            // choose which one to target based on the squadron number
            int targetNo = squadronNum % enemyArchons.length;
            target = enemyArchons[targetNo]; // get the target's location
        }
        // see if I can find out anything about my squadron mates
        int currentlyMusteringSquadron = rc.readBroadcast(SQUADRON_BASE);
        if (currentlyMusteringSquadron == squadronNum) {
            int i = 0;
            for (; i < SQUADRON_SIZE; i++) {
                squadronMates[i] = rc.readBroadcast(SQUADRON_BASE + 1 + i);
                if (squadronMates[i] == 0) {
                    mustering = true;
                    break; // not everyone has mustered
                }
            }
            if (i == SQUADRON_SIZE) {
                // we found everyone!  Time to attack
                mustering = false;
            }
        }

        // look for nearby enemy bots
        RobotInfo[] enemyBots = rc.senseNearbyRobots(-1, enemy);
        RobotInfo enemyArchon = Util.containsArchon(enemyBots);

        MapLocation loc;
        if (enemyArchon != null)
            loc = lockOn(enemyBots, enemyArchon);
        if (mustering)
            loc = proceedToMusteringGrounds(enemyBots, enemyArchon);
        else
            loc = attackFormation(enemyBots, enemyArchon);

        Direction dir = new Direction(rc.getLocation(), loc);
        tryMove(dir);

        RobotInfo shootAt = chooseTargetToShoot(enemyBots, enemyArchon);
        if (shootAt != null) {
            if (rc.canFireSingleShot()) {
                rc.fireSingleShot(rc.getLocation().directionTo(shootAt.location));
            }
        }

        // let the rest of the team know we are still alive
        channel.writeChannelHeader(rc, robotType, enemyBots.length > 0, enemyArchon != null);
        // and tell them my squadron number
        rc.broadcast(channel.myChannel + squadronNumPositionInChannel, squadronNum);
    }

    public MapLocation proceedToMusteringGrounds(RobotInfo[] enemyBots, RobotInfo enemyArchon) throws GameActionException {

        /* head toward squadron's mustering grounds, near either the top or bottom of the screen */

        float stride = rc.getType().strideRadius;

        if ((squadronNum % 2) == 1) {
            // odd squadrons head north
            return rc.getLocation().add(Direction.getNorth(), stride);
        } else {
            // evens go south
            return rc.getLocation().add(Direction.getSouth(), stride);
        }
    }

    public MapLocation attackFormation(RobotInfo[] enemyBots, RobotInfo enemyArchon) throws GameActionException {

        /* head toward our squadron's target */

        return target;
    }

    public MapLocation lockOn(RobotInfo[] enemyBots, RobotInfo enemyArchon) throws GameActionException {

        /* we see an enemy Archon, destroy it */

        return enemyArchon.getLocation();
    }

    public RobotInfo chooseTargetToShoot(RobotInfo[] enemyBots, RobotInfo enemyArchon) throws GameActionException {

        /* we see an enemy Archon, shoot it, otherwise shoot the nearest target */

        if (enemyArchon != null)
            return enemyArchon;
        else if (enemyBots.length > 0)
            return enemyBots[0];
        else
            return null;
    }
}