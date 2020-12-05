package nju.ics.Algorithm;

import static java.lang.Math.pow;
import static nju.ics.Entity.NodeSource.*;

import java.util.List;
import nju.ics.Entity.*;

public class DPAlgorithm implements Algorithm {

    /**
     * Road recovery DP algorithm
     * @param graph graph(G(V, E), T(V))
     * @param originalPath input path P
     *
     * @return output path(P*), return null when failed
     */
    public RuntimePath execute(Graph graph, RuntimePath originalPath, List<Double> configs, int vehicleType) {

        double modifyCost = configs.get(0); //0.01
        double addCost = configs.get(1); //0.1
        double deleteCost = configs.get(2); //4000
        double deleteCost2 = configs.get(3); //2
        double deleteEndCost = configs.get(4); //1000000
        // 保证5个加<1个删 (id9710)

        boolean debug = false;

        int originalPathSize = originalPath.runtimeNodeList.size();
        double[][] dp = new double[originalPathSize][2];
        RuntimePath[][] dpPath = new RuntimePath[originalPathSize][2];
        double[][] distanceFromDeletedNodesToIJ = new double[originalPathSize][originalPathSize];

        double answer = -1;
        RuntimePath answerPath = new RuntimePath();

        boolean not_delete_first = originalPath.runtimeNodeList.get(0).node.type == NodeType.TOLLSTATION;
        boolean not_delete_last =
            originalPath.runtimeNodeList.get(originalPathSize - 1).node.type == NodeType.TOLLSTATION;

//        ArrayList<Node> reasonableNodeList = new ArrayList<Node>();
//
//        for (int i = 0; i < originalPathSize - 1; ++i) {
//            Path path1 = graph.getShortestPath(originalPath.runtimeNodeList.get(i).node, originalPath.runtimeNodeList.get(i + 1).node);
//            if (path1 != null) reasonableNodeList.addAll(path1.nodeList);
//        }

        for (int i = 0; i < originalPathSize; ++i) {
            for (int j = i; j < originalPathSize; ++j) {
                distanceFromDeletedNodesToIJ[i][j] =
                    j - i <= 1 ? 0 : distanceFromNodesToNodes(graph, originalPath.runtimeNodeList, i, j);
                if (debug) {
//                    System.out.println("distance delete " + i + " " + j + ": "
//                        + distanceFromDeletedNodesToIJ[i][j]);
                }
            }
        }

        for (int i = 0; i < originalPathSize; ++i) {
            for (int flagI = 0; flagI <= 1; ++flagI) {
                // initial dp array
                dp[i][flagI] = (i == 0) ? (modifyCost * flagI) : -1;
                dpPath[i][flagI] = new RuntimePath();
                // nodeI: v_i or T(v_i) controlled by nodeI
                RuntimeNode nodeI = flagI == 0 ? originalPath.runtimeNodeList.get(i)
                    : new RuntimeNode(originalPath.runtimeNodeList.get(i).node.getMutualNode(), null);
                if (nodeI.node == null) {
                    continue;
                }
                dpPath[i][flagI].runtimeNodeList.add(new RuntimeNode(nodeI.node, nodeI.transTime));
                dpPath[i][flagI].runtimeNodeList.get(0).node.source = IDENTIFY;
                for (int flagJ = 0; flagJ <= 1; ++flagJ) {
                    for (int j = i - 1; j >= 0; --j) {
                        // nodeJ: v_j or T(v_j) controlled by flagJ
                        RuntimeNode nodeJ = flagJ == 0 ? originalPath.runtimeNodeList.get(j)
                            : new RuntimeNode(originalPath.runtimeNodeList.get(j).node.getMutualNode(), null);
                        if (nodeJ.node == null) {
                            continue;
                        }
                        // shortest path from nodeJ to nodeI
                        Path shortestPath = graph.getShortestPath(nodeJ.node, nodeI.node);
                        if (shortestPath == null) {
                            continue;
                        }
                        RuntimePath runtimeShortestPath = new RuntimePath(shortestPath, nodeJ, nodeI);
                        if (!nodeI.node.equals(nodeJ.node) || (flagI == 1 && flagJ == 1)) { // When i == j and has one IDENTIFY, then IDENTIFY
                            if (flagJ == 1) { // 反转结点
                                runtimeShortestPath.runtimeNodeList.get(0).node.source = MODIFY;
                            }
                            if (flagI == 1) { // 反转结点
                                runtimeShortestPath.runtimeNodeList
                                    .get(runtimeShortestPath.runtimeNodeList.size() - 1).node.source = MODIFY;
                            }
                        }
                        // FIXME: find suitable cost
                        double distance = runtimeShortestPath.getLength();

                        // update method 1: 从 (j, flagJ) 转移到 (i, flagI)，删除 j+1 到 i-1 之间的门架，补上 j 到 i 的最短路
                        if (dp[j][flagJ] != -1) {
                            double result = dp[j][flagJ]
                                + modifyCost * flagI
                                + deleteCost * pow(i - j - 1, 1)
                                + deleteCost2 * distanceFromDeletedNodesToIJ[j][i]
                                + addCost * pow(distance, 1);
//                                + addCost2 * unreasonableNodes(reasonableNodeList, shortestPath);
                            // update
                            if (dp[i][flagI] == -1 || result <= dp[i][flagI]) {
                                dp[i][flagI] = result;
                                dpPath[i][flagI].runtimeNodeList.clear();
                                dpPath[i][flagI].add(dpPath[j][flagJ]);
//                            if (flagJ == 1 && nodeI.equals(nodeJ)) {
//                                dpPath[i][flagI].nodeList
//                                    .remove(dpPath[i][flagI].nodeList.size() - 1);
//                            }
                                dpPath[i][flagI].add(runtimeShortestPath);
                                if (debug) {
                                    dpPath[i][flagI].print(
                                        "dp[" + i + "][" + flagI + "]: " + dp[i][flagI]
                                            + " (from dp[" + j + "][" + flagJ + "])");
                                }
                            }
                        }
                        // update method 2: delete node 0 to j-1, j+1 to i-1
                        if (!not_delete_first) {
                            double result = modifyCost * (flagJ + flagI)
                                + deleteEndCost * j
                                + deleteCost * pow(i - j - 1, 1)
                                + deleteCost2 * distanceFromDeletedNodesToIJ[j][i]
                                + addCost * pow(distance, 1);
                            if (dp[i][flagI] == -1 || result <= dp[i][flagI]) {
                                dp[i][flagI] = result;
                                dpPath[i][flagI].runtimeNodeList.clear();
                                dpPath[i][flagI].add(runtimeShortestPath);
                                if (debug) {
                                    dpPath[i][flagI].print(
                                        "dp[" + i + "][" + flagI + "]: " + dp[i][flagI]
                                            + " (from node[" + j + "][" + flagJ + "])");
                                }
                            }
                        }
                    }
                }
                if ((!not_delete_last || i == originalPathSize - 1) && (answer == -1
                    || dp[i][flagI] + (originalPathSize - 1 - i) * deleteEndCost < answer) && (
                    dp[i][flagI] != -1)) {
                    answer = dp[i][flagI] + (originalPathSize - 1 - i) * deleteEndCost; // 相当于后面都删掉
                    answerPath = dpPath[i][flagI];
                }
            }
        }

        //lida: for temporally usage, no need of new parametrized strategies

        // for debugging
//        answerPath.print("middle phase");

        double threshold_value = 0.2;
        // TODO: value of threshold?

        List<RuntimeNode> answerNodes = answerPath.runtimeNodeList;
        for (int length = answerNodes.size(); length > 2; --length) {
            for (int start = 0; start + length - 1 < answerNodes.size(); ++start) {
                int end = start + length - 1;
                // if delta(P_{start:end}, min-cost-path(P_{start}, P_{end})) <= d, then replace P[start:end] with min cost path
//                if (answerNodes.get(start).node.source != IDENTIFY || answerNodes.get(end).node.source != IDENTIFY) {
//                    continue;
//                }
                Path minCostPath = graph
                    .getMinCostPath(answerNodes.get(start).node, answerNodes.get(end).node, vehicleType);
                // add + identify + modify == answerNodes.subList(start + 1, end).size() == length - 2
                long addNodeNum = answerNodes.subList(start + 1, end).stream().filter(x -> x.node.source == ADD)
                    .count();
                long identifyNodeNum = answerNodes.subList(start + 1, end).stream()
                    .filter(x -> x.node.source == IDENTIFY).count();
                long modifyNodeNum = answerNodes.subList(start + 1, end).stream().filter(x -> x.node.source == MODIFY)
                    .count();
                int lcs = longestCommonSubsequence(answerNodes.subList(start, end + 1), minCostPath.nodeList);
                long original_cost = new RuntimePath(answerNodes.subList(start, end + 1))
                    .getCost(graph.moneyMap, vehicleType);
                long min_cost = minCostPath.getCost(graph.moneyMap, vehicleType);
                double reliability = (double) (lcs - 2) / (length - 2) * 0.5
                    + (double) (identifyNodeNum + modifyNodeNum) / (length - 2) * 0.2
                    + (double) min_cost / original_cost * 0.3;
                // TODO: how to calculate reliability?
//                if (reliability < threshold_value) {
//                    System.out.println(
//                        "reliability(" + start + " " + answerNodes.get(start).node.index + ", " + end + " "
//                            + answerNodes.get(end).node.index + ") = " + reliability);
//                    System.out.println(
//                        "original cost: " + original_cost + ", new cost: " + min_cost + ", length: " + length
//                            + ", lcs: " + lcs + ", identify: " + identifyNodeNum + ", modify: " + modifyNodeNum
//                            + ", add: " + addNodeNum);
//                }
                if (reliability < threshold_value) {
//                    System.out.println("Fix by min cost");
                    RuntimePath newPath = new RuntimePath();
                    newPath.add(new RuntimePath(answerNodes.subList(0, start)));
                    newPath.add(new RuntimePath(minCostPath, answerNodes.get(start), answerNodes.get(end)));
                    newPath.add(new RuntimePath(answerNodes.subList(end + 1, answerNodes.size())));
//                    assert(newPath.getCost(graph.moneyMap, vehicleType) <= answerPath.getCost(graph.moneyMap, vehicleType));
//                    newPath.print("fix");
                    return newPath;
                    // TODO: not break, continue and do it again?
                }
            }
        }
        return answerPath; // empty when failed
    }

