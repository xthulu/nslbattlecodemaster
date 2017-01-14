package nslbattlecodemaster.util;

import battlecode.common.*;

/**
 * Created by morris on 1/13/17.
 *
 * Facilities for reading and writing to the broadcast array.  The first int in the array is reserved for
 * assigning channels to communicate on.  The Channels start at int 100, and each channel is made up of 10
 * ints.  Since the broadcast array is of length 1000, we can potentially support 90 channels.
 *
 * See GaScoutRush and ArComm for examples of how these are used.
 */
public class CommChannel {

    public static final int UNKNOWN_CHANNEL = -1;
    public static final int NEXT_CHANNEL_CHANNEL = 0;
    public static final int CHANNEL_BASE = 100;
    public static final int CHANNEL_SIZE = 10;

    public int myChannel = UNKNOWN_CHANNEL;

    // values read from another robot's channel
    public int robotID;
    public int robotType;
    public int roundSent;
    public int x;
    public int y;
    public int senseEnemy;
    public int senseEnemyArchon;

    public void assignNewChannel(RobotController rc) {

        /*  Before writing you must get a channel assigned to you.  This should be done
            in your run method, not in your constructor.  Note that this does write
            to the broadcast array.
         */

        try {
            // read the NEXT_CHANNEL_CHANNEL, it counts how many channels have
            // been assigned, it's the base channel we will use to communicate
            int nextChannel = rc.readBroadcast(NEXT_CHANNEL_CHANNEL);
            myChannel = (nextChannel * CHANNEL_SIZE) + CHANNEL_BASE;
            rc.broadcast(NEXT_CHANNEL_CHANNEL,nextChannel + 1);
            //System.out.println("assigned new channel "+ myChannel);
            // XXX check if BROADCAST_MAX_CHANNELS is hit
        } catch (Exception e) {
            System.out.println("assignNewChannel Exception");
            e.printStackTrace();
        }
    }

    public void writeChannelHeader(RobotController rc, int myType, boolean sensesEnemy,
                                   boolean sensesEnemyArchon) throws GameActionException {

        /* Write useful information to my channel:
                my id - so that the recipient knows who was writing, especially if they want to write back
                my robot type - so they know what kind of robot was writing
                the game round when I wrote - so they know if it was recent, and can guess if I'm still alive
                my x/y position - note it's int not float, so not 100% precise
                if I see any enemy robots - so they can form a plan
                if I see an enemy archon - so they can react

           This totals 5 ints, so there are 5 more ints on this channel.  Feel free to use them.

           Also feel free to use the channel for your own stuff, you don't have to use this.

           A note about robot type - it would be nice if we could use RobotType.ARCHON for example,
           but that is an enum, and the broadcast array is made of ints.  There is no easy conversion.
           So for now, your app will have to define it's own type.
         */
        if (myChannel == UNKNOWN_CHANNEL || myChannel < CHANNEL_BASE || myChannel >= GameConstants.BROADCAST_MAX_CHANNELS) {
            throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE,"not a valid channel");
        }
        rc.broadcast(myChannel,rc.getID());
        rc.broadcast(myChannel + 1, myType);
        rc.broadcast(myChannel + 2, rc.getRoundNum());

        MapLocation myLoc = rc.getLocation();
        // GameConstants.MAP_MAX_HEIGHT = 1000 so we need to shift X far enough to leave room for y to be 1000
        int integerLoc = ((int) myLoc.x) << 16;
        integerLoc += (int) myLoc.y;
        rc.broadcast(myChannel + 3, integerLoc);

        int enemyFlags = 0;
        if (sensesEnemy)
            enemyFlags = 0x1; // bit 1
        if (sensesEnemyArchon)
            enemyFlags = enemyFlags | 0x10; // 2nd bit
        rc.broadcast(myChannel + 4, enemyFlags);
    }

    public boolean readChannelHeader(RobotController rc, int channel) throws GameActionException {

        /* Read the channel headers and store the data in our instance variables */

        if (channel == UNKNOWN_CHANNEL || channel < CHANNEL_BASE || channel >= GameConstants.BROADCAST_MAX_CHANNELS) {
            throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE,"not a valid channel");
        }
        robotID = rc.readBroadcast(channel);
        robotType = rc.readBroadcast(channel + 1);
        roundSent = rc.readBroadcast(channel + 2);

        int integerLoc = rc.readBroadcast(channel + 3);
        x = integerLoc >> 16;
        y = integerLoc & 0xFFFF;

        int enemyFlags = rc.readBroadcast(channel + 4);
        senseEnemy = enemyFlags & 0x01;
        senseEnemyArchon = enemyFlags & 0x02;

        //System.out.println("read Channel " + channel + " " + this);

        return true;
    }

    public String toString() {
        return "CommChannel id: " + robotID +
                " type " + robotType +
                " roundSent " + roundSent +
                " x" + x +
                " y" + y +
                " senseEnemy " + senseEnemy +
                " senseEnemyArchon " + senseEnemyArchon;
    }

    public void dbgChannels(RobotController rc) {
        // go through and print channels
        try {
            for (int i = 0; i < 70; i++) {
                int channel = CHANNEL_BASE + (i * CHANNEL_SIZE);
                readChannelHeader(rc,channel);
                if (robotID <= 0)
                    break; // no more robots
                System.out.println("read Channel " + channel + " " + this);
            }
        } catch (Exception e) {
            System.out.println("assignNewChannel Exception");
            e.printStackTrace();
        }
    }

    public int countLivingRobots(RobotController rc, int interestingType, int window) {
        // go through all the channels looking for robots of the
        // given type that have updated within the last window rounds
        // interestingType == -1 means count all robots
        //System.out.println("in countLivingRobots "+interestingType+" "+window);
        try {
            int currRnd = rc.getRoundNum();
            int cnt = 0;
            int i = 0;
            while (true) {
                int channel = CHANNEL_BASE + (i * CHANNEL_SIZE);
                readChannelHeader(rc,channel);
                if (robotID <= 0) {
                    //System.out.println("countLivingRobots returns "+cnt+ " on round "+currRnd);
                    return (cnt); // no more robots
                }
                if (interestingType == -1 || robotType == interestingType) {
                    // it's one we're interested in
                    if (roundSent + window >= currRnd)
                        cnt++; // and it was recently alive, count it
                }
                i++; // read to next channel
            }
        } catch (Exception e) {
            System.out.println("assignNewChannel Exception");
            e.printStackTrace();
        }
        return 0;
    }
}
