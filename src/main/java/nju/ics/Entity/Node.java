package nju.ics.Entity;


import java.util.List;
import java.util.Map;

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

    //For Penny to call
    public Long getNodeTotalFee(Map<String, Long> moneyMap, int vehicleType)
            throws IllegalArgumentException {
        if (type == NodeType.TOLLSTATION) return 0L;

        long feeSum = 0;
//        System.err.printf("node index=%s\n", index);
        if (tollUnitList == null) {
            throw new IllegalArgumentException("this gantry doesn't contain any toll unit.");
        }
        for (String tollUnitIndex:
             tollUnitList) {
            String mapKey = tollUnitIndex + vehicleType;
            Long fee = moneyMap.get(mapKey);
            if (fee == null) {
                System.err.printf("gantryIndex = %s, tollUnitIndex = %s, vehicleType = %d\n",
                        index, tollUnitIndex, vehicleType);
                throw new IllegalArgumentException(
                        "this basic dataset (403) doesn't contain this very toll unit.");
            }
            feeSum += fee;
        }
        return feeSum;
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