    private int longestCommonSubsequence(List<RuntimeNode> a, List<Node> b) {
        int[][] dp = new int[a.size() + 1][b.size() + 1];
        for (int i = 1; i <= a.size(); ++i) {
            for (int j = 1; j <= b.size(); ++j) {
                dp[i][j] = Math.max(dp[i - 1][j - 1] + (a.get(i - 1).node.equals(b.get(j - 1)) ? 1 : 0),
                    Math.max(dp[i - 1][j], dp[i][j - 1]));
            }
        }
        return dp[a.size()][b.size()];
    }

    /***
     * NOTE:
     * any shortest path computed in this method is not accessible from outside,
     * therefore no need to wrap with runtime.
     ***/
    private double distanceFromNodesToNodes(Graph graph, List<RuntimeNode> nodeList, int i, int j) {
        long ret = 0;
        RuntimeNode nodeI = nodeList.get(i);
        RuntimeNode nodeJ = nodeList.get(j);
        for (int k = i + 1; k < j; ++k) {
            long dis = -1;
            RuntimeNode nodeK = nodeList.get(k);
            Path path = graph.getShortestPath(nodeI.node, nodeK.node);
            if (path != null) dis = path.getLength();
            path = graph.getShortestPath(nodeK.node, nodeJ.node);
            if (path != null && (dis == -1 || path.getLength() < dis)) dis = path.getLength();
            if (nodeK.node.getMutualNode() != null) {
                nodeK = new RuntimeNode(nodeK.node.getMutualNode(), null);
                path = graph.getShortestPath(nodeI.node, nodeK.node);
                if (path != null && (dis == -1 || path.getLength() < dis)) dis = path.getLength();
                path = graph.getShortestPath(nodeK.node, nodeJ.node);
                if (path != null && (dis == -1 || path.getLength() < dis)) dis = path.getLength();
            }
            ret += dis;
        }
        return ((double) ret) / (j - i - 1);
//        int ret = -1;
//        for (Node node1 : list1) {
//            for (Node node2 : list2) {
//                Path path = graph.getShortestPath(node1, node2);
//                if (path != null && (ret == -1 || path.getLength() < ret)) {
//                    ret = path.getLength();
//                }
//                path = graph.getShortestPath(node2, node1);
//                if (path != null && (ret == -1 || path.getLength() < ret)) {
//                    ret = path.getLength();
//                }
//            }
//        }
//        return max(ret - 1, 0);
    }

    private int unreasonableNodes(List<Node> reasonableNodeList, Path path) {
        int ret = 0;
        for (Node node: path.nodeList) {
            if (!reasonableNodeList.contains(node)) ret++;
        }
        return ret;
    }
}
