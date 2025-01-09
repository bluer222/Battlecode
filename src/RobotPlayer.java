
package examplefuncsplayer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * RobotPlayer is the class that describes your main robot strategy. The run()
 * method inside this class is like your main function: this is what we'll call
 * once your robot is created!
 */

public class RobotPlayer {

    /*
    This RNG to make some random
    * moves. The Random class is provided by the java.util.Random import at the
     * top of this file. Here, we *seed* the RNG with a constant number (6147);
     * this makes sure we get the same sequence of numbers every time this code
     * is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /**
     * We will use this variable to count the number of turns this robot has
     * been alive. You can use static variables like this to save any
     * information you want. Keep in mind that even though these variables are
     * static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    //we need to remember the most recent paint tower we saw so we can go back
    //99 is a default because defaulting to 0 would mean we cant find a paint tower at coords 0, 0(it will register as not being found)
    static int rptx = 99; //rptx = recent paint tower x
    static int rpty = 99;

    //store the mapinfo objects for found things
    static MapInfo[] found = {};

    //when we start an exploration, we should start neer a paint tower and store it here
    static MapInfo start;

    //store all known freindly paint towers(in case of emergency)
    static MapInfo[] towers = {};

    //this is our exploration goal, in exploration the goal will be to travel to these coords and find stuff along the way
    //this will be set randomly in the explorer function(obviously the distance will need to be within reach)
    static int goalx = 99;
    static int goaly = 99;

    //movetowards function should be a boolg real life robots it seems li    /*
    //why whats a bool
    /*
    type
    starts at either 0 or 1
    this makes it so that some soldiers are explorers and others are builders
    at turn ??? randomly some of these will set type to 2 and become soldiers  
     */
    static int type = rng.nextInt(2);
    /**
     * Array containing all the possible movement directions.
     */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,};

    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world. It is like the main function for your robot. If this
     * method returns, the robot dies!
     *
     * @param rc The RobotController object. You use it to perform actions from
     * this robot, and to get information on its current status. Essentially
     * your portal to interacting with the world.
     *
     */
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm alive");

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");
        
        //the first two towers behave diferently so this will check if the tower is original
        static boolean isOriginal = false;
        
        if(rc.getRoundNum() == 1){
            isOriginal = true;
        }

        static UnitType type = rc.getType();
    
        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the UnitType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case SOLDIER:
                        runSoldier(rc);
                        break;
                    case MOPPER:
                        runMopper(rc);
                        break;
                    case SPLASHER:
                        runSplasher(rc);
                        break; // Consider upgrading examplefuncsplayer to use splashers!
                    default:
                        runTower(rc);
                        break;
                }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }
    //this code runs for the two original towers
    public static void runStartingTower(RobotController rc) throws GameActionException {
    }
        
    /*
     * Run a single turn for towers. This code is wrapped inside the infinite
     * loop in run(), so it is called once per turn.
     */
    public static void runTower(RobotController rc) throws GameActionException {
        //if the turn is even
        if (static int turnCount % 2 == 0){
            static RobotInfo[] nearbyEnemies = findEnemyRobots(rc);
            if (nearbyEnemies.length != 0) {
                rc.setIndicatorString("ENNEMIES!!!!");
                
            }
            //if this is the first turn and this is a starting tower
            if(isOriginal && turnCount = 1){
            if(type.isTowerType(LEVEL_ONE_MONEY_TOWER)){
                //we are the money tower
                //we need to make two soldiers
            }
            if(type.isTowerType(LEVEL_ONE_PAINT_TOWER)){
                //we are the paint tower
                //we need to make one splasher
            }        
        }
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        //create a maplocation thats the currentlocation + direction
        MapLocation nextLoc = rc.getLocation().add(dir);
        // Pick a random robot type to build.
        int robotType = rng.nextInt(3);
        //build it
        if (robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER");
        } else if (robotType == 1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
            rc.buildRobot(UnitType.MOPPER, nextLoc);
            System.out.println("BUILT A MOPPER");
        } else if (robotType == 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)) {
            rc.buildRobot(UnitType.SPLASHER, nextLoc);
            System.out.println("BUILT A SPLASHER");
        }

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }

        // TODO: can we attack other bots?
    }

    /**
     * Run a single turn for a Soldier. This code is wrapped inside the infinite
     * loop in run(), so it is called once per turn.
     */
    public static void runSoldier(RobotController rc) throws GameActionException {
        // Sense information about all visible nearby tiles.
        //f
        //get our current location
        Maplocation currenLoc = rc.getLocation();
        //get our ammount of paint
        Maplocation currentPaint = rc.getPaint();

        //we are a builder/explorer
        if (type == 0) {
           //what is this
            //oh i thought it was for the explorers
            //the eplorers need more complex code so when they move they check all of the tiles that are newly visable
            //we havent set a goal yet
            if (goalx == 99) {
                //lets set a location
                //
                //wait how do we get current location
                //theres something on it in the documentation
            }
            //lets search our surriondings
            //we should coordinate
            //ok
            //just set a random goal coord and it will end up going in diff direcitons

            MapInfo curRuin = null;
            for (MapInfo tile : nearbyTiles) {

                if (tile.hasRuin()) {
                    curRuin = tile;
                }
                if (tile.hasRuin()) {
                    curRuin = tile;
                }
            }
            //we found
            // Search for a nearby ruin to complete.
            //shouldnt we go to the coords that explorers send us
            //so you want to just wait and waste time until we get an explorer
            //what if the explorers were builders
            //and just built on the ruin if the had enough paint
            //if not then they go back, replenish then return
            //ok yeah lets just have one type

            if (curRuin != null) {

            }
        } else if (type == 1) {
            //we are a explorer

        }

        //if we found a ruin
        if (curRuin != null) {
            //get the location
            MapLocation targetLoc = curRuin.getMapLocation();
            Direction dir = rc.getLocation().directionTo(targetLoc);
            //move twards it
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
            // Mark the pattern we need to draw to build a tower here if we haven't already.
            MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
            if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)) {
                rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
                System.out.println("Trying to build a tower at " + targetLoc);
            }
            // Fill in any spots in the pattern with the appropriate paint.
            for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)) {
                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY) {
                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
                    if (rc.canAttack(patternTile.getMapLocation())) {
                        rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                    }
                }
            }
            // Complete the ruin if we can.
            if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)) {
                rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
                rc.setTimelineMarker("Tower built", 0, 255, 0);
                System.out.println("Built a tower at " + targetLoc + "!");
            }
        }

        // Move and attack randomly if no objective.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        // Try to paint beneath us as we walk to avoid paint penalties.
        // Avoiding wasting paint by re-painting our own tiles.
        MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
        if (!currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())) {
            rc.attack(rc.getLocation());
        }
    }

    public static void runSplasher(RobotController rc) throws GameActionException {

    }

    /**
     * Run a single turn for a Mopper. This code is wrapped inside the infinite
     * loop in run(), so it is called once per turn.
     */
    public static void runMopper(RobotController rc) throws GameActionException {
        // Move and attack randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        if (rc.canMopSwing(dir)) {
            rc.mopSwing(dir);
            System.out.println("Mop Swing! Booyah!");
        } else if (rc.canAttack(nextLoc)) {
            rc.attack(nextLoc);
        }
        // We can also move our code into different methods or classes to better organize it!
        updateEnemyRobots(rc);
    }
    //my own version
    //mathias, what are you doing
    // i think you mean to put this in the tower funciton
    public static void findEnemyRobots(RobotController rc) throws GameActionException {
        //find nearby robots
        static RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        //create an array, this will store enemies
        static RobotInfo[] nearbyEnemies;
        
        int a = 0;
        //go through each bot
        for (int i = 0; i < nearbyRobots.length; i++) {
            if(nearbyRobots[i].getTeam().opponent()){
                nearbyEnemies[a] = nearbyRobots[i];
                a++
            }
        }
        return nearbyEnemies;
    }
    public static void updateEnemyRobots(RobotController rc) throws GameActionException {
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        //how tf does this work
        //this has no readability
        //wtf mit
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        //hey mathias
        //i'm remaking this function right above here in findEnemyRobots
        if (enemyRobots.length != 0) {
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for possible future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++) {
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
            // Occasionally try to tell nearby allies how many enemy robots we see.
            if (rc.getRoundNum() % 20 == 0) {
                for (RobotInfo ally : allyRobots) {
                    if (rc.canSendMessage(ally.location, enemyRobots.length)) {
                        rc.sendMessage(ally.location, enemyRobots.length);
                    }
                }
            }
        }
    }
    //figure out the best direction to move given a destination and start
    public static Direction moveTowards(MapLocation currentLoc, MapLocation endLoc){
        Direction dir;
        boolean picknext = false; 
        //check if it's right
        if (endLoc.x > currentLoc.x || picknext){
            //check if it's up right
            if (endLoc.y > currentLoc.y || picknext){
                if (rc.canMove(Direction.NORTHEAST)){
                    return Direction.NORTHEAST;
                } else {
                    picknext = true;
                }
            //if not it might be down right
            } 
            if (endLoc.y < currentLoc.y || picknext) {
                if (rc.canMove(Direction.SOUTHEAST)){
                    return Direction.SOUTHEAST;
                }else {
                    picknext = true;
                }
            //if not then it's directly right
            } else {
                if (rc.canMove(Direction.EAST)){
                    return Direction.EAST;
                }else {
                    picknext = true;
                }
            }
        //alr bro it ain't that hard figure it out
        } 
        if (endLoc.x < currentLoc.x || picknext){
            if (endLoc.y > currentLoc.y || picknext){
                if (rc.canMove(Direction.NORTHWEST)){
                    return Direction.NORTHWEST;
                }else {
                    picknext = true;
                }
            } 
            if (endLoc.y < currentLoc.y || picknext) {
                if (rc.canMove(Direction.SOUTHWEST)){
                    return Direction.SOUTHWEST;
                }else {
                    picknext = true;
                }
            } else {
                if (rc.canMove(Direction.WEST)){
                    return Direction.WEST;
                }else {
                    picknext = true;
                }
            }
        } 
        if (endLoc.y > currentLoc.y || picknext) {
            if (rc.canMove(Direction.UP)){
                    return Direction.UP;
                }else {
                    picknext = true;
                }
        } 
        if (endLoc.y > currentLoc.y || picknext){
            if (rc.canMove(Direction.DOWN)){
                    return Direction.DOWN;
                }else {
                    picknext = true;
                }
        } else {
            return Direction.CENTER;
        }

        //Generates a random map location the farthest distance a player can move
        //If all maps are square, you can merge x and y size
        //This doesn't pythagorean theorem this just uses max side length of that triange
        //I'll probably add pythagorean theorem to calculate that tomorrow
        public static MapLocation setFarthest(MapLocation currentLoc, int maxDistance, int mapXSize, int mapYSize){
            int sideLength = maxDistance * 2;
            int x = currentLoc.x - maxDistance + random.nextInt(sideLength + 1);
            int y = currentLoc.y - maxDistance + random.nextInt(sideLength + 1);

            MapLocation newLoc.x = x;
            MapLocation newLoc.y = y;
            //clamps the x and y to inside the map.
            newLoc.x = clamp(newLoc.x, 0, mapXSize);
            newLoc.y = clamp(newLoc.y, 0, mapYSize);
            return newLoc;
        }