package nju.ics.Entity;

import java.util.ArrayList;
import java.util.List;

import static nju.ics.Entity.NodeSource.IDENTIFY;
import static nju.ics.Entity.Path.fixedLengthString;

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

    public RuntimePath() {}

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

    public long getLengthWithEnd() {
        return getLength() + runtimeNodeList.get(runtimeNodeList.size() - 1).node.mileage;
    }

    public void print(String desc) {
        System.out.println("---"+ desc +" begin---");
        System.out.println("---length = "+ runtimeNodeList.size() +" ---");
        for (RuntimeNode runtimeNode : runtimeNodeList) {
            System.out.println(fixedLengthString(runtimeNode.node.index, 20) + " " +
                    fixedLengthString(runtimeNode.node.source.toString(), 20)+ " " +
                    fixedLengthString(runtimeNode.transTime, 20) + " " +
                    fixedLengthString(String.valueOf(runtimeNode.node.mileage), 20) + " " +
                    fixedLengthString(runtimeNode.node.name, 20));
        }
        System.out.println("---path end---");
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
