package nju.ics.Main;

import nju.ics.Entity.UpdatedBasicData;
import org.json.JSONObject;

import java.io.File;
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

        //make sure the zip file exists, right now
        String fullName = updatedBasicData.filePath + File.separator + updatedBasicData.file;
        File f = new File(fullName);

        if(!(f.exists() && !f.isDirectory())) {
            JSONObject retJson = new JSONObject();
            retJson.put("code", 2);
            retJson.put("description", "读取zip文件时，发生数据异常或网络中断，更新失败，请稍后重试");
            retJson.put("version", PathRestoration.concat());
            return retJson.toString();
        }

        dataArray[updatedBasicData.paramType -1] = updatedBasicData;

        boolean enter = false;
        //write to Graph
        if (dataArray[0] != null && dataArray[1] != null && dataArray[2] != null && dataArray[3] != null) {
            writeLock.lock(); // lock
            enter = true;

            for (UpdatedBasicData data :
                    dataArray) {
                try {

                    if (!GraphUpdating.updateGraph(graph, data)) {
                        graph_consistent = false;
                        JSONObject retJson = new JSONObject();
                        retJson.put("code", 2);
                        retJson.put("description", GraphUpdating.errMsg);
                        retJson.put("version", PathRestoration.concat());
                        writeLock.unlock(); // unlock when exception
                        System.err.println("OhMyGod: " + GraphUpdating.errMsg);
                        return retJson.toString();
                    }
                } catch (IOException e) {
                    System.err.println("读取zip文件时，发生数据异常或网络中断，更新失败，请稍后重试");
                    JSONObject retJson = new JSONObject();
                    retJson.put("code", 2);
                    retJson.put("description", "读取zip文件时，发生数据异常或网络中断，更新失败，请稍后重试");
                    retJson.put("version", PathRestoration.concat());
                    writeLock.unlock(); // unlock when exception

                    return retJson.toString();
                }
            }

            graph_consistent = true;
            for (int i = 0; i < 4; i++) {
                versions[i] = dataArray[i].file.substring(4,12);
            }

            graph.buildAllShortestPathByDijkstra();
            System.out.println("基础数据已更新");

            writeLock.unlock(); // unlock when normal
        }

        JSONObject retJson = new JSONObject();
        retJson.put("code", 1);
        if (enter)
            retJson.put("description", "数据更新成功");
        else
            retJson.put("description", "数据路径已知，但并未读取");
        retJson.put("version", PathRestoration.concat());
        return retJson.toString();
    }
}
