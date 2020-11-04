package nju.ics.Algorithm;

import nju.ics.Entity.Graph;
import nju.ics.Entity.Path;
import nju.ics.Entity.RuntimePath;

import java.util.List;

public class NullAlgorithm implements Algorithm {
    public static int cnt = 0;
    public RuntimePath execute(Graph graph, RuntimePath path, List<Double> configs, int vehicleType) {
        //do nothing but return oracle path
        return path;
    }
}
