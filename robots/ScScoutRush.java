package nslbattlecodemaster.robots;

import battlecode.common.*;
import nslbattlecodemaster.util.*;

/*  TODO


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

    public static final int MISSION_ABORT_RANGE = 3; // how close we can get to a location w/o seeing it before we give up

    public CommChannel channel;
    public int squadronNum = -1; // -1 == unassigned
    public int[] squadronMates; // their channels
    MapLocation missionTarget = null;

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
        if (squadronNum == -1) {
            // first see if there is a squadron already mustering that I can join
            squadronNum = rc.readBroadcast(SQUADRON_BASE);
            //System.out.println("p1 sn"+squadronNum);
            int i = 0;
            for (; i < SQUADRON_SIZE; i++) {
                //System.out.println("p1.1 about to read matesChannel i"+i);
                int matesChannel = rc.readBroadcast(SQUADRON_BASE + 1 + i);
                //System.out.println("p1.2 read matesChannel i"+i +" mc"+matesChannel);
                if (matesChannel == 0) {
                    // good an empty slot, put our channel here
                    //System.out.println("p1.3 about to write matesChannel i"+i+" mych"+channel.myChannel);
                    rc.broadcast(SQUADRON_BASE + 1 + i, channel.myChannel);
                    //System.out.println("p1.4 about to break i"+i);
                    break;
                }
            }
            //System.out.println("p2 sn"+squadronNum+" i"+i);
            if (i == SQUADRON_SIZE) {
                // oops all the slots are taken for this squadron, We need to start a new squadron
                squadronNum++;
                rc.broadcast(SQUADRON_BASE, squadronNum); // increment the currently mustering squadron number
                rc.broadcast(SQUADRON_BASE + 1, channel.myChannel); // join the new squadron
                for (i = 1; i < SQUADRON_SIZE; i++) {
                    // and zero out the squadron's other channels so future guys can join us
                    rc.broadcast(SQUADRON_BASE + 1 + i, 0);
                }
            }
            //System.out.println("p3 sn"+squadronNum+" i"+i);

            // now that we have our squadron, lets get our squadron's missionTarget
            missionTarget = getDefaultTarget(); // get the missionTarget's location
        }

        // see if I can find out anything about my squadron mates
        int currentlyMusteringSquadron = rc.readBroadcast(SQUADRON_BASE);
        //System.out.println("p4 sn"+squadronNum+" cms"+currentlyMusteringSquadron);

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

        //System.out.println(toString());

        // look for nearby enemy bots
        RobotInfo[] enemyBots = rc.senseNearbyRobots(-1, enemy);
        RobotInfo enemyArchon = Util.containsArchon(enemyBots);
        // and update sightings
        EnemyTracker.trackEnemies(rc, enemyBots);

        // figure out what our next move is
        MapLocation loc;
        if (mustering) {
            loc = proceedToMusteringGrounds(enemyBots, enemyArchon);
            Direction dir = new Direction(rc.getLocation(), loc);
            tryMove(dir);
        } else {

            // our squadron is ready to attack!  get our orders
            loc = attackFormation(enemyBots, enemyArchon);

            // make sure the move isn't off the board, because if it is, we don't want to
            // waste time trying to get there
            boolean isOnMap;
            try {
                isOnMap = rc.onTheMap(loc);
                //System.out.println("loc " + loc + " on map " + isOnMap);
            } catch (Exception e) {
                // this happens when we are out of sensor range, in other words
                // we don't know if it is on the map.  So, since it is too far
                // away to sense, we'll check if we can take a stride toward it
                // without leaving the map.
                Direction dir = new Direction(rc.getLocation(), loc);
                if (rc.canMove(dir)) {
                    isOnMap = true; // at least we can take one stride in that direction
                    //System.out.println("loc " + loc + " out of sensor range, but one stride in that direction is on map");
                } else {
                    isOnMap = false; // nope, even one stride is off the map, get a new mission target
                    //System.out.println("loc " + loc + " out of sensor range, and one stride in that direction is off map");
                }
            }
            if (isOnMap) {
                // try to make the move
                Direction dir = new Direction(rc.getLocation(), loc);
                tryMove(dir);
                //System.out.println("trying to move to loc " + loc + " location after moving " + rc.getLocation());
            } else {
                missionTarget = null; // get a new mission next turn
                //System.out.println("loc is off map, get a new missionTarget");
                tryMove(randomDirection()); // move randomly for now
            }
        }

        // find something to shoot at
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

    public MapLocation getDefaultTarget() {

        /* the idea is to send sequential squadrons against different targets, this
           may not be the best idea because one squadron probably isn't enough to
           take out an archon.  XXX revisit this
         */
        MapLocation[] enemyArchons = rc.getInitialArchonLocations(enemy);
        // choose which one to missionTarget based on the squadron number
        int targetNo = squadronNum % enemyArchons.length;
        return enemyArchons[targetNo]; // get the missionTarget's location
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

    public static final int LOCK_ON_RANGE = 3; // XXX maybe depends on other factors?

    public boolean shouldLockOn(MapLocation ourLoc, MapLocation targetLoc) {
        /* return true if we are in loclon range */

        if (ourLoc.distanceTo(targetLoc) <= LOCK_ON_RANGE)
            return true;
        return false;
    }

    public MapLocation attackFormation(RobotInfo[] enemyBots, RobotInfo enemyArchon) throws GameActionException {

        /* head toward our squadron's best target */

        // ask for our best target
        MapLocation target = EnemyTracker.getTargetLocation(); // ZZZ would be nice if it also returned the id
        MapLocation ourLocation = rc.getLocation();

        //System.out.println("q1 " + target + " ourloc " + ourLocation);
        if (target != null) {
            // we have a target, is it close enough to lock on
            if (shouldLockOn(ourLocation, target)) {
                target = lockOn(ourLocation, target);
            }
            //System.out.println("q2 " + target + " ourloc " + ourLocation);
            return target;
        }

        // no priority targets available, continue toward our missionTarget

        if (missionTarget == null) {
            // we ran into the edge of the board pursuing our last mission target.  Get a new one
            missionTarget = EnemyTracker.getSearchPattern(ourLocation);
            //System.out.println("q3 " + missionTarget + " ourloc " + ourLocation);
        }

        if (ourLocation.distanceTo(missionTarget) < MISSION_ABORT_RANGE) {
            // we are within close range of our mission target, but we can't find a priority target,
            // that means it has moved or was already destroyed.  We need a new missionTarget
            //System.out.println("q4 " + missionTarget + " ourloc " + ourLocation);
            missionTarget = EnemyTracker.getSearchPattern(ourLocation);
            //System.out.println("q4.1 new target " + missionTarget + " ourloc " + ourLocation);
        }

        //System.out.println("q5 " + missionTarget + " ourloc " + ourLocation);

        // proceed toward mission target
        return missionTarget;
    }

    public MapLocation lockOn(MapLocation ourLoc, MapLocation target) throws GameActionException {

        /* given our current location, and the target location, return the location we should drive to */

        return target; // XXX should do more
    }

    public RobotInfo chooseTargetToShoot(RobotInfo[] enemyBots, RobotInfo enemyArchon) throws GameActionException {

        /* we see an enemy Archon, shoot it, otherwise shoot the nearest gardener, otherwise the nearest robot */

        // ZZZ might want to factor target range in here, shooting at a long distance target might be a waste

        // look for a gardener
        for (int i = 0; i < enemyBots.length; i++) {
            if (enemyBots[i].getType() == RobotType.GARDENER) {
                return enemyBots[i];
            }
        }

        if (enemyArchon != null)
            return enemyArchon;

        // look for nearest bot
        if (enemyBots.length > 0)
            return enemyBots[0];

        // no target
        return null;
    }

    public String toString() {
        String res = "ScScoutRush id: " + rc.getID() +
                " hp " + rc.getHealth() +
                " channel " + channel.myChannel +
                " squadronNum " + squadronNum +
                " mustering " + mustering +
                " missionTarget " + missionTarget +
                " location " + rc.getLocation();
        for (int i = 0; i < SQUADRON_SIZE; i++) {
            res = res + " " + squadronMates[i];
        }
        return res;
    }
}