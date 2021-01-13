package nju.ics.Main;

import nju.ics.Entity.UpdatedBasicData;
import org.json.JSONObject;

import java.io.IOException;

import static nju.ics.Main.PathRestoration.*;

public class RateLoading {

    public String rateLoadingMethod(String jsonData) {

        JSONObject jsonObj = new JSONObject(jsonData);

        UpdatedBasicData updatedBasicData = new UpdatedBasicData();
        updatedBasicData.file      = jsonObj.getString("file");
        updatedBasicData.filePath  = jsonObj.getString("filePath");
        updatedBasicData.paramType = jsonObj.getInt("paramType");
//        updatedBasicData.updatedTime = Long.valueOf(updatedBasicData.file.substring(13, 13+14));

        dataArray[updatedBasicData.paramType -1] = updatedBasicData;

        //write to Graph
        if (dataArray[0] != null && dataArray[1] != null && dataArray[2] != null && dataArray[3] != null) {
            writeLock.lock(); // lock

            for (UpdatedBasicData data :
                    dataArray) {
                try {

                    if (!GraphUpdating.updateGraph(graph, data)) {
                        graph_consistent = false;
                        JSONObject retJson = new JSONObject();
                        retJson.put("code", 2);
                        retJson.put("description", GraphUpdating.errMsg);
                        writeLock.unlock(); // unlock when exception
                        System.err.println("OhMyGod: " + GraphUpdating.errMsg);
                        return retJson.toString();
                    }
                } catch (IOException e) {
                    System.err.println("读取zip文件时，发生数据异常或网络中断，更新失败，请稍后重试");
                    JSONObject retJson = new JSONObject();
                    retJson.put("code", 2);
                    retJson.put("description", "读取zip文件时，发生数据异常或网络中断，更新失败，请稍后重试");

                    writeLock.unlock(); // unlock when exception

                    return retJson.toString();
                }
            }
            graph_consistent = true;
            graph.buildAllShortestPathByDijkstra();
            System.out.println("基础数据已更新");

            writeLock.unlock(); // unlock when normal
        }

        JSONObject retJson = new JSONObject();
        retJson.put("code", 1);
        retJson.put("description", "Updates succeeded.");
        return retJson.toString();
    }
}
