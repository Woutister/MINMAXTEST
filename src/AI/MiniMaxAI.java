package AI;

import Controllers.DataController;
import Controllers.ReversiController;
import Models.Node;
import Models.Tree;

import java.util.ArrayList;
import java.util.List;

public class MiniMaxAI {

    private DataController dataController;
    private ReversiController reversiController;
    private Tree tree;

    private static int MIN = Integer.MIN_VALUE;
    private static int MAX = Integer.MAX_VALUE;

    public MiniMaxAI(){
        this.dataController = DataController.getInstance();
        this.reversiController = new ReversiController();
    }

    private void buildTree(){
        Node root = new Node(dataController.getData());
        tree = new Tree(root);
    }

    public int getMove(int depth){
        buildTree();
        minMaxMove(tree.getRoot(), depth, playerNumber(), true, MIN, MAX);
        Node testNode = tree.getRoot();
        List<Node> kindjes = testNode.getChildren();
        int eval = MIN;
        Node bestNode = null;

        for(int i = 0; i < kindjes.size(); i++){
            Node node = kindjes.get(i);
            if(eval < node.getEval()){
                bestNode = node;
            }
        }
        return bestNode.getMove();
    }

    private int playerNumber(){
        if(reversiController.getPlayer1()){
            return 1;
        } else {
            return 2;
        }
    }


    private int minMaxMove(Node root, int depth, int playerTurn, boolean maxingPlayer, int alpha, int beta){
        ArrayList<Integer> moves = countMoves(reversiController.updatePossibleMoves(root.getBoard(), playerTurn));

        if(depth == 0 || moves.size() == 0){
            return evaluate(root);
        }

        if(maxingPlayer){
            int best = MIN;

            for(int i = 0; i < moves.size(); i++) {
                Node newNode = new Node(reversiController.calculateMove(moves.get(i), playerTurn, root.getBoard()));
                root.addChild(newNode);

                int val = minMaxMove(newNode, depth - 1, playerFlip(playerTurn), false, alpha, beta);

                best = Math.max(best, val);
                alpha = Math.max(alpha, best);

                if(beta <= alpha){
                    break;
                }
            }
            return best;
        } else {
            int best = MAX;

            for(int i = 0; i < moves.size(); i++){
                Node newNode = new Node(reversiController.calculateMove(moves.get(i), playerTurn, root.getBoard()));
                root.addChild(newNode);

                int val = minMaxMove(newNode, depth - 1, playerFlip(playerTurn), true, alpha, beta);

                best = Math.max(best, val);
                beta = Math.max(beta, best);

                if(beta <= alpha){
                    break;
                }
            }
            return best;
        }
    }

    private int playerFlip(int player) {
        if(player == 1){
            return 2;
        } else {
            return 1;
        }
    }

    private int evaluate(Node node){
        return 0;
    }



    private ArrayList countMoves(int[] possibleMoves){
        ArrayList<Integer> moves = new ArrayList<>();
        for(int i = 0; i < possibleMoves.length; i++){
            if (possibleMoves[i] == 1){
                moves.add(i);
            }
        }
        return moves;
    }

}
