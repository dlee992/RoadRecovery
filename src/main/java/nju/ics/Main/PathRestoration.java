package nju.ics.Main;


import nju.ics.Algorithm.Algorithm;
import nju.ics.Algorithm.DPAlgorithm;
import nju.ics.Entity.*;
import org.apache.commons.math3.analysis.function.StepFunction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PathRestoration {

    private boolean debugging = false;

    public static Graph graph = new Graph();
    /**
     * when outside told me to update graph metadata, then set @updatedFlag to true;
     * when Penny updated the graph metadata, then set @updatedFlag to false again.
     */
    static Comparator<UpdatedBasicData> comparator = new UpdatedComparator();
    public static PriorityQueue<UpdatedBasicData> priorityQueue = new PriorityQueue<>(comparator);

    /**
     * input key
     */
    String enStationId, exStationId;
    String enTime, exTime;
    String basicDataPath;

    double addCost, deleteCost, deleteCost2, modifyCost, deleteEndCost;

    int vehicleType;

    StringBuilder description = new StringBuilder("Unknown gantry: ");
    int desCount = 0;

    RuntimePath originalPath = new RuntimePath();
    public RuntimePath recoveredPath = null;

    /**
     * interface method for external call
     *
     * @param inputJson each element of an input path in JSON format
     * @return inputJson.toString()
     */
    public String pathRestorationMethod(String inputJson)  {
        //analyze JSON data
        JSONObject jsonObj = new JSONObject(inputJson);

        enStationId = jsonObj.getString("enStationId");
        exStationId = jsonObj.getString("exStationId");
        enTime      = jsonObj.getString("enTime");
        if (enTime.length() == 0) enTime = null;
        exTime      = jsonObj.getString("exTime");
        if (exTime.length() == 0) exTime = null;

        //new interface params, from JSONArray to List<Map<String, String>>
        JSONArray json_arr = jsonObj.getJSONArray("gantryIdList");
        List<String> gantryList = new ArrayList<>();
        List<String> timeList = new ArrayList<>();

        for (int i = 0; i < json_arr.length(); i++) {
            JSONObject jsonArrayObj = json_arr.getJSONObject(i);
            String gantryHex = jsonArrayObj.getString("gantryHex");
            String transTime = jsonArrayObj.getString("transTime");
            if (transTime.length() == 0) transTime = null;

            gantryList.add(gantryHex);
            timeList.add(transTime);
        }

//        basicDataPath = jsonObj.getString("basicDataPath");

        //configuration parameter for DP
        modifyCost    = jsonObj.getDouble("modifyCost");
        addCost       = jsonObj.getDouble("addCost");
        deleteCost    = jsonObj.getDouble("deleteCost");
        deleteCost2   = jsonObj.getDouble("deleteCost2");
        deleteEndCost = jsonObj.getDouble("deleteEndCost");

        List<Double> configs = new ArrayList<>();
        configs.add(modifyCost);
        configs.add(addCost);
        configs.add(deleteCost);
        configs.add(deleteCost2);
        configs.add(deleteEndCost);

        vehicleType = jsonObj.getInt("vehicleType");

        //TODO: if new updated time validates, then rebuild the graph
        Long currentDate = getCurrentDate();

        //TODO: read all needed metadata from queue
        UpdatedBasicData current = priorityQueue.peek();
        while (current != null && current.updatedTime <= currentDate) {
            current = priorityQueue.poll();
            assert current != null;
            GraphUpdating.updateGraph(graph, current);
            current = priorityQueue.peek();
        }

        //TODO:
        if (!GraphUpdating.consistentChecking(graph)) {
            System.err.println("updated graph is inconsistent.");
        }

        //add the start and end node into original path
        Node startNode = getNode(graph, enStationId, false);
        if (startNode != null) {
            startNode.source = NodeSource.IDENTIFY;
            originalPath.runtimeNodeList.add(new RuntimeNode(startNode, enTime));
//            System.out.println(enTime);
        }
        else {
            System.err.println("no start node");
        }

        //I need a runtime node, {node, timestamp}
        int count = 0;
        if (gantryList.size() > 0) {
            for (String gantry : gantryList) {
//                System.out.println(gantry);
                Node completeNode = getNode(graph, gantry, true);
                if (completeNode != null) {
                    completeNode.source = NodeSource.IDENTIFY;
                    originalPath.runtimeNodeList.add(new RuntimeNode(completeNode, timeList.get(count)));
                }
                count++;
            }
        }

        Node endNode = getNode(graph, exStationId, false);
        if (endNode != null) {
            endNode.source = NodeSource.IDENTIFY;
            originalPath.runtimeNodeList.add(new RuntimeNode(endNode, exTime));
        }
        else {
            System.err.println("no end node");
        }

        //If exist unknown gantry, then return with failure.
        if (desCount > 0)
            return getReturnedJsonObject(
                    null,
                    description.toString()
            ).toString();

        //If only exist one node, then return with failure.
        if (originalPath.runtimeNodeList.size() == 0) {
            return getReturnedJsonObject(
                    null,
                    "No identified node"
            ).toString();
        }

        if (originalPath.runtimeNodeList.size() == 1) {
            return getReturnedJsonObject(
                    null,
                    "Exist only one node "+originalPath.runtimeNodeList.get(0).node.index
            ).toString();
        }

//        originalPath.print("input path");
//        System.out.println("原路径长度=" + originalPath.runtimeNodeList.size());
        Algorithm algorithm = new DPAlgorithm();
        recoveredPath = algorithm.execute(graph, originalPath, configs, vehicleType);

        //generate JSON data for return
        JSONObject returnJsonObj = getReturnedJsonObject(recoveredPath, "Unknown reason");

        return returnJsonObj.toString();
    }

    private Long getCurrentDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        return Long.valueOf(dtf.format(now));
    }

    private JSONObject getReturnedJsonObject(RuntimePath recoveredPath, String description) {
        JSONObject returnJsonObj = new JSONObject();
        if (recoveredPath != null) {
            if (debugging)
                recoveredPath.print("恢复出的路径");

            returnJsonObj.put("code", "1");
            returnJsonObj.put("description", "Success");

            //directly obtain the recoveredPath info.
            StringBuilder gantryHexGroup  = new StringBuilder();
            StringBuilder gantryFlagGroup = new StringBuilder();

            //fix missing time information
            StringBuilder transTimeGroup  = new StringBuilder();
            String lastTime = exTime;

            //reverse order
            for (int i = recoveredPath.runtimeNodeList.size()-1; i >= 0 ; i--) {
                RuntimeNode runtimeNode = recoveredPath.runtimeNodeList.get(i);
                if (runtimeNode.transTime != null) lastTime = runtimeNode.transTime;
                if (runtimeNode.node.index.length() >= 10) continue;
                if (runtimeNode.transTime == null) runtimeNode.transTime = lastTime;
            }

            //delete the entry & exit toll station
            int count = 0;
            for (RuntimeNode runtimeNode: recoveredPath.runtimeNodeList
                 ) {
                if (runtimeNode.node.index.length() >= 10) continue;

                if (count > 0) {
                    gantryHexGroup.append("|");
                    gantryFlagGroup.append("|");
                    transTimeGroup.append("|");
                }
                count++;

                gantryHexGroup.append(runtimeNode.node.index);

                if (runtimeNode.node.source == NodeSource.IDENTIFY) gantryFlagGroup.append("1");
                if (runtimeNode.node.source == NodeSource.MODIFY)   gantryFlagGroup.append("2");
                if (runtimeNode.node.source == NodeSource.ADD)      gantryFlagGroup.append("2");

                transTimeGroup.append(runtimeNode.transTime);
            }

            if (gantryHexGroup.length() == 0) {
                handleFailure(returnJsonObj, "gantryHexGroup is empty");
            }
            else {
                returnJsonObj.put("gantryHexGroup", gantryHexGroup.toString());
                returnJsonObj.put("gantryFlagGroup", gantryFlagGroup.toString());
                returnJsonObj.put("transTimeGroup", transTimeGroup.toString());
            }

        } else {
            handleFailure(returnJsonObj, description);
        }
        return returnJsonObj;
    }

    private void handleFailure(JSONObject returnJsonObj, String description) {
        //A 5-element tuple
        returnJsonObj.put("code",            "2");
        returnJsonObj.put("description",     "Failure cause: "+description);
        returnJsonObj.put("gantryHexGroup",  "");
        returnJsonObj.put("gantryFlagGroup", "");
        returnJsonObj.put("transTimeGroup",  "");
    }

    private Node getNode(Graph graph, String gantry, boolean isGantry) {
        Node node = new Node();
        node.index = gantry;
        if (!graph.nodes.contains(node)) {
            if (isGantry) {
                System.err.println("[Error] Unknown gantry [" + gantry + "] exists.");
                updateDescription(gantry);
            }
            return null;
        }
        return graph.nodes.get(graph.nodes.indexOf(node));
    }

    private void updateDescription(String gantry) {
        if (desCount > 0) description.append("|");
        desCount++;
        description.append(gantry);
    }

    private static class UpdatedComparator implements Comparator<UpdatedBasicData> {
        @Override
        public int compare(UpdatedBasicData o1, UpdatedBasicData o2) {
            return o1.updatedTime.compareTo(o2.updatedTime);
        }
    }
}
