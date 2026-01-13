package pmcsn.estimators;

import pmcsn.entities.Job;

public class PopulationEstimator {

    private static final PopulationEstimator istance = new PopulationEstimator();

    public static PopulationEstimator getInstance() {
        return istance;
    }
    private Population popA = new Population();
    private Population popB = new Population();
    private Population popP = new Population();
    private Population popSystem = new Population();

    private double finishTime = 0.0;

    //metodo per resettare le population
    public void resetPopulation(String node, double time) {
        getPopulation(node).resetPopulation(time);
    }

    public void hardReset() {
        popA.hardReset();
        popB.hardReset();
        popP.hardReset();
        popSystem.hardReset();
    }

    public void resetAllPopulation(double time) {
        popA.resetPopulation(time);
        popB.resetPopulation(time);
        popP.resetPopulation(time);
        popSystem.resetPopulation(time);
    }

    //metodo che aggiorna la popolazione del nodo a cui arriva un job
    public void updatePopulationOnArrival(Job j) {
        Population temp = getPopulation(j.getNode());
        if (temp != null) {
            temp.jobArrival(j.getArrivalTime());
        }
        if (j.getJobClass() == -1) {
            popSystem.jobArrival(j.getArrivalTime());
        }
    }

    //meotodo che aggiorna la popolazione del nodo da cui parte un job
    public void updatePopulationOnDeparture(Job j) {
        Population temp = getPopulation(j.getNode());
        if (temp != null) {
            temp.jobDeparture(j.getCompleteTime());
        }
        if (j.getJobClass() == 2 && j.getNode() == "A") {
            popSystem.jobDeparture(j.getCompleteTime());
        }
    }

    //funzione ausilio per ottenere l'istanza corretta di Population
    private Population getPopulation(String node) {
        switch (node) {
            case "A":
                return popA;
            case "B":
                return popB;
            case "P":
                return popP;
            case "System":
                return popSystem;
            default:
                return null;
        }
    }

    //metodo che ritorna le medie della popolazione per un nodo o per il sistema
    //per il sistema usare la stringa SYSTEM
    public double getPopulationMean(String node, double time) {
        return getPopulation(node).getPopulationMean(time);
    }

    //simile al metodo di sopra ma per la varianza
    public double getPopulationVariance(String node) {
        return getPopulation(node).getPopulationVariance(finishTime);
    }

    //metodo per ottenere l'utilization
    public double getUtilization(String node, double time) {
        Population pop = getPopulation(node);
        if (pop != null) {
            return pop.getUtilization(time);
        }
        return -1.0;
    }

    public double getThroughput(String node, double time) {
        Population pop = getPopulation(node);
        if (pop != null) {
            return pop.getThroughput(time);
        }
        return -1.0;
    }

    public void setFinishTime(double time) {
        finishTime = time;
    }


}
