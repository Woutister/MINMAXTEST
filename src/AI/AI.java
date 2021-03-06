package AI;

import Controllers.DataController;
import Controllers.ReversiController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AI {
    private DataController dataController;
    private int totalTiles;
    private MiniMaxAI miniMaxAI;
    public AI() {
        this.dataController = DataController.getInstance();
        miniMaxAI = new MiniMaxAI();

        totalTiles = 0;
    }

    /**
     * Makes a move depending on the AI difficulty, 0 for a random move, 1 for a max priority move, 2 for a hardMove(C)
     * @param dataset the board
     * @param pMoves the possible moves
     * @return the move that the AI decided on
     */
    public int makeMove(int[] dataset, int[] pMoves) {
        int move;
        int amountofmoves = 0;
        int lastmove = -1;
        for(int i = 0 ; i < pMoves.length;i++){
            if(pMoves[i] == 1){
                amountofmoves++;
                lastmove = i;
            }
        }
        if(amountofmoves == 1){
            return lastmove;
        }
        switch (dataController.getAiDifficulty()) {
            case 0: {
                move = randomMove(pMoves);
                break;
            }
            case 1: {
                try{
                    ArrayList<Integer> moves = maxPrioMove(dataset, getPriotitymoves(pMoves));
                    move = moves.get(new Random().nextInt(moves.size()));
                }catch(Exception e){
                    move = randomMove(pMoves);
                }

                break;
            }
            case 2:{
                move = hardMove(dataset, getPriotitymoves(pMoves));
                break;
            }
            case 3:{
                move = miniMaxAI.getMove(7);
                System.out.println("the move is: " + move);
                break;
            }
            default: {
                move = -1;
                System.out.println("Not a difficulty option");
                break;
            }
        }
        return move;
    }



    /**
     * Chooses a random move from all the possible Moves
     * Checks for each move if it is possible, if it is it will save the index from that move
     * It chooses a random index from the saved indexes
     * It returns the move associated with the random index
     * @param possibleMoves an int[] from the board where 0 = not possible, 1 = possible
     * @return int move The index on the board that is the move made
     */
    private int randomMove(int[] possibleMoves) {
        System.out.println("Making random move");
        Random random = new Random();
        ArrayList<Integer> moves = new ArrayList<>(possibleMoves.length);
        for(int i = 0; i < possibleMoves.length; i++) {
            if(possibleMoves[i] == 1) {
                moves.add(i);
            }
        }
        moves.trimToSize();
        //System.out.println(moves.size());
        int index = random.nextInt(moves.size());
        int move = moves.get(index);

        return move;
    }

    /**
     * Checks the hardavoidance moves first, then the soft avoidance, then the most tiles and the max prio move to come up with the best move
     * @param dataset the board
     * @param priorityMoves the moves assigned to their priority
     * @return
     */
    private int hardMove(int[] dataset ,HashMap<Integer, ArrayList<Integer>> priorityMoves){
        ArrayList<Integer> moves = new ArrayList<>();
        System.out.println("Making hard move");

        // check if hardAV move is good
        moves.addAll(this.checkHardAv(dataset,priorityMoves));
        if(moves.size() > 0){
            return this.getMostTileMove(dataset, moves);
        }

        // check if softAV move is good
        moves.addAll(this.checkSoftAv(dataset,priorityMoves));
        if(moves.size() > 0){
            return this.getMostTileMove(dataset, moves);
        }


        // get max tiles based on priority
        try{
            moves.addAll(this.maxPrioMove(dataset,priorityMoves));
        }catch(Exception e){

        }

        if(moves.size() == 0){
            //System.out.println("moves size = 0");
            ReversiController controller = new ReversiController();
            return this.randomMove(controller.updatePossibleMoves(dataset, getplayer()));
        }
        Random r = new Random();
        int index = r.nextInt(moves.size());
        return moves.get(index);

    }

    /**
     * Calculates a move(s) that leaves the opponent with the worst possible moves
     * @param dataset the board
     * @param priorityMoves the moves assigned to their priority
     * @return an ArrayList with all the moves
     */
    private ArrayList<Integer> blockingMove(int[] dataset ,HashMap<Integer, ArrayList<Integer>> priorityMoves) {
        ReversiController controller = new ReversiController();
        ArrayList<Integer> moves = new ArrayList<>();
        if(priorityMoves.get(4).size() > 0) {
            moves = priorityMoves.get(4);
        } else if(priorityMoves.get(3).size() > 0) {
            moves = priorityMoves.get(3);

        } else if(priorityMoves.get(2).size() > 0) {
            moves = priorityMoves.get(2);

        } else if(priorityMoves.get(1).size() > 0) {
            moves = priorityMoves.get(1);

        } else if(priorityMoves.get(0).size() > 0) {
            moves = priorityMoves.get(0);
        }

        ArrayList<Integer> blockingMoves = new ArrayList<>();
        int prefPrio = 0;

        int player = getplayer();
        int opponent = getopponent();

        for(int move : moves) {
            int[] newBoard = controller.calculateMove(move, player, dataset);
            int[] opponentPm = controller.updatePossibleMoves(newBoard, opponent);
            int amount = 0;
            int priosum = 0;
            if(getPriotitymoves(opponentPm).get(4).size() > 0){
                // this move gives opponent a corner.
                continue;
            }

            priosum += getPriotitymoves(opponentPm).get(0).size();
            priosum += getPriotitymoves(opponentPm).get(1).size() * 1;
            priosum += getPriotitymoves(opponentPm).get(2).size() * 2;
            priosum += getPriotitymoves(opponentPm).get(3).size() * 3;

            amount += getPriotitymoves(opponentPm).get(0).size();
            amount += getPriotitymoves(opponentPm).get(1).size();
            amount += getPriotitymoves(opponentPm).get(2).size();
            amount += getPriotitymoves(opponentPm).get(3).size();

            // no moves for opponent
            if(amount == 0){
                blockingMoves.clear();
                blockingMoves.add(move);
                return blockingMoves;
            }

            int prio = priosum / amount;

            if(prio < prefPrio){
                blockingMoves.clear();
                blockingMoves.add(move);
            }

            if(prio == prefPrio){
                blockingMoves.add(move);
            }
        }
        return blockingMoves;
    }

    /**
     * Combines the number of tiles gained from a move and the priority of the move, the move with the highest score
     * is made, if there are more moves that have the same score a random move is picked from those options.
     * It also relies on the future opponent tile gain and player tile loss to make a decision
     * @param dataset
     * @param priorityMoves
     * @return an ArrayList of move indexes
     */
    private ArrayList<Integer> maxPrioMove(int[] dataset ,HashMap<Integer, ArrayList<Integer>> priorityMoves) {
        ReversiController controller = new ReversiController();

        ArrayList<Integer> doableMoves = new ArrayList<>();

        int player = getplayer();
        int opponent = getopponent();

        int prevPlayerTiles = 0;
        double prevMovePoint = 0;
        int prevTilePoints = 0;
        int lastAmount = 0;

        for(Map.Entry entrySet : priorityMoves.entrySet()) {
            int prio = (int) entrySet.getKey();

            ArrayList<Integer> moves = (ArrayList) entrySet.getValue();
            for(int move : moves) {
                int playerTiles = 0;
                int opponentTiles = 0;

                int[] newBoard = controller.calculateMove(move, player, dataset);
                for (int cell : newBoard) {
                    if (cell == player) {
                        playerTiles++;
                    } else if (cell == opponent) {
                        opponentTiles++;
                    }
                }
                int[] opponentPM = controller.updatePossibleMoves(newBoard, opponent);
                ArrayList<Integer> opponentPmlist = new ArrayList<>();
                for(int i = 0; i < opponentPM.length;i++){
                    if(opponentPM[i] == 1){
                        opponentPmlist.add(i);
                    }
                }
                int predictionMove = this.predictOpponent(newBoard, opponentPmlist, opponent);
                int size = this.getPriotitymoves(controller.updatePossibleMoves(controller.calculateMove(predictionMove,opponent,newBoard),opponent)).get(4).size();
                if(size > 0){
                    if(size > lastAmount ){
                        lastAmount = size;
                        continue;
                    }
                }

                double movePoint = 0.0;
                double prioWorth = 0.85;
                double tileWorth = 0.15;
                if(prio != 0) {
                    if(prio == 4) {
                        movePoint = (0.95 * prio) + (0.05 * playerTiles);
                    } else {
                        movePoint = (prioWorth * prio) + (tileWorth * playerTiles);
                    }
                } else {
                    movePoint = tileWorth * playerTiles+1;
                }
                totalTiles += playerTiles;
                //System.out.println("Total Tiles: " + totalTiles);

                int opponentMove = predictOpponent(newBoard, moves, opponent);

                int[] secondNewBoard = controller.calculateMove(opponentMove,opponent,newBoard);
                int newOpponentTiles = 0;
                int newPlayerTiles = 0;

                for(int tile : secondNewBoard) {
                    if(tile == opponent) {
                        newOpponentTiles++;
                    } else if(tile == player) {
                        newPlayerTiles++;
                    }
                }

                int opponentGain = newOpponentTiles - opponentTiles;
                int playerLoss = newPlayerTiles - playerTiles;

                int threshhold = opponentTiles / 3;
                /*System.out.println("Move " + move);
                System.out.println("Priority " + prio);
                System.out.println("Opponentgain " + opponentGain);
                System.out.println("PlayerLoss " + playerLoss);
                System.out.println("Threshold " + threshhold);
                System.out.println("MovePoints " + movePoint);*/

                //make choice based on thresholds of loss and gain
                if(opponentGain < threshhold && playerLoss > -threshhold) {
                    //make choice based on movepoints
                    if (movePoint >= prevMovePoint) {
                        prevMovePoint = movePoint;
                        doableMoves.clear();
                        //System.out.println("List cleared");
                        doableMoves.add(move);
                        //System.out.println("Move added to list");
                    } else if (movePoint == prevMovePoint) {
                        //System.out.println("Move added to existing list");
                        doableMoves.add(move);
                    }
                } else if(doableMoves.size() == 0) {
                    doableMoves.add(move);
                }


            }
        }

        return doableMoves;
    }

    /**
     * Checks if a move with priority 0 (hard avoidance) is a move that may be useful
     * @param dataset the current board
     * @param priorityMoves Hashmap with all the moves assigned to their key priority
     * @return ArrayList of move indexes
     */
    private ArrayList<Integer> checkHardAv(int[] dataset, HashMap<Integer, ArrayList<Integer>> priorityMoves){
        ArrayList<Integer> moves = new ArrayList<>();
        ArrayList<Integer> avoid = this.getHardDiagonalAvoidance();
        if(priorityMoves.get(4).size() > 0){
            return priorityMoves.get(4);
        }
        if(priorityMoves.get(0).size() > 0){
            ArrayList<Integer> hardAv = priorityMoves.get(0);
            for(int pmmove : hardAv){
                if(avoid.contains(pmmove)){
                    continue;
                }
                int [] opponentboard = this.calculateOpponentmoves(dataset, pmmove);
                HashMap<Integer, ArrayList<Integer>> opponentPriorityMoves = this.getPriotitymoves(opponentboard);
                if(opponentPriorityMoves.get(4).size() == 0){
                    moves.add(pmmove);
                }
            }
        }
        return moves;
    }

    /**
     * Checks if a move with priority 1 (soft avoidance) may be usefull
     * @param dataset the current board
     * @param priorityMoves Hashmap with all the moves assigned to their key priority
     * @return ArrayList of move indexes
     */
    private ArrayList<Integer> checkSoftAv(int[] dataset, HashMap<Integer, ArrayList<Integer>> priorityMoves) {
        ArrayList<Integer> moves = new ArrayList<>();
        ReversiController controller = new ReversiController();

        boolean equal = false;
        int player = getplayer();
        int opponent = getopponent();


        if(priorityMoves.get(3).size() > 0){
            HashMap<Integer, ArrayList<Integer>> oldmoves = this.getPriotitymoves(controller.updatePossibleMoves(dataset, opponent));
            for(int sidemove : priorityMoves.get(3)){
                HashMap<Integer, ArrayList<Integer>> newmoves = this.getPriotitymoves(controller.updatePossibleMoves(controller.calculateMove(sidemove, player, dataset), opponent));
                if(newmoves.get(4).size() == 0 || newmoves.get(3).size() < oldmoves.get(3).size()){
                    moves.add(sidemove);
                }
            }
            return moves;
        }

        HashMap<Integer, ArrayList<Integer>> opponentmoves = this.getPriotitymoves(controller.updatePossibleMoves(dataset, opponent));
        int opponentprio1moves = opponentmoves.get(1).size();

        for(int move : priorityMoves.get(1)) {
            HashMap<Integer, ArrayList<Integer>> newmoves = this.getPriotitymoves(controller.updatePossibleMoves(controller.calculateMove(move, player, dataset), opponent));
            if(newmoves.get(3).size() > opponentprio1moves){
                continue;
            }
            if(newmoves.get(3).size() < opponentprio1moves){
                if(equal){
                    moves.clear();
                    equal = false;
                }
                moves.add(move);
            }
            if(newmoves.get(3).size() == opponentprio1moves){
                moves.add(move);
                equal = true;
            }
        }

        return moves;
    }


    /**
     * Predicts the move the opponents is likely to make if they want to get the max tiles
     * @param board
     * @param possibleMoves
     * @param opponent
     * @return
     */
    private int predictOpponent(int[] board, ArrayList<Integer> possibleMoves, int opponent) {
        int move = -1;

        int numTiles = 0;
        int prevNumTiles = 0;

        ReversiController controller = new ReversiController();
        for(int index : possibleMoves) {
            int[] newBoard = controller.calculateMove(index,opponent,board);
            for(int a = 0; a< newBoard.length; a++) {
                if(newBoard[a] == opponent) {
                    numTiles++;
                }
            }
            if(numTiles > prevNumTiles) {
                prevNumTiles = numTiles;
                move = index;
            }

        }

        return move;
    }

    private boolean aheadOfOpponent(int[] dataset){
        int player = getplayer();
        int opponent = getopponent();


        int playerTiles = 0;
        int opponentTiles = 0;

        for(int data : dataset){
            if(data == 0){
                continue;
            }
            if(data == player){
                playerTiles++;
            }
            if(data == opponent){
                opponentTiles++;
            }
        }

        if(playerTiles > opponentTiles ){
            return true;
        }

        return false;
    }

    /**
     * Gets the total amount of tiles on the board
     * @param board the board
     * @return total amount of tiles
     */
    private int getTotalTiles(int[] board){
        int totaltiles = 0;
        for (int move : board){
            if(move != 0){
                totaltiles++;
            }
        }
        return totaltiles;
    }

    /**
     * Loops to the possible moves int[] and creates a hashmap that assigns a priority as key with its value
     * an ArrayList with all the moves with that priority
     * @param pmdataset the possiblemoves int[]
     * @return a hashmap<Integer, ArrayList<Integer>>
     */
    private HashMap<Integer, ArrayList<Integer>> getPriotitymoves(int[] pmdataset){
        HashMap<Integer, ArrayList<Integer>> priomoves = new HashMap<>();
        ArrayList<Integer> cornerMoves = new ArrayList<>();
        ArrayList<Integer> sideMoves = new ArrayList<>();
        ArrayList<Integer> softAvMoves = new ArrayList<>();
        ArrayList<Integer> hardAvMoves = new ArrayList<>();
        ArrayList<Integer> restMoves = new ArrayList<>();


        for(int i = 0; i < pmdataset.length; i++){
            if(pmdataset[i] == 0){
                continue;
            }

            if(getCorners().contains(i)){
                cornerMoves.add(i);
            }
            else if(getSides().contains(i)){
                sideMoves.add(i);
            }
            else if(getSoftAvoidance().contains(i)){
                softAvMoves.add(i);
            }
            else if(getHardAvoidance().contains(i)){
                hardAvMoves.add(i);
            }
            else{
                restMoves.add(i);
            }
        }

        priomoves.put(4,cornerMoves);
        priomoves.put(3,sideMoves);
        priomoves.put(2,restMoves);
        priomoves.put(1,softAvMoves);
        priomoves.put(0,hardAvMoves);

        return priomoves;
    }

    /**
     * Calculates the board if the opponent makes a certain move
     * @param board the old board
     * @param move the move the will be made
     * @return the new board after the move is made
     */
    private int[] calculateOpponentmoves(int[] board, int move){
        int player = 2;
        int opponent = 1;

        if(dataController.getPlayerOne()){
            player = 1;
            opponent = 2;
        }

        ReversiController controller = new ReversiController();
        int[] newboard = controller.calculateMove(move, player, board);

        return controller.updatePossibleMoves(newboard, opponent);
    }

    /**
     * Calculates the best move if you only want the most tiles
     * @param dataset the current board
     * @param goodmoves the moves that are doable
     * @return the move that gains the most tiles
     */
    private int getMostTileMove(int[] dataset, ArrayList<Integer> goodmoves){
        int mostTiles = 0;
        int bestmove = -1;

        int player = getplayer();

        ReversiController controller = new ReversiController();
        for(int i : goodmoves){
            int[] newboard = controller.calculateMove(i,player, dataset);
            int total = this.getTotalTiles(newboard);
            if(total >= mostTiles){
                mostTiles = total;
                bestmove = i;
            }
        }
        return bestmove;
    }

    /**
     * returns 1 or 2 depending on which player you are
     * @return 1 or 2
     */
    private int getplayer(){
        int player = 2;
        if(dataController.getPlayerOne()){
            player = 1;
        }
        return player;
    }

    /**
     * returns 1 or 2 depending on which player the opponent is
     * @return 1 or 2
     */
    private int getopponent(){
        int opponent = 1;
        if(dataController.getPlayerOne()){
            opponent = 2;
        }
        return opponent;
    }


    /**
     * Creates and fills an ArrayList with all the indexes that are corners
     * @return an arraylist with indexes
     */
    private ArrayList<Integer> getCorners() {
        ArrayList<Integer> corners = new ArrayList<>();
        corners.add(0);
        corners.add(7);
        corners.add(56);
        corners.add(63);
        return corners;
    }

    /**
     * Creates and fills an ArrayList with all the indexes that are sides
     * @return an arraylist with indexes
     */
    private ArrayList<Integer> getSides() {
        ArrayList<Integer> sides = new ArrayList<>();
        sides.add(2);
        sides.add(3);
        sides.add(4);
        sides.add(5);
        sides.add(16);
        sides.add(24);
        sides.add(32);
        sides.add(40);
        sides.add(23);
        sides.add(31);
        sides.add(39);
        sides.add(47);
        sides.add(58);
        sides.add(59);
        sides.add(60);
        sides.add(61);
        return sides;
    }

    /**
     * Creates and fills an ArrayList with all the indexes that are soft avoidance
     * @return an arraylist with indexes
     */
    private ArrayList<Integer> getSoftAvoidance() {
        ArrayList<Integer> softAvoidance = new ArrayList<>();
        softAvoidance.add(10);
        softAvoidance.add(11);
        softAvoidance.add(12);
        softAvoidance.add(13);
        softAvoidance.add(17);
        softAvoidance.add(25);
        softAvoidance.add(33);
        softAvoidance.add(41);
        softAvoidance.add(22);
        softAvoidance.add(30);
        softAvoidance.add(38);
        softAvoidance.add(46);
        softAvoidance.add(50);
        softAvoidance.add(51);
        softAvoidance.add(52);
        softAvoidance.add(53);
        return softAvoidance;
    }

    /**
     * Creates and fills an ArrayList with all the indexes that are hard avoidance
     * @return an arraylist with indexes
     */
    private ArrayList<Integer> getHardAvoidance() {
        ArrayList<Integer> hardAvoidance = new ArrayList<>();
        hardAvoidance.add(1);
        hardAvoidance.add(8);
        hardAvoidance.add(9);
        hardAvoidance.add(6);
        hardAvoidance.add(14);
        hardAvoidance.add(15);
        hardAvoidance.add(48);
        hardAvoidance.add(49);
        hardAvoidance.add(57);
        hardAvoidance.add(54);
        hardAvoidance.add(55);
        hardAvoidance.add(62);
        return hardAvoidance;
    }

    /**
     * Creates and fills an ArrayList with all the indexes that are diagonal to hard avoidance indexes
     * @return an arraylist with indexes
     */
    private ArrayList<Integer> getHardDiagonalAvoidance(){
        ArrayList<Integer> hardAvoidance = new ArrayList<>();
        hardAvoidance.add(9);
        hardAvoidance.add(14);
        hardAvoidance.add(49);
        hardAvoidance.add(54);
        return hardAvoidance;
    }
}