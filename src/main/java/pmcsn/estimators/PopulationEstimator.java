package pmcsn.estimators;

import pmcsn.entities.Job;

import static java.lang.System.out;

public class PopulationEstimator {

    private static final PopulationEstimator istance = new PopulationEstimator();

    public static PopulationEstimator getInstance() {
        return istance;
    }
    private final Population popA = new Population();
    private final Population popB = new Population();
    private final Population popP = new Population();
    private final Population popSystem = new Population();

    public void updatePopulationOnArrival(Job j) {
        Population temp = getPopulation(j.getNode());
        if (temp != null) {
            temp.jobArrival(j.getArrivalTime());
        }
        if (j.getJobClass() == -1) {
            popSystem.jobArrival(j.getArrivalTime());
        }
    }

    public void updatePopulationOnDeparture(Job j) {
        Population temp = getPopulation(j.getNode());
        if (temp != null) {
            temp.jobDeparture(j.getCompleteTime());
        }
        if (j.getJobClass() == 2 && j.getNode() == "A") {
            popSystem.jobDeparture(j.getCompleteTime());
        }
    }

    private Population getPopulation(String node) {
        switch (node) {
            case "A":
                return popA;
            case "B":
                return popB;
            case "P":
                return popP;
            case "SYSTEM":
                return popSystem;
            default:
                return null;
        }
    }

    //metodo che ritorna le medie della popolazione per un nodo o per il sistema
    //per il sistema usare la stringa SYSTEM
    public double getPopulationMean(String node) {
        return getPopulation(node).getPopulationMean();
    }

    //simile al metodo di sopra ma per la varianza
    public double getPopulationVariance(String node) {
        return getPopulation(node).getPopulationVariance();
    }

    //metodo per ottenere l'utilization
    public double getUtilization(String node) {
        Population pop = getPopulation(node);
        if (pop != null) {
            return pop.getUtilization();
        }
        return -1.0;
    }

    public double getThroughput(String node) {
        Population pop = getPopulation(node);
        if (pop != null) {
            return pop.getThroughput();
        }
        return -1.0;
    }


}
