package nju.ics.Main;

import nju.ics.Entity.Graph;

public class GraphUpdating {

    public static void updateGraph(Graph graph, UpdatedBasicData updatedBasicData) {
        if (updatedBasicData.paramType == 1) {
            updateEdge(graph);
        }
        else if (updatedBasicData.paramType == 2) {
            updateMutualNode(graph);
        }
        else if (updatedBasicData.paramType == 3) {
            updateMoneyMap(graph);
        }
    }

    private static void updateEdge(Graph graph) {
        //TODO
        graph.edgeFlag = true;
    }

    private static void updateMutualNode(Graph graph) {
        //TODO
        graph.mutualFlag = true;
    }

    private static void updateMoneyMap(Graph graph) {
        //TODO
        graph.moneyFlag = true;
    }
}
