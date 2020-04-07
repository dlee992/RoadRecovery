package nju.ics.Entity;


public class Node implements Cloneable {
    /* attributes */
    public String index;
    public String name;
    public NodeType type;
    public NodeSource source; // for testing
    public Node mutualNode = null;
    public long mileage;

    public Node() {
    }

    //copy a node element except for NodeSource.
    public Node(String index, String name, NodeType type, Node mutualNode) {
        this.index = index;
        this.name = name;
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

