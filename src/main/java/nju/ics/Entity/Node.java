package nju.ics.Entity;


import java.util.List;

public class Node implements Cloneable {
    /* attributes */
    public String index;
    public String name;
    public NodeType type;
    public NodeSource source; // for testing
    public Node mutualNode = null;
    public long mileage;

    /* new attributes for money */
    public List<String> tollUnitList = null;
    public int tollUnitLength;

    public Node() {
    }

    public Node(String index) {
        this.index = index;
    }

    public Node(String index, NodeType type) {
        this.index = index;
        this.type = type;
    }

    //copy a node element except for NodeSource.
    public Node(String index, NodeType type, Node mutualNode) {
        this.index = index;
        this.type = type;
        this.mutualNode = mutualNode;
    }

    //operations
    public Node getMutualNode() {
        return mutualNode;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Node)) return false;
        Node node = (Node) object;
        return this.index != null && node.index != null && this.index.equals(node.index);
    }

    @Override
    public Object clone() {
        Node stu = null;
        try {
            stu = (Node) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return stu;
    }

}

