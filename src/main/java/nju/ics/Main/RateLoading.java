package nju.ics.Main;

import nju.ics.Entity.UpdatedBasicData;
import org.json.JSONObject;

public class RateLoading {

    public String rateLoadingMethod(String jsonData) {

        JSONObject jsonObj = new JSONObject(jsonData);

        UpdatedBasicData updatedBasicData = new UpdatedBasicData();
        updatedBasicData.file      = jsonObj.getString("file");
        updatedBasicData.filePath  = jsonObj.getString("filePath");
        updatedBasicData.paramType = jsonObj.getInt("paramType");
        updatedBasicData.updatedTime = Long.valueOf(updatedBasicData.file.substring(13, 13+14));

        PathRestoration.priorityQueue.add(updatedBasicData);

        JSONObject retJson = new JSONObject();
        retJson.put("code", 1);
        retJson.put("description", "已记录更新时间和文件位置");
        return retJson.toString();

        //TODO: when to return failed?
    }
}
