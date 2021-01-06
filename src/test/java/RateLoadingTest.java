import nju.ics.Main.RateLoading;
import org.json.JSONObject;
import org.junit.Test;


public class RateLoadingTest {

    String[] file1 = new String[]{"401_20103001_20201101000000.txt.zip", "401_20120601_20201206080000.txt.zip", "401_21010401_20210104000000.txt.zip"};
    String[] file2 = new String[]{"402_20102101_20201101000000.txt.zip", "402_20112602_20201206080000.txt.zip", "402_21010401_20210104000000.txt.zip"};
    String[] file3 = new String[]{"403_20102201_20201101000000.txt.zip", "403_20112701_20201206080000.txt.zip", "403_20122001_20210104000000.txt.zip"};
    String filePath = "src/test/resources/inputs";

    @Test
    public void testRateLoading(int base) {
        JSONObject jsonObject;
        RateLoading rateLoading;
        String ret;

        jsonObject = new JSONObject();
        jsonObject.put("file",      file1[base]);
        jsonObject.put("filePath",  filePath);
        jsonObject.put("paramType", 1);
        rateLoading = new RateLoading();
        ret = rateLoading.rateLoadingMethod(jsonObject.toString());
        JSONObject jsonObject1 = new JSONObject(ret);
        if (jsonObject1.get("code").equals("false")) {
            System.err.println("OhMyGod");
            System.exit(1);
        }

        jsonObject = new JSONObject();
        jsonObject.put("file",      file2[base]);
        jsonObject.put("filePath",  filePath);
        jsonObject.put("paramType", 2);
        rateLoading = new RateLoading();
        ret = rateLoading.rateLoadingMethod(jsonObject.toString());
        jsonObject1 = new JSONObject(ret);
        if (jsonObject1.get("code").equals("false")) {
            System.err.println("OhMyGod");
            System.exit(1);
        }

        jsonObject = new JSONObject();
        jsonObject.put("file",      file3[base]);
        jsonObject.put("filePath",  filePath);
        jsonObject.put("paramType", 3);
        rateLoading = new RateLoading();
        ret = rateLoading.rateLoadingMethod(jsonObject.toString());
        jsonObject1 = new JSONObject(ret);
        if (jsonObject1.get("code").equals("false")) {
            System.err.println("OhMyGod");
            System.exit(1);
        }
    }
}
