package nju.ics.Algorithm;

import nju.ics.Entity.Graph;
import nju.ics.Entity.Path;
import nju.ics.Entity.RuntimePath;

import java.util.List;

public interface Algorithm {

    public RuntimePath execute(Graph graph, RuntimePath path, List<Double> configs, int vehicleType);
}
