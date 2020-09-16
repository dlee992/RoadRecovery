import nju.ics.Main.RateLoading;
import org.json.JSONObject;
import org.junit.Test;


public class RateLoadingTest {

    @Test
    public void testRateLoading() {
        String file1    = "401_20090201_20200506000000.txt.zip";
        String file2    = "402_20081901_20200819205649.txt.zip";
        String file3    = "403_20082001_20200820092752.txt.zip";
        String filePath = "src/test/resources/inputs";

        JSONObject jsonObject;
        RateLoading rateLoading;
        String ret;

        jsonObject = new JSONObject();
        jsonObject.put("file",      file1);
        jsonObject.put("filePath",  filePath);
        jsonObject.put("paramType", 1);
        rateLoading = new RateLoading();
        ret = rateLoading.rateLoadingMethod(jsonObject.toString());

        jsonObject = new JSONObject();
        jsonObject.put("file",      file2);
        jsonObject.put("filePath",  filePath);
        jsonObject.put("paramType", 2);
        rateLoading = new RateLoading();
        ret = rateLoading.rateLoadingMethod(jsonObject.toString());

        jsonObject = new JSONObject();
        jsonObject.put("file",      file3);
        jsonObject.put("filePath",  filePath);
        jsonObject.put("paramType", 3);
        rateLoading = new RateLoading();
        ret = rateLoading.rateLoadingMethod(jsonObject.toString());
    }
}
