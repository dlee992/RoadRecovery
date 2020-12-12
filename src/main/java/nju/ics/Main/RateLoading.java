package nju.ics.Main;

import nju.ics.Entity.UpdatedBasicData;
import org.json.JSONObject;

import static nju.ics.Main.PathRestoration.*;

public class RateLoading {

    public String rateLoadingMethod(String jsonData) {

        JSONObject jsonObj = new JSONObject(jsonData);

        UpdatedBasicData updatedBasicData = new UpdatedBasicData();
        updatedBasicData.file      = jsonObj.getString("file");
        updatedBasicData.filePath  = jsonObj.getString("filePath");
        updatedBasicData.paramType = jsonObj.getInt("paramType");
        updatedBasicData.updatedTime = Long.valueOf(updatedBasicData.file.substring(13, 13+14));

        dataArray[updatedBasicData.paramType -1] = updatedBasicData;

        //write to Graph
        if (dataArray[0] != null && dataArray[1] != null && dataArray[2] != null) {
            writeLock.lock();

            for (UpdatedBasicData data :
                    dataArray) {
                GraphUpdating.updateGraph(graph, data);
            }
            graph.buildAllShortestPathByDijkstra();
            System.out.println("基础数据已更新");

            writeLock.unlock();
        }

        JSONObject retJson = new JSONObject();
        retJson.put("code", 1);
        retJson.put("description", "已记录更新时间和文件位置");
        return retJson.toString();
    }
}
