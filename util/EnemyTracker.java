package nslbattlecodemaster.util;

import battlecode.common.*;

/**
 * Created by morris on 1/16/17.
 * <p>
 * Keep Track of enemys.  Particularly Archons and Gardeners.
 * <p>
 * For each type of enemy, we just keep track of the first one spotted this round.  We need 3 memory locations
 * for each enemy type - it's id, it's location, and the round it was last reported
 *
 * TODO
 * -add third type of target that is anything, but store a 4th location which is the enemy type, this will
 *  help when the enemy gardeners and archons are killed
 */
public class EnemyTracker {

    public static final int TRACKER_BASE = 90; // we take up slots 90-95 in broadcast memory

    public static int currRnd; // the current round

    // the following are read from the shared robot memory
    public static int enemyArchonID;            // it's id
    public static int enemyArchonLoc;           // it's location as a packed MapLocation
    public static int enemyArchonRndSighted;    // when it was last sighted
    public static int enemyGardenerID;          // it's id
    public static int enemyGardenerLoc;         // it's location as a packed MapLocation
    public static int enemyGardenerRndSighted;  // when it was last sighted

    public static void trackEnemies(RobotController rc, RobotInfo[] enemies) throws GameActionException {

        boolean foundArchon = false;
        boolean foundGarderner = false;
        boolean dirty = false; // true if we found something we need to write

        currRnd = rc.getRoundNum();

        // first get caught up with what other bots have found this round
        readCurrentSightings(rc);

        // look through our enemy sightings and see if there are any Archons or Gardeners
        for (int i = 0; i < enemies.length; i++) {
            RobotInfo e = enemies[i];
            if (foundArchon == false
                    && e.getType() == RobotType.ARCHON
                    && enemyArchonRndSighted < currRnd) {
                // we are the first to find an enemy Archon this turn, make it our target
                enemyArchonID = e.getID();
                enemyArchonLoc = CommChannel.packMapLocation(e.getLocation());
                enemyArchonRndSighted = currRnd;
                dirty = true;
            }
            if (foundGarderner == false
                    && e.getType() == RobotType.GARDENER
                    && enemyGardenerRndSighted < currRnd) {
                // we are the first to find an enemy Gardener this turn, make it our target
                enemyGardenerID = e.getID();
                enemyGardenerLoc = CommChannel.packMapLocation(e.getLocation());
                enemyGardenerRndSighted = currRnd;
                dirty = true;
            }

            if (foundArchon && foundGarderner) {
                // we found one of each, that's enough, stop looking
                break;
            }
        }

        if (dirty) {
            // we found something new, write it
            writeNewSightings(rc);
        }
    }

    public static void readCurrentSightings(RobotController rc) throws GameActionException {

        /* read what we currently know about enemy locations */

        int i = 0;
        enemyArchonID = rc.readBroadcast(TRACKER_BASE + i++);
        enemyArchonLoc = rc.readBroadcast(TRACKER_BASE + i++);
        enemyArchonRndSighted = rc.readBroadcast(TRACKER_BASE + i++);
        enemyGardenerID = rc.readBroadcast(TRACKER_BASE + i++);
        enemyGardenerLoc = rc.readBroadcast(TRACKER_BASE + i++);
        enemyGardenerRndSighted = rc.readBroadcast(TRACKER_BASE + i++);
    }

    public static void writeNewSightings(RobotController rc) throws GameActionException {

        /* write our new sightings */

        int i = 0;
        rc.broadcast(TRACKER_BASE + i++, enemyArchonID);
        rc.broadcast(TRACKER_BASE + i++, enemyArchonLoc);
        rc.broadcast(TRACKER_BASE + i++, enemyArchonRndSighted);
        rc.broadcast(TRACKER_BASE + i++, enemyGardenerID);
        rc.broadcast(TRACKER_BASE + i++, enemyGardenerLoc);
        rc.broadcast(TRACKER_BASE + i++, enemyGardenerRndSighted);
    }

    public static MapLocation getTargetLocation() {

        /* based on what we know, choose the best target */

        //System.out.println("in getTargetLocation");

        if (enemyGardenerRndSighted == currRnd || enemyGardenerRndSighted == currRnd - 1) {
            // a recent sighting, go after it
            return CommChannel.unpackMapLocation(enemyGardenerLoc);
        }

        if (enemyArchonRndSighted == currRnd || enemyArchonRndSighted == currRnd - 1) {
            // a recent sighting, go after it
            return CommChannel.unpackMapLocation(enemyArchonLoc);
        }

        return null;  // no good target
    }

    public static MapLocation getSearchPattern(MapLocation currentLocation) {

        //System.out.println("in getSearchPattern");

        /* move in a random direction a random longish distance, but make sure it is on the map */

        // XXX it would be best to do this in a loop along with onTheMap, but that requires an rc
        // come back to this later
        Direction dir = new Direction((float) Math.random() * 2 * (float) Math.PI);
        float dist = 30; // XXX 10 is scout sight radius.  (float)Math.random() * 10 + 10; // 10 to 20 units
        MapLocation target = currentLocation.add(dir, dist);
        return target;
    }
}
