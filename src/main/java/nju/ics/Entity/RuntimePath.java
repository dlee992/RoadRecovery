package nju.ics.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nju.ics.Entity.NodeSource.IDENTIFY;

public class RuntimePath {
    public List<RuntimeNode> runtimeNodeList = new ArrayList<>();

    public RuntimePath(Path path, RuntimeNode startRuntimeNode, RuntimeNode endRuntimeNode) {
        //build a path with start and end time information
        for (Node node: path.nodeList
             ) {
            String transTime = null;
            if (node.equals(startRuntimeNode.node)) transTime = startRuntimeNode.transTime;
            if (node.equals(endRuntimeNode.node))   transTime = endRuntimeNode.transTime;

            runtimeNodeList.add(new RuntimeNode(node, transTime));
        }
    }

    public RuntimePath(List<RuntimeNode> runtimeNodeList) {
        this.runtimeNodeList = runtimeNodeList;
    }

    public RuntimePath() {}

    public String fixedLengthString(String string, int length) {
        return String.format("%1$"+length+ "s", string);
    }

    public String getLiteralPath() {
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (RuntimeNode runtimeNode: runtimeNodeList
             ) {
            if (count > 0) stringBuilder.append("|");
            count++;
            stringBuilder.append(runtimeNode.node.index);
        }
        return stringBuilder.toString();
    }

    public long getLength() {
        long length = 0;
        for (int i = 1; i < runtimeNodeList.size() - 1; ++i) {
            length += runtimeNodeList.get(i).node.mileage;
        }
        return length;
    }

    public long getCost(Map<String, Long> moneyMap, int vehicleType) {
        return runtimeNodeList.stream().mapToLong(i -> i.node.getNodeTotalFee(moneyMap, vehicleType)).sum();
    }

    public void print(String desc) {
        int length = 20;
        System.out.println("---"+ desc +" begin: path length = "+ runtimeNodeList.size() +" ---");
        System.out.println(fixedLengthString("node index", length) + " " +
                fixedLengthString("node origin", length)+ " " +
                fixedLengthString("node name", length) + " " +
                fixedLengthString("node mileage", length) + " " +
                fixedLengthString("node timestamp", length));
        for (RuntimeNode runtimeNode : runtimeNodeList) {
            System.out.println(fixedLengthString(runtimeNode.node.index, length) + " " +
                    fixedLengthString(runtimeNode.node.source.toString(), length)+ " " +
                    fixedLengthString(runtimeNode.node.name, length) + " " +
                    fixedLengthString(String.valueOf(runtimeNode.node.mileage), length) + " " +
                    fixedLengthString(runtimeNode.transTime, length));
        }
        System.out.println("---"+ desc +" end---");
    }

    public String[] getStringArray() {
        String[] arrays = new String[runtimeNodeList.size()-2];
        for (int i = 1; i < runtimeNodeList.size()-1; i++) {
            arrays[i-1] = runtimeNodeList.get(i).node.index;
        }
        return arrays;
    }

    public void add(RuntimePath path2) {
        if (!runtimeNodeList.isEmpty() && !path2.runtimeNodeList.isEmpty() && runtimeNodeList.get(runtimeNodeList.size() - 1)
                .equals(path2.runtimeNodeList.get(0))) {
            if (path2.runtimeNodeList.get(0).node.source == IDENTIFY) {
                runtimeNodeList.get(runtimeNodeList.size() - 1).node.source = IDENTIFY;
            }
            runtimeNodeList.addAll(path2.runtimeNodeList.subList(1, path2.runtimeNodeList.size()));
        } else {
            runtimeNodeList.addAll(path2.runtimeNodeList);
        }
    }

}
