package Models;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private List<Node> children;
    private int[] board;
    private int move;
    private int eval;

    public Node(int[] board){
        this.board = board;
    }

    public void setMove(int move){
        this.move = move;
    }

    public void setEval(int eval){
        this.eval = eval;
    }

    public List getChildren(){
        return children;
    }

    public void addChild(Node child){
        children.add(child);
    }

    public int[] getBoard(){
        return  board;
    }

    public int getMove() {
        return move;
    }

    public int getEval() {
        return eval;
    }
}
