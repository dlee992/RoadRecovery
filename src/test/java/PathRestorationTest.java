import nju.ics.Main.PathRestoration;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@RunWith(Parameterized.class)
public class PathRestorationTest {

    static String test_data_single = "src/test/resources/inputs/single-test-case.txt";
    static List<String> originDPResults = new ArrayList<>();
    static int count = 0;

    @Parameterized.Parameters(name = "{index}: assertEquals(DPResult, ManualResult)")
    public static Collection<Object> data() throws IOException {
        Collection<Object> retList = new ArrayList<>();
        readAFile(retList, test_data_single);
        return retList;
    }

    private static void readAFile(Collection<Object> retList, String test_data_file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(test_data_file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));

        String strLine;
        while ((strLine = br.readLine()) != null) {
            JSONObject jsonObject = new JSONObject(strLine);
            jsonObject.put("index", count);

            retList.add(jsonObject);
            count++;
        }
        fileInputStream.close();
    }

    @Parameterized.Parameter
    public JSONObject testCase;

    @Test
    public void testPathRestorationWithNewCases()  {
        //TODO: load testing data
        RateLoadingTest rateLoadingTest = new RateLoadingTest();
        rateLoadingTest.testRateLoading();

        System.out.println("testcase = " + testCase.toString());

        PathRestoration pathRestoration = new PathRestoration();
        String ret = pathRestoration.pathRestorationMethod(testCase.toString());
        try {
            if (pathRestoration.recoveredPath != null) {
                String DPResult = pathRestoration.recoveredPath.getLiteralPath();
                pathRestoration.recoveredPath.print("DP result");
//                System.out.println(DPResult);
                System.out.println(ret);
                String manualResult = testCase.getString("manualResult");
                System.out.println(manualResult);
                assertEquals(manualResult, DPResult);
            }
            else {
                System.err.println(ret);
            }
        }
        catch (JSONException Exp) {
            //do nothing.
        }
    }
}
