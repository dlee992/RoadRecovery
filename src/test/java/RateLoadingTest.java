import nju.ics.Main.RateLoading;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

//@RunWith(Parameterized.class)
public class RateLoadingTest {

    @Test
    public void testRateLoading() {
        String file     = "401_20090201_20200506000000.txt.zip";
        String filePath = "/home/lida/Desktop/highway";
        int paramType   = 1;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("file",      file);
        jsonObject.put("filePath",  filePath);
        jsonObject.put("paramType", paramType);

        RateLoading rateLoading = new RateLoading();
        String ret = rateLoading.rateLoadingMethod(jsonObject.toString());
    }
}
