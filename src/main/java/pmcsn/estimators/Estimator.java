package pmcsn.estimators;

import java.util.Objects;

public class Estimator {

    private Welford nodeAEstimator = new Welford();
    private Welford nodeBEstimator = new Welford();
    private Welford nodePEstimator = new Welford();

    public void update(String node, double x) {
        if(Objects.equals(node, "A")) {
            nodeAEstimator.addData(x);
        } else if ("B".equals(node)) {
            nodeBEstimator.addData(x);
        } else {
            //node == P
            nodePEstimator.addData(x);
        }
    }

    public double getMean(String node) {
        switch (node) {
            case "A":
                return nodeAEstimator.getMean();
            case "B":
                return nodeBEstimator.getMean();
            case "P":
                return nodePEstimator.getMean();
            default:
                return -1.0;
        }
    }

    public double getStandardDeviation(String node) {
        switch (node) {
            case "A":
                return nodeAEstimator.getStandardDeviation();
            case "B":
                return nodeBEstimator.getStandardDeviation();
            case "P":
                return nodePEstimator.getStandardDeviation();
            default:
                return -1.0;
        }
    }

    public double getVariance(String node) {
        switch (node) {
            case "A":
                return nodeAEstimator.getVariance();
            case "B":
                return nodeBEstimator.getVariance();
            case "P":
                return nodePEstimator.getVariance();
            default:
                return -1.0;
        }
    }

}
