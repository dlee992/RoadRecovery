package nju.ics.Entity;

import static nju.ics.Entity.NodeSource.*;
import static nju.ics.Entity.NodeType.TOLLSTATION;

import java.util.*;

public class Graph {
    //for update flag
    public boolean edgeFlag = false;
    public boolean mutualFlag = false;
    public boolean moneyFlag = false;

    //data structure for Li Da
    public List<Node> nodes = new ArrayList<>();
    public Set<Edge> edges = new HashSet<>();
    public Map<String, Long> moneyMap = new HashMap<>();

    //data structure for Penny
    private long INF = Long.MAX_VALUE / 2;
    private long[][] dist;
    private int[][] pre_node;
    private ArrayList<ArrayList<Integer>> dijkstraEdges = new ArrayList<ArrayList<Integer>>(); // for Dijkstra

    private static class NodeDijkstra {

        private long dis;
        private int index, pre_node;

        NodeDijkstra(int index, long dis, int pre_node) {
            this.index = index;
            this.dis = dis;
            this.pre_node = pre_node;
        }
    }

    public Path getMinCostPath(Node inNode, Node outNode, int vehicleType) {
        int from = nodes.indexOf(inNode);
        int to = nodes.indexOf(outNode);

        long[] cost = new long[nodes.size()];
        int[] cost_pre_node = new int[nodes.size()];
        PriorityQueue<NodeDijkstra> q = new PriorityQueue<>(Comparator.comparingLong(x -> x.dis));
        Arrays.fill(cost, INF);
        Arrays.fill(cost_pre_node, -1);
        q.add(new NodeDijkstra(from, 0, -1));

        for (int flag = 1; !q.isEmpty(); flag = 0) {
            NodeDijkstra x = q.poll();
            if (cost[x.index] >= x.dis) {
                if (!(flag == 1 && nodes.get(from).type == TOLLSTATION)) {
                    cost[x.index] = x.dis;
                }
                cost_pre_node[x.index] = x.pre_node;
                if (x.index != from && nodes.get(x.index).type == TOLLSTATION) { // 收费站不能再往下转移
                    continue;
                }
                if (x.index == to) {
                    break;
                }
                for (int y : dijkstraEdges.get(x.index)) {
                    long disy = x.dis + nodes.get(y).getNodeTotalFee(moneyMap, vehicleType);
                    if (cost[y] > disy) {
                        q.add(new NodeDijkstra(y, cost[y] = disy, x.index));
                    }
                }
            }
        }
        return getPathAlongPreNode(cost_pre_node, cost, from, to);
    }

    //If inNode equals to outNode, then return only one node
    //If can not find path, then return null
    private Path getPathAlongPreNode(int[] pre, long[] dis, int from, int to) {
        if (dis[to] == INF) {
            return null;
        }
        Path path = new Path();
        for (int x = to, flag = from == to ? 0 : 1; x != -1; x = pre[x], flag = 1) {
            Node node = (Node) (nodes.get(x)).clone();
            node.source = (x == to || x == from) ? IDENTIFY : ADD;
            path.nodeList.add(node);
            if (flag == 1 && x == from) {
                break;
            }
        }
        Collections.reverse(path.nodeList);
//        assert (path.getLength() == dist[from][to]);
        return path;
    }

    public Path getShortestPath(Node inNode, Node outNode) {
        int from = nodes.indexOf(inNode);
        int to = nodes.indexOf(outNode);
        return getPathAlongPreNode(pre_node[from], dist[from], from, to);
    }

    public void buildAllShortestPathByDijkstra() {
        for (int i = 0; i < nodes.size(); ++i) {
//            System.out.println(nodes.get(i).index + nodes.get(i).name);
            dijkstraEdges.add(new ArrayList<Integer>());
        }
        for (Edge edge : edges) {
//            if (edge.inNode.getMutualNode() != null && edge.inNode.getMutualNode() // 打印能调头的门架
//                .equals(edge.outNode)) {
//                System.out.println("+++" + edge.inNode.index + edge.inNode.name);
//            }
            dijkstraEdges.get(nodes.indexOf(edge.inNode)).add(nodes.indexOf(edge.outNode));
        }

        dist = new long[nodes.size()][nodes.size()];
        pre_node = new int[nodes.size()][nodes.size()];
        PriorityQueue<NodeDijkstra> q = new PriorityQueue<>(Comparator.comparingLong(x -> x.dis));
        for (int from = 0; from < nodes.size(); ++from) {
            Arrays.fill(dist[from], INF);
            Arrays.fill(pre_node[from], -1);
            q.clear();
            q.add(new NodeDijkstra(from, 0, -1));

            for (int flag = 1; !q.isEmpty(); flag = 0) {
                NodeDijkstra x = q.poll();
                if (dist[from][x.index] >= x.dis) {
                    if (!(flag == 1 && nodes.get(from).type == TOLLSTATION)) {
                        dist[from][x.index] = x.dis;
                    }
                    pre_node[from][x.index] = x.pre_node;
                    if (x.index != from && nodes.get(x.index).type == TOLLSTATION) { // 收费站不能再往下转移
                        continue;
                    }
                    for (int y : dijkstraEdges.get(x.index)) {
                        long disy = x.dis + nodes.get(y).mileage;
                        if (dist[from][y] > disy) {
                            q.add(new NodeDijkstra(y, dist[from][y] = disy, x.index));
                        }
                    }
                }
            }
        }
    }
}
