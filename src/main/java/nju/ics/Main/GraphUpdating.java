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

    public static String errMsg = null;
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
            return updateMileage();
        else if (updatedBasicData.paramType == 3)
            return updateMoneyMap();
        else if (updatedBasicData.paramType == 4)
            return updateMutualNode();

        //should not go here
        return false;
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

    //update 402 supported
    private static void updateOneNode(Node realNode, String[] elements) {
        realNode.tollUnitList = new ArrayList<>();
        realNode.tollUnitList.addAll(Arrays.asList(elements[2].split("\\|")));
        realNode.tollUnitLength = Integer.parseInt(elements[3]);
        realNode.mileage = Long.parseLong(elements[4]);
    }

    //update 401
    private static boolean updateNodeAndEdge() throws IOException {
        // Assume 401 is always correct, so never return false.
        reader = getBufferReader();
        int rowIndex = 0;
        while (true) {
            String line = getLineFromReader();
            if (line == null) break;
            if (rowIndex++ < 2) continue;
            String[] elements = line.split(",");
            if (elements.length < 4) {
                errMsg = "one line (" + line + ") in 403, but too short.";
                if (PathRestoration.debugging)
                    System.err.println(errMsg);
                return false;
            }
        }

        graph.nodes = new ArrayList<>();
        graph.edges = new HashSet<>();

        reader = getBufferReader();
        rowIndex = 0;
        while (true) {
            String line = getLineFromReader();
            if (line == null) break;
            if (rowIndex++ < 2) continue;
            String[] elements = line.split(",");
            Node startNode = extractNodeAndAddIntoGraph(elements[0], elements[1]);
            Node endNode = extractNodeAndAddIntoGraph(elements[2], elements[3]);
            Edge edge = new Edge(startNode, endNode);
            if (graph.edges.contains(edge)) {
                errMsg = "this edge (" + line + ") has already added into graph";
                System.err.println(errMsg);
//                return false;
            }
            else {
                graph.edges.add(edge);
                if (PathRestoration.debugging)
                    System.out.printf("edgeIndex=%d, startNode=%s, endNode=%s\n",
                            graph.edges.size(), startNode.index, endNode.index);
            }
        }

        releaseResources();
        if (PathRestoration.debugging)
            System.out.printf("graph node size = %d\n", graph.nodes.size());

        return true;
    }

    //update 402
    private static boolean updateMileage() throws IOException {
        // Assume always correct, never return false.
        reader = getBufferReader();
        int rowIndex = 0;
        while (true) {
            String line = getLineFromReader();
            if (line == null) break;
            if (rowIndex++ < 2) continue;
            String[] elements = line.split(",");

            if (elements.length < 5) {
                if (PathRestoration.debugging)
                    System.err.printf(
                            "{WARNING}[exec updateMutualNode] rowIndex = %d in 402 lack of some info.\n",
                            rowIndex);
                return false;
            }
        }

        //update
        for (Node node:
                graph.nodes) {
            node.tollUnitList = null;
            node.tollUnitLength = 0;
            node.mileage = 0;
        }

        reader = getBufferReader();
        rowIndex = 0;
        while (true) {
            Node realNode;

            String line = getLineFromReader();
            if (line == null) break;
            if (rowIndex++ < 2) continue;
            String[] elements = line.split(",");
            Node fakeNode = new Node(elements[0]);

            //update for each node, then set each other to mutual node.
            if (!graph.nodes.contains(fakeNode)) {
                errMsg = "{WARNING}[exec updateMutualNode]: " + fakeNode.index +  " not found in graph.";
                if (PathRestoration.debugging)
                    System.err.println(errMsg);
                continue;
            } else {
                realNode = graph.nodes.get(graph.nodes.indexOf(fakeNode));
                updateOneNode(realNode, elements);
            }

            if (realNode.mutualNode != null) {
                if (PathRestoration.debugging)
                    System.err.print("one node has already have mutual node\n");
            }
        }

        releaseResources();
        return true;
    }

    //update 403
    private static boolean updateMoneyMap() throws IOException {
        //check
        reader = getBufferReader();
        int rowIndex = 0;
        while (true) {
            String line = getLineFromReader();
            if (line == null) break;
            if (rowIndex++ < 2) continue;
            String[] elements = line.split(",");
            if (elements.length < 5) {
                errMsg = "one line (" + line + ") in 403, but too short.";
                if (PathRestoration.debugging)
                    System.err.println(errMsg);
                return false;
            }
        }

        //update
        graph.moneyMap.clear();
        reader = getBufferReader();
        rowIndex = 0;
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

        releaseResources();
        return true;
    }

    //update 404
    private static boolean updateMutualNode() throws IOException {
        //check
        reader = getBufferReader();
        String line = getLineFromReader();
        for (int index = 0; line != null; index++, line = getLineFromReader()) {
            if (index < 2) continue;
            String[] elements = line.split(",");
            if (elements.length < 4) {
                errMsg = "one line (" + line + ") in 404, but too short.";
                if (PathRestoration.debugging)
                    System.err.println(errMsg);
                return false;
            }
            Node fakeNode_1 = new Node(elements[0]);
            Node fakeNode_2 = new Node(elements[2]);
            if (!(graph.nodes.contains(fakeNode_1) && graph.nodes.contains(fakeNode_2))) {
                errMsg = "at least one node in " + line + " exists in 404, but doesn't exist in 401.";
                if (PathRestoration.debugging)
                    System.err.println(errMsg);
                return false;
            }
        }

        //update
        for (Node node: graph.nodes)
            node.mutualNode = null;

        reader = getBufferReader();
        line = getLineFromReader();
        for (int index = 0; line != null; index++, line = getLineFromReader()) {
            if (index < 2) continue;
            String[] elements = line.split(",");
            Node fakeNode_1 = new Node(elements[0]);
            Node fakeNode_2 = new Node(elements[2]);
            Node realNode_1 = graph.nodes.get(graph.nodes.indexOf(fakeNode_1));
            Node realNode_2 = graph.nodes.get(graph.nodes.indexOf(fakeNode_2));
            realNode_1.mutualNode = realNode_2;
            realNode_2.mutualNode = realNode_1;
        }

        return true;
    }

}
