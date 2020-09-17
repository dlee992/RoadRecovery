import nju.ics.Main.PathRestoration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@RunWith(Parameterized.class)
public class PathRestorationTest {

    static String test_data_single = "src/test/resources/inputs/single-test-case.txt";
    static String test_data = "src/test/resources/inputs/testdata.txt";
    static int count = 0;

    @Parameterized.Parameters(name = "{index}: assertEquals(DPResult, ManualResult)")
    public static Collection<Object> data() throws IOException {
        Collection<Object> retList = new ArrayList<>();
//        readAFile(retList, test_data);
        readBFile(retList, test_data_single);
        return retList;
    }

    private static void readBFile(Collection<Object> retList, String test_data_file) throws IOException {
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

    private static void readAFile(Collection<Object> retList, String test_data_file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(test_data_file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));

        String strLine;
        while ((strLine = br.readLine()) != null) {
//            System.out.printf("strLine=%s\n", strLine);
            String[] data = strLine.split(",");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", count);
            jsonObject.put("enStationId", data[0]);
            jsonObject.put("exStationId", data[1]);

            if (data[2].charAt(0) != '(') {
                String gantries = data[2];
                int index = 0;
                StringBuilder formalGantries = new StringBuilder(data[0]);
                while (index < gantries.length()) {
                    formalGantries.append('|').append(gantries.substring(index, index + 6));
                    index += 6;
                }
                formalGantries.append('|').append(data[1]);
                jsonObject.put("manualResult", formalGantries.toString());

                //{"enTime":"","exTime":"","enStationId":"G0002370110070","exStationId":"G003W370040050","gantryIdList":[],
                // "modifyCost":0.01,"addCost":0.1,"deleteCost":300,"deleteCost2":2,"deleteEndCost":100000,"vehicleType":1}
                jsonObject.put("enTime", "");
                jsonObject.put("exTime", "");
                JSONArray jsonArray = new JSONArray();
                jsonObject.put("gantryIdList", jsonArray);
                jsonObject.put("modifyCost", 0.01);
                jsonObject.put("addCost", 0.1);
                jsonObject.put("deleteCost", 300);
                jsonObject.put("deleteCost2", 2);
                jsonObject.put("deleteEndCost", 100000);
                jsonObject.put("vehicleType", 1);

                retList.add(jsonObject);
                count++;
            }
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
//                assertEquals(manualResult, DPResult);
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
