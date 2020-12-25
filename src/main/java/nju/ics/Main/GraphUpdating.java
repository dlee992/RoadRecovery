package nju.ics.Main;

import nju.ics.Entity.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GraphUpdating {

    static Graph graph = null;
    static UpdatedBasicData updatedBasicData = null;

    static ZipFile zipFile = null;
    static BufferedReader reader = null;


    public static boolean updateGraph(Graph graph, UpdatedBasicData updatedBasicData) throws IOException {
        GraphUpdating.graph = graph;
        GraphUpdating.updatedBasicData = updatedBasicData;

        if (updatedBasicData.paramType == 1)
            return updateNodeAndEdge();
        else if (updatedBasicData.paramType == 2)
            return updateMutualNode();
        else if (updatedBasicData.paramType == 3)
            return updateMoneyMap();

        //should not go here
        return false;
    }

    private static boolean updateNodeAndEdge() throws IOException {
        graph.nodes = new ArrayList<>();
        graph.edges = new HashSet<>();

        reader = getBufferReader();

        int rowIndex = 0;
        while (true) {
            String line = getLineFromReader();
            if (line == null) break;
            if (rowIndex++ < 2) continue;
            String[] elements = line.split(",");
            Node startNode = extractNodeAndAddIntoGraph(elements[0], elements[1]);
            Node endNode = extractNodeAndAddIntoGraph(elements[2], elements[3]);
            Edge edge = new Edge(startNode, endNode);
            if (graph.edges.contains(edge)) {
                System.err.println("this edge has already added into graph");
                StackTraceElement[] stackList = Thread.currentThread().getStackTrace();
                for (StackTraceElement stackTrace:
                        stackList) {
                    System.err.println(stackTrace);
                }
                return false;
            }
            else {
                graph.edges.add(edge);
                if (PathRestoration.debugging)
                    System.out.printf("edgeIndex=%d, startNode=%s, endNode=%s\n",
                            graph.edges.size(), startNode.index, endNode.index);
            }
        }

        graph.edgeFlag = true;
        releaseResources();

        if (PathRestoration.debugging)
            System.out.printf("graph node size = %d\n", graph.nodes.size());

        return true;
    }

    private static void releaseResources() {
        try {
            zipFile.close();
            zipFile = null;
            reader.close();
            reader = null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Node extractNodeAndAddIntoGraph(String nodeIndex, String type) {
        NodeType nodeType = null;
        switch (type) {
            case "0":
                nodeType = NodeType.NORMALPORTAL;
                break;
            case "1":
                nodeType = NodeType.PROVINCIALPORTAL;
                break;
            case "3":
                nodeType = NodeType.TOLLSTATION;
                break;
        }
        Node newNode = new Node(nodeIndex, nodeType);
        if (!graph.nodes.contains(newNode))
            graph.nodes.add(newNode);
        return newNode;
    }

    private static BufferedReader getBufferReader() throws IOException {
        String fullName = updatedBasicData.filePath + File.separator + updatedBasicData.file;

        zipFile = new ZipFile(fullName);

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        if (!entries.hasMoreElements()) {
            System.err.println("not found anything in .zip");
            throw new IOException("not found anything in .zip");
        }

        ZipEntry entry = entries.nextElement();
        return new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
    }

    private static boolean updateMutualNode() throws IOException {
        for (Node node:
             graph.nodes) {
            node.mutualNode = null;
            node.tollUnitList = null;
            node.tollUnitLength = 0;
            node.mileage = 0;
        }

        reader = getBufferReader();

        int rowIndex = 0;
        while (true) {
            Node realMutualNode1 = null, realMutualNode2 = null;

            String line = getLineFromReader();
            if (line == null) break;
            if (rowIndex++ < 2) continue;
            String[] elements = line.split(",");
            Node mutualNode1 = new Node(elements[0]);
            Node mutualNode2 = new Node(elements[2]);

            //update for each node, then set each other to mutual node.
            boolean missingOne = false;
            if (!graph.nodes.contains(mutualNode1)) {
                missingOne = true;
                if (PathRestoration.debugging)
                    System.err.printf("{WARNING}[exec updateMutualNode]: %s not found in graph.\n", mutualNode1.index);
            } else {
                realMutualNode1 = graph.nodes.get(graph.nodes.indexOf(mutualNode1));
                updateOneNode(realMutualNode1, elements, rowIndex);
            }

            if (!graph.nodes.contains(mutualNode2)) {
                missingOne = true;
                if (PathRestoration.debugging)
                    System.err.printf("{WARNING}[exec updateMutualNode]: %s not found in graph.\n", mutualNode2.index);
            } else {
                realMutualNode2 = graph.nodes.get(graph.nodes.indexOf(mutualNode2));
                updateOneNode(realMutualNode2, elements, rowIndex);
            }

            if (missingOne) continue;

            if (realMutualNode1.mutualNode != null || realMutualNode2.mutualNode != null) {
                if (PathRestoration.debugging)
                    System.err.print("one node has already have mutual node\n");
                continue;
            }

            realMutualNode1.mutualNode = realMutualNode2;
            realMutualNode2.mutualNode = realMutualNode1;
        }

        graph.mutualFlag = true;
        releaseResources();
        return true;
    }

    private static void updateOneNode(Node realMutualNode, String[] elements, int rowIndex) {
        String[] tollUnits = elements[3].split("\\|");
        realMutualNode.tollUnitList = new ArrayList<>();
        realMutualNode.tollUnitList.addAll(Arrays.asList(tollUnits));
        realMutualNode.tollUnitLength = Integer.parseInt(elements[4]);
        if (elements.length < 6) {
            if (PathRestoration.debugging)
                System.err.printf("{WARNING}[exec updateMutualNode] rowIndex = %d in 402 hasn't mileage info.\n", rowIndex);
            return;
        }
        realMutualNode.mileage = Long.parseLong(elements[5]);
    }

    private static String getLineFromReader() {
        String line = null;
        try {
            assert reader != null;
            if (!reader.ready()) return null;
            line = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert line != null;
        return line;
    }

    private static boolean updateMoneyMap() throws IOException {
        graph.moneyMap.clear();

        reader = getBufferReader();

        int rowIndex = 0;
        while (true) {
            String line = getLineFromReader();
            if (line == null) break;
            if (rowIndex++ < 2) continue;
            String[] elements = line.split(",");
            String tollUnitIndex = elements[2];
            String vehicleType = elements[3];
            Long fee = Long.parseLong(elements[4]);
            String combinedKey = tollUnitIndex + vehicleType;
            graph.moneyMap.put(combinedKey, fee);
            if (PathRestoration.debugging) {
//                System.out.printf("unit=%s, vehicleType=%s, fee=%d\n", tollUnitIndex, vehicleType, fee);
            }
        }

        graph.moneyFlag = true;
        releaseResources();
        return true;
    }

    public static boolean consistentChecking(Graph graph) {
        //todo
        return true;
    }
}
