package nju.ics.Tool;

import nju.ics.Entity.Edge;
import nju.ics.Entity.Graph;
import nju.ics.Entity.Node;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static nju.ics.Entity.NodeType.*;

public class ReadExcel {

    int EDGESHEET = 1;
    int MUTUALSHEET = 3;
    int MILEAGESHEET = 6;

    private Graph graph = new Graph();

    public Graph buildGraph(String filePath) {
        try {
            Workbook workbook = WorkbookFactory.create(new File(filePath));
            Iterator<Sheet> sheetIterator = workbook.sheetIterator();
            while (sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();
                switch (sheet.getSheetName()) {
                    case "1":  // all edge information
                        addEdgeFromSheet(sheet, EDGESHEET);
                        break;
                    case "3":  // all mutual information
                        addEdgeFromSheet(sheet, MUTUALSHEET);
                        break;
                    case "门架里程":
                        addEdgeFromSheet(sheet, MILEAGESHEET);
                        break;
                }
            }
            System.out.println("node size = " + graph.nodes.size());
            graph.buildAllShortestPathByDijkstra();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return graph;
    }

    private void addEdgeFromSheet(Sheet sheet, int sheetIndex) {

        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {

            Row row = rowIterator.next();
            int SHEETHEAD1AND3 = 4;
            int SHEETHEAD6 = 3;
            if ((sheetIndex <=3 && row.getRowNum() < SHEETHEAD1AND3) ||
                    (sheetIndex == 6 && row.getRowNum() < SHEETHEAD6)) continue;

            //Specifically for sheet 6
//            System.out.println("node size = " + graph.nodes.size());

            Node inNode = extractNodeFromRow(row, 1);

            if (sheetIndex == 6) {
                if (inNode == null || !graph.nodes.contains(inNode)) continue;

                inNode = graph.nodes.get(graph.nodes.indexOf(inNode));
                inNode.mileage = (long) row.getCell(3).getNumericCellValue();
                continue;
            }

            /*
             Add two nodes and one edge into graph
             */
            //add two nodes
            Node outNode = extractNodeFromRow(row, 4);
            // exist outNode is {0, wu, wu} | {index, wu, wu}
            if (inNode == null || outNode == null) continue;
            if (!graph.nodes.contains(inNode)) {
                graph.nodes.add(inNode);
            } else {
                inNode = graph.nodes.get(graph.nodes.indexOf(inNode));
            }
            if (!graph.nodes.contains(outNode)) {
                graph.nodes.add(outNode);
            } else {
                outNode = graph.nodes.get(graph.nodes.indexOf(outNode));
            }
            //I assume each node has only one mutual node.
            //FIXED: exist two mutual relations are miss-typed, and I fixed it manually in excel file.
            if (sheetIndex == MUTUALSHEET) {
                inNode.mutualNode = outNode;
                outNode.mutualNode = inNode;
            }

            //add one edge into edgeSet or mutualSet
            Edge edge = new Edge();
            edge.inNode = inNode;
            edge.outNode = outNode;
            if (sheetIndex == EDGESHEET) {
                graph.edgeSet.add(edge);
            }
        }
    }

    private Node extractNodeFromRow(Row row, int base) {
        Node node = new Node();

        // null node
        if (row.getCell(base+1).getStringCellValue().equals("无"))
            return null;

        try {
            if (row.getCell(base).getCellType() == CellType.NUMERIC) {
                node.index = String.valueOf(row.getCell(base).getNumericCellValue());
            }
            else if (row.getCell(base).getCellType() == CellType.STRING) {
                node.index = row.getCell(base).getStringCellValue();
            }

            if (row.getCell(base+1).getCellType() == CellType.NUMERIC) {
                node.name = String.valueOf(row.getCell(base+1).getNumericCellValue());
            }
            else if (row.getCell(base+1).getCellType() == CellType.STRING) {
                node.name = row.getCell(base+1).getStringCellValue();
            }

            int tmpType = -1;
            if (row.getCell(base+2).getCellType() == CellType.NUMERIC) {
                tmpType = (int) row.getCell(base+2).getNumericCellValue();
            }
            else if (row.getCell(base+2).getCellType() == CellType.STRING) {
                tmpType = Integer.parseInt(row.getCell(base+2).getStringCellValue());
            }

            switch (tmpType) {
                case 0: node.type = NORMALPORTAL; break;
                case 1: node.type = PROVINCIALPORTAL; break;
                case 3: node.type = TOLLSTATION; break;
            }
        }
        catch (IllegalStateException exc) {
            System.err.println("Error location: " +
                    "sheet name=" + row.getSheet().getSheetName() +
                    " row=" + (row.getCell(base).getRowIndex()+1) +
                    " column=" + (row.getCell(base).getColumnIndex()+1));
        }

        return node;
    }
}
