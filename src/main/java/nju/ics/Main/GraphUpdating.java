package nju.ics.Main;

import nju.ics.Entity.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GraphUpdating {

    static Graph graph = null;
    static UpdatedBasicData updatedBasicData = null;

    static ZipFile zipFile = null;
    static BufferedReader reader = null;


    public static boolean updateGraph(Graph graph, UpdatedBasicData updatedBasicData) {
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

    private static boolean updateNodeAndEdge() {
        graph.nodes = new ArrayList<>();
        graph.edges = new HashSet<>();

        try {
            reader = getBufferReader();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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

        try {
            zipFile = new ZipFile(fullName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert zipFile != null;
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        if (!entries.hasMoreElements()) {
            System.err.println("not found anything in .zip");
            StackTraceElement[] stackList = Thread.currentThread().getStackTrace();
            for (StackTraceElement stackTrace:
                    stackList) {
                System.err.println(stackTrace);
            }
        }

        ZipEntry entry = entries.nextElement();
        return new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
    }

    private static boolean updateMutualNode() {
        for (Node node:
             graph.nodes) {
            node.mutualNode = null;
            node.tollUnitList = null;
            node.tollUnitLength = 0;
            node.mileage = 0;
        }

        try {
            reader = getBufferReader();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        int rowIndex = 0;
        while (true) {
            String line = getLineFromReader();
            if (line == null) break;
            if (rowIndex++ < 2) continue;
            String[] elements = line.split(",");
            Node mutualNode1 = new Node(elements[0]);
            Node mutualNode2 = new Node(elements[2]);
            if (!graph.nodes.contains(mutualNode1) || !graph.nodes.contains(mutualNode2)) {
                System.err.println("mutual node not found in graph.");
                return false;
            }

            Node realMutualNode1 = graph.nodes.get(graph.nodes.indexOf(mutualNode1));
            Node realMutualNode2 = graph.nodes.get(graph.nodes.indexOf(mutualNode2));

            if (realMutualNode1.mutualNode != null || realMutualNode2.mutualNode != null) {
//                System.err.printf("one node has already have mutual node\n");
//                System.err.flush();
                continue;
            }

            realMutualNode1.mutualNode = realMutualNode2;
            realMutualNode2.mutualNode = realMutualNode1;

            if (PathRestoration.debugging) {
                System.out.printf("node1=%s, node2=%s\n", realMutualNode1.index, realMutualNode2.index);
//                System.out.flush();
            }

            String[] tollUnits = elements[3].split("\\|");
            realMutualNode1.tollUnitList = new ArrayList<>();
            realMutualNode2.tollUnitList = new ArrayList<>();
            for (String tollUnitIndex:
                 tollUnits) {
                realMutualNode1.tollUnitList.add(tollUnitIndex);
                realMutualNode2.tollUnitList.add(tollUnitIndex);
            }
            realMutualNode1.tollUnitLength = realMutualNode2.tollUnitLength = Integer.parseInt(elements[4]);
            realMutualNode1.mileage = realMutualNode2.mileage = Long.parseLong(elements[5]);
        }

        graph.mutualFlag = true;
        releaseResources();
        return true;
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

    private static boolean updateMoneyMap() {
        graph.moneyMap.clear();

        try {
            reader = getBufferReader();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

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
                System.out.printf("unit=%s, vehicleType=%s, fee=%d\n", tollUnitIndex, vehicleType, fee);
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
