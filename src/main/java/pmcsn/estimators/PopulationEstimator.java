package pmcsn.estimators;

import pmcsn.entities.Job;

public class PopulationEstimator {

    private static final PopulationEstimator istance = new PopulationEstimator();


    public static PopulationEstimator getInstance() {
        return istance;
    }

    private Population[] batches = new Population[4];
    private Population[] stats = new Population[4];
    /*private Population popA = new Population();
    private Population popB = new Population();
    private Population popP = new Population();
    private Population popSystem = new Population();*/

    private double finishTime = 0.0;

    //metodo per resettare le population
    public void resetPopulation(String node, double time, boolean isBatch) {
        getPopulation(node,isBatch).resetPopulation(time);
    }

    public void setPopulations() {
        batches[0] = new Population(); //per A
        batches[1] = new Population(); //per B
        batches[2] = new Population(); //per P
        batches[3] = new Population(); //per system
        stats[0] = new Population(); //per A
        stats[1] = new Population(); //per B
        stats[2] = new Population(); //per P
        stats[3] = new Population(); //per system
    }

    public void hardReset() {
        stats[0].hardReset();
        stats[1].hardReset();
        stats[2].hardReset();
        stats[3].hardReset();
    }

    /*public void resetAllPopulation(double time) {
        popA.resetPopulation(time);
        popB.resetPopulation(time);
        popP.resetPopulation(time);
        popSystem.resetPopulation(time);
    }*/

    //metodo che aggiorna la popolazione del nodo a cui arriva un job
    public void updatePopulationOnArrival(Job j) {
        Population tempStats = getPopulation(j.getNode(),false);
        Population tempBatch = getPopulation(j.getNode(),true);
        if (tempStats != null && tempBatch != null) {
            tempStats.jobArrival(j.getArrivalTime());
            tempBatch.jobArrival(j.getArrivalTime());
        }
        if (j.getJobClass() == -1) {
            stats[3].jobArrival(j.getArrivalTime());
            batches[3].jobArrival(j.getArrivalTime());
        }
    }

    //meotodo che aggiorna la popolazione del nodo da cui parte un job
    public void updatePopulationOnDeparture(Job j) {
        Population tempStat = getPopulation(j.getNode(),false);
        Population tempBatch = getPopulation(j.getNode(),true);

        if (tempStat != null && tempBatch != null) {
            tempStat.jobDeparture(j.getCompleteTime());
            tempBatch.jobDeparture(j.getCompleteTime());
        }

        if (j.getJobClass() == 2 && j.getNode() == "A") {
            stats[3].jobDeparture(j.getCompleteTime());
            batches[3].jobDeparture(j.getCompleteTime());
        }
    }

    public void debug(String node,double time) {
        getPopulation(node,true).debug(time);
    }

    //funzione ausilio per ottenere l'istanza corretta di Population
    private Population getPopulation(String node, boolean isBatch) {

        Population[] temp = stats;
        if(isBatch) {
            temp = batches;
        }
        switch (node) {
            case "A":
                return temp[0];
            case "B":
                return temp[1];
            case "P":
                return temp[2];
            case "System":
                return temp[3];
            default:
                return null;
        }
    }

    //metodo che ritorna le medie della popolazione per un nodo o per il sistema
    //per il sistema usare la stringa SYSTEM
    public double getPopulationMean(String node, double time, boolean isBatch) {
        return getPopulation(node,isBatch).getPopulationMean(time);
    }

    //simile al metodo di sopra ma per la varianza
    public double getPopulationVariance(String node,boolean isBatch) {
        return getPopulation(node,isBatch).getPopulationVariance(finishTime);
    }

    //metodo per ottenere l'utilization
    public double getUtilization(String node, double time, boolean isBatch) {
        Population pop = getPopulation(node,isBatch);
        if (pop != null) {
            return pop.getUtilization(time);
        }
        return -1.0;
    }

    public double getThroughput(String node, double time, boolean isBatch) {
        Population pop = getPopulation(node, isBatch);
        if (pop != null) {
            return pop.getThroughput(time);
        }
        return -1.0;
    }

    public void setFinishTime(double time) {
        finishTime = time;
    }


}
