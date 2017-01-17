package nslbattlecodemaster.util;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

/**
 * Created by morris on 1/16/17.
 *
 * Keep Track of enemys.  Particularly Archons, though maybe that can be generalized in the future.
 *
 * We track up to NUM_ENEMIES enemy positions.  For each one we record it's  id, location, and the round
 * it was last seen.
 */
public class EnemyTracker {

    int memoryBase;
    int numToTrack;
    RobotType typeToTrack;
    int[] enemyLocations = null;
    int[] roundLastSighted;

    EnemyTracker(int memBase, int num, RobotType enemyType) {
        memoryBase = memBase;
        numToTrack = num;
        typeToTrack = enemyType;


    }

    public void trackEnemies(RobotController rc, RobotInfo[] enemies) {

        boolean dirty = false;
        int currRnd = rc.getRoundNum();

        for (int i = 0; i < enemies.length; i++) {
            RobotInfo e = enemies[i];
            if (e.getType() == typeToTrack) {
                // we want to track it
                if (enemyLocations == null)
                    readCurrentSightings(rc); // just have to do this once per turn
                MapLocation loc = e.getLocation(); // get it's location
                int intLoc = CommChannel.packMapLocation(loc);
                // see if we already have a sighting this round
                for (int j = 0; j < numToTrack; j++) {
                    if (enemyLocations[j] == intLoc) {
                        // someone saw something there.  How long ago
                        //ZZZ
                    }
                }

            }
        }
    }

    public void readCurrentSightings(RobotController rc) {
        enemyLocations = new int[numToTrack];
        roundLastSighted = new int[numToTrack];
        // ZZZ now read them

    }
}
