package pmcsn.estimators;

import pmcsn.entities.Job;

import static java.lang.System.out;

public class Statistics {

    private static final Statistics istanza = new Statistics();

    public static Statistics getInstance() {
        return istanza;
    }

    //lista di copie di Estimator per ogni tipo di metrica
    private Estimator responseTimeEstimator = new Estimator();
    private Estimator waitTimeEstimator = new Estimator();
    private PopulationEstimator populationEstimator = PopulationEstimator.getInstance();
    private  GlobalEstimator globalEstimator = new GlobalEstimator();


    //metodo che aggiorna i vari estimator con la departure di un job
    public void updateEstimators(Job j) {

        //tempi significativi del job
        double arrivalTime = j.getArrivalTime();
        double completeTime = j.getCompleteTime();
        double serviceTime = j.getServiceTime();
        double responseTime = completeTime-arrivalTime;
        double waitTime = responseTime-serviceTime;

        //out.println("JOB di "+j.getNode()+" con a: "+arrivalTime+ " c: "+completeTime+ " s: "+serviceTime);

        //aggiornamento metriche per nodo
        responseTimeEstimator.update(j.getNode(),responseTime);
        waitTimeEstimator.update(j.getNode(),waitTime);

        //aggiornamento metriche globali
        globalEstimator.update(j);

    }

    public void resetStatistics() {
        responseTimeEstimator = new Estimator();
        waitTimeEstimator = new Estimator();
        PopulationEstimator.getInstance().resetPopulation();
        globalEstimator = new GlobalEstimator();
    }
    public void printStatistics() {
        out.println("NODO A: STATISTICHE--------------------");
        out.println("mean response time: "+responseTimeEstimator.getMean("A"));
        out.println("mean wait time: "+waitTimeEstimator.getMean("A"));
        out.println("mean population: "+populationEstimator.getPopulationMean("A"));
        out.println("utilizzazione: "+populationEstimator.getUtilization("A"));
        out.println("throughput: "+populationEstimator.getThroughput("A"));
        out.println("\n\n");

        out.println("NODO B: STATISTICHE--------------------");
        out.println("mean response time: "+responseTimeEstimator.getMean("B"));
        out.println("mean wait time: "+waitTimeEstimator.getMean("B"));
        out.println("mean population: "+populationEstimator.getPopulationMean("B"));
        out.println("utilizzazione: "+populationEstimator.getUtilization("B"));
        out.println("throughput: "+populationEstimator.getThroughput("B"));
        out.println("\n\n");

        out.println("NODO P: STATISTICHE--------------------");
        out.println("mean response time: "+responseTimeEstimator.getMean("P"));
        out.println("mean wait time: "+waitTimeEstimator.getMean("P"));
        out.println("mean population: "+populationEstimator.getPopulationMean("P"));
        out.println("utilizzazione: "+populationEstimator.getUtilization("P"));
        out.println("throughput: "+populationEstimator.getThroughput("P"));
        out.println("\n\n");

        out.println("SISTEMA: STATISTICHE--------------------");
        out.println("mean response time: "+globalEstimator.getResponseTimeMean());
        out.println("mean population: "+populationEstimator.getPopulationMean("SYSTEM"));
        out.println("utilizzazione: "+populationEstimator.getUtilization("SYSTEM"));
        out.println("throughput: "+populationEstimator.getThroughput("SYSTEM"));

        out.println("\n\nSTAMPA VERIFICA-------------------------------------------");
        double temp = responseTimeEstimator.getMean("A")*3+responseTimeEstimator.getMean("B")+responseTimeEstimator.getMean("P");
        out.println("media response time ricalcolata: "+temp);
        temp = populationEstimator.getPopulationMean("A")+populationEstimator.getPopulationMean("B")+populationEstimator.getPopulationMean("P");
        out.println("media popolazione ricalcolata: "+temp);

    }

    public double getPopMean(String node) {
        return populationEstimator.getPopulationMean(node);
    }

    public double getUtilizationMean(String node) {
        return populationEstimator.getUtilization(node);
    }

    public double getResponseTimeMean(String node) {
        if (node == "SYSTEM") {
            return globalEstimator.getResponseTimeMean();
        } else {
            return responseTimeEstimator.getMean(node);
        }
    }

    public double getResponseTimeStandDev(String node) {
        if (node == "SYSTEM") {
            return globalEstimator.getResponseTimeStandardDeviation();
        } else {
            return responseTimeEstimator.getStandardDeviation(node);
        }
    }
}
