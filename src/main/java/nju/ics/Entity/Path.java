package nju.ics.Entity;

import static nju.ics.Entity.NodeSource.IDENTIFY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Path {

    public List<Node> nodeList = new ArrayList<Node>();

    public long getLength() {
        long length = 0;
        for (int i = 1; i < nodeList.size() - 1; ++i) {
            length += nodeList.get(i).mileage;
        }
        return length;
    }

    public long getCost(Map<String, Long> moneyMap, int vehicleType) {
        long cost = 0;
        for (Node node : nodeList) {
            cost += node.getNodeTotalFee(moneyMap, vehicleType);
        }
        return cost;
    }

}
