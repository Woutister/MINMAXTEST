package Models;

public class Tree {
    private Node root;
    private Node bestNode;

    public Tree(Node root){
        this.root = root;
        bestNode = root;
    }

    public Node getRoot(){
        return root;
    }

    public void updateBestNode(Node bestNode){
        this.bestNode = bestNode;
    }

    public Node getBestNode(){
        return bestNode;
    }
}
