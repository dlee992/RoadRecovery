package nju.ics.Entity;

import static nju.ics.Entity.NodeSource.IDENTIFY;

import java.util.ArrayList;
import java.util.List;

public class Path {

    public List<Node> nodeList = new ArrayList<Node>();

    public long getLength() {
        long length = 0;
        for (int i = 1; i < nodeList.size() - 1; ++i) {
            length += nodeList.get(i).mileage;
        }
        return length;
    }

}
