import nju.ics.Main.PathRestoration;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@RunWith(Parameterized.class)
public class PathRestorationTest {

    static String test_data = "src/test/resources/inputs/test-data.txt";
    static int count = 0;

    @Parameterized.Parameter
    public JSONObject testCase;

    @Parameterized.Parameters(name = "{index}: assertEquals(DPResult, ManualResult)")
    public static Collection<Object> data() throws IOException {
        Collection<Object> retList = new ArrayList<>();
        readBFile(retList, test_data);
        return retList;
    }

    private static void readBFile(Collection<Object> retList, String test_data_file)
            throws IOException {
        FileInputStream fileInputStream = new FileInputStream(test_data_file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));

        String strLine;
        while ((strLine = br.readLine()) != null) {
            JSONObject jsonObject = new JSONObject(strLine);
            retList.add(jsonObject);
            count++;
        }
        fileInputStream.close();
    }

    @Test
    public void testPathRestorationWithNewCases() {
        RateLoadingTest rateLoadingTest = new RateLoadingTest();
        rateLoadingTest.testRateLoading();

        System.out.println("testcase = " + testCase.toString());

        PathRestoration pathRestoration = new PathRestoration();
        String ret = pathRestoration.pathRestorationMethod(testCase.toString());
        try {
            if (pathRestoration.recoveredPath != null) {
                pathRestoration.recoveredPath.print("cost fix result");
                String[] intellijResult = pathRestoration.recoveredPath.getStringArray();
                String[] manualResult = testCase.getString("manualResult").split("\\|");
                Assert.assertArrayEquals(manualResult, intellijResult);
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
