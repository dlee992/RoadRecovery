import nju.ics.Entity.Edge;
import nju.ics.Entity.Node;
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

    static int caseCount = 0;

    @Test
    public void runWithTestCase() {
        RateLoadingTest rateLoadingTest = new RateLoadingTest();
        if (++caseCount == 1)
            rateLoadingTest.testRateLoading(0);
        else if (caseCount == 5)
            rateLoadingTest.testRateLoading(1);

        PathRestoration pathRestoration = new PathRestoration();
        String ret = pathRestoration.pathRestorationMethod(testCase.toString());
        System.out.println(ret);
        try {
            if (pathRestoration.recoveredPath != null) {
                pathRestoration.recoveredPath.print("Final recovered path");
                String[] intellijResult = pathRestoration.recoveredPath.getStringArray();
                String[] manualResult = testCase.getString("manualResult").split("\\|");
                Assert.assertTrue(isValid(manualResult,
                        testCase.getString("enStationId"),
                        testCase.getString("exStationId")));
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

    private boolean isValid(String[] manualResult, String enStationId, String exStationId) {
        int length = manualResult.length;
        if (!connected(enStationId, manualResult[0])) {
            System.err.println("start not connected");
            return false;
        }
        if (!connected(manualResult[length - 1], exStationId)) {
            System.err.println("end not connected");
            return false;
        }

        for (int i = 0; i < length-1; i++) {
            if (!connected(manualResult[i], manualResult[i + 1])) {
                System.err.printf("%s is not connected with %s\n", manualResult[i], manualResult[i+1]);
                return false;
            }
        }
        return true;
    }

    private boolean connected(String prevId, String nextId) {
        List<Node> nodes = PathRestoration.graph.nodes;
        int index = nodes.indexOf(new Node(prevId));
        if (index < 0) {
//            System.err.println("first node not exists.");
            return false;
        }
        Node prevNode = nodes.get(index);

        index = nodes.indexOf(new Node(nextId));
        if (index < 0) {
//            System.err.printf("second node %s not exists.", nextId);
            return false;
        }
        Node nextNode = nodes.get(index);
        if (prevNode == null || nextNode == null) return true;

//        System.err.println("two nodes exist.");

        Edge edge = new Edge(prevNode, nextNode);
        if (PathRestoration.graph.edges.contains(edge)) return false;
        return true;
    }
}
