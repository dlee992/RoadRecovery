import nju.ics.Main.GraphUpdating;
import nju.ics.Main.RateLoading;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;


public class RateLoadingTest {

    String[] file1 = new String[]{"401_21010701_20210107000000.txt.zip"};
    String[] file2 = new String[]{"402_20112602_20210111000000.txt.zip"};
    String[] file3 = new String[]{"403_20112701_20201206080000.txt.zip"};
    String[] file4 = new String[]{"404_21010701_20210111000000.txt.zip"};

    String filePath = "src/test/resources/inputs";
    public int base;

    @Test
    public void testRateLoading() throws IOException {
        JSONObject jsonObject;
        RateLoading rateLoading = new RateLoading();
        String ret;

        jsonObject = new JSONObject();
        jsonObject.put("file",      file1[base]);
        jsonObject.put("filePath",  filePath);
        jsonObject.put("paramType", 1);
        ret = rateLoading.rateLoadingMethod(jsonObject.toString());
        JSONObject jsonObject1 = new JSONObject(ret);
        if (jsonObject1.getInt("code") == 2) {
            System.err.println("OhMyGod_1"+ GraphUpdating.errMsg);
            throw new IOException();
        }

        jsonObject = new JSONObject();
        jsonObject.put("file",      file2[base]);
        jsonObject.put("filePath",  filePath);
        jsonObject.put("paramType", 2);
        ret = rateLoading.rateLoadingMethod(jsonObject.toString());
        jsonObject1 = new JSONObject(ret);
        if (jsonObject1.getInt("code") == 2) {
            System.err.println("OhMyGod_2"+ GraphUpdating.errMsg);
            throw new IOException();
        }

        jsonObject = new JSONObject();
        jsonObject.put("file",      file3[base]);
        jsonObject.put("filePath",  filePath);
        jsonObject.put("paramType", 3);
        ret = rateLoading.rateLoadingMethod(jsonObject.toString());
        jsonObject1 = new JSONObject(ret);
        if (jsonObject1.getInt("code") == 2) {
            System.err.println("OhMyGod_3"+ GraphUpdating.errMsg);
            throw new IOException();
        }

        jsonObject = new JSONObject();
        jsonObject.put("file",      file4[base]);
        jsonObject.put("filePath",  filePath);
        jsonObject.put("paramType", 4);
        ret = rateLoading.rateLoadingMethod(jsonObject.toString());
        jsonObject1 = new JSONObject(ret);
        if (jsonObject1.getInt("code") == 2) {
            System.err.println("OhMyGod_4" + GraphUpdating.errMsg);
            throw new IOException();
        }
    }
}
