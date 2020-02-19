package Entity;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static jdk.nashorn.api.scripting.ScriptObjectMirror.identical;

public class PathSet {

    public List<Path> paths = new ArrayList<Path>();
    public Path finalPath;
    public Path oraclePath = new Path();
    String testFileName = "src/main/resources/test-data.xls";
    int SheetHead = 3;
    String outDirectory = "src/main/resources/outputs/";

    /*
    FIXME: I don't where is test oracle.
    read two kinds of paths, combined into a path set.
     */
    public void readAllPath(Graph graph, int testIndex) {
        System.out.println("testIndex=" + testIndex);
        try {
            /*
            get steam info and test oracle.
             */
            Workbook workbook = WorkbookFactory.create(new File(testFileName));

            //get steam info
            Sheet sheet = workbook.getSheetAt(testIndex*2-1);
            extractOnePath(graph, sheet, 0); // first path:  simplified path
            extractOnePath(graph, sheet, 2); // second path: complete path

            //get test oracle (a complete path)
            /*
            sheet = workbook.getSheetAt(testIndex*2);
            rowIterator = sheet.rowIterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getRowNum() < SHEETHEAD) continue;
                System.out.println(row.getRowNum()+1);
                if (row.getCell(2) == null ||
                        row.getCell(3) == null || row.getCell(5) == null) continue;
                Node node = new Node();
                node.index = row.getCell(2).getStringCellValue();
                node.name = row.getCell(3).getStringCellValue();
                String portalSource = row.getCell(5).getStringCellValue();
                if (portalSource.equals("收费站")) {
                    node.source = NodeSource.TOLLSTATION;
                }
                else if (portalSource.equals("标识出的门架")) {
                    node.source = NodeSource.IDENTIFY;
                }
                else if (portalSource.equals("还原出的门架")) {
                    node.source = NodeSource.RECOVER;
                }
                //node.type can be completed with graph information
                oraclePath.nodeList.add(node);
            }
             */

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void extractOnePath(Graph graph, Sheet sheet, int columnBase) {
        Path path = new Path();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() < SheetHead) continue;
//            System.out.println(row.getRowNum()+1);
            if (row.getCell(columnBase) == null || row.getCell(columnBase+1) == null) continue;
            Node node = new Node();
            node.index = row.getCell(columnBase).getStringCellValue();
            node.name = row.getCell(columnBase+1).getStringCellValue();
            //avoid to read empty cells
            if (node.index.length() !=6 && node.index.length() != 14) continue;
//            System.out.println(node.index+", "+node.name);
            //node.type can be completed with graph information
            path.nodeList.add(graph.nodes.get(graph.nodes.indexOf(node))); // node's id can be different from the node in graph
        }
        paths.add(path); //testcase has only one path.
    }

    public void compareAndPrint(Graph graph, Path oraclePath, int testIndex) {
        /*
         if equal, print "Successful recovery"
         else, print "Failed recovery"

         And print complete path.
         */
        //compare two paths
        //FIXME: I don't know oracle. Refer to Pathset.java line 19.
        boolean successful = true;
        /*
        Iterator<Node> ourNodeIterator = nodeList.iterator();
        Iterator<Node> oracleIterator = oraclePath.nodeList.iterator();
        while (ourNodeIterator.hasNext()) {
            Node completedNode = ourNodeIterator.next();
            if (!oracleIterator.hasNext()) {
                successful = false;
                break;
            }
            Node oracleNode = oracleIterator.next();
            if (!completedNode.index.equals(oracleNode.index) ||
                !completedNode.name.equals(oracleNode.name) ||
                completedNode.source!=oracleNode.source) {
                successful = false;
                break;
            }
        }
         */

        //TODO: if any two paths of paths are identical, final path = anyone;
        //      else final path = shortest path of beginning and ending.
        for (int i = 0; i < paths.size()-1; i++) {
            for (int j = i+1; j < paths.size(); j++) {
                if (!identicalPath(paths.get(i), paths.get(j))) {
                    successful = false; break;
                }
            }
        }

        if (successful) {
            System.out.println("Success: All recovered paths are identical.");
            finalPath = paths.get(0);
        }
        else {
            System.out.println("Failure: Recovered paths are different.");
            List<Node> nodeList = paths.get(0).nodeList;
            Node beginNode = nodeList.get(0);
            Node endNode = nodeList.get(nodeList.size()-1);
            finalPath = graph.getShortestPath(beginNode, endNode);
        }

        //save result
        //correctly output chinese characters
        try {
            //use Excel file to save outputs.
            String[] columns = {"门架HEX/收费站编号","收费站/门架名称","门架来源"};
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("case " + testIndex);
            Row row = sheet.createRow(0);
            if (successful) row.createCell(0).setCellValue("Success: All recovered paths are identical.");
            else row.createCell(0).setCellValue("Failure: Recovered paths are different.");
            row = sheet.createRow(1);
            row.createCell(0).setCellValue("Recovered path");
            row.createCell(3).setCellValue("Oracle path");
            row = sheet.createRow(2);
            for (int columnIndex = 0; columnIndex < 6; columnIndex++) {
                row.createCell(columnIndex).setCellValue(columns[columnIndex%3]);
            }
            int rowIndex = 3;
            for (Node node: finalPath.nodeList
            ) {
                row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(node.index);
                row.createCell(1).setCellValue(node.name);
                if (node.source == NodeSource.IDENTIFY)
                    row.createCell(2).setCellValue("标记出的点");
                else if (node.source == NodeSource.RECOVER)
                    row.createCell(2).setCellValue("还原出的点");
                else row.createCell(2).setCellValue("不明出处的点");
            }

            // Write the output to a file
            File outDir = new File(outDirectory);
            if (!outDir.exists()) outDir.mkdir();
            String filename = outDirectory + testIndex + ".xlsx";
//            File file = new File(filename);
//            if (!file.exists()) file.createNewFile();
            FileOutputStream fileOut = new FileOutputStream(filename);
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean identicalPath(Path path1, Path path2) {
        for (int i = 0; i < path1.nodeList.size(); i++) {
            Node node1 = path1.nodeList.get(i);
            Node node2 = null;
            if (path2.nodeList.size() > i)
                node2 = path2.nodeList.get(i);
            if (!(node1.equals(node2))) return false;
        }
        return true;
    }
}
