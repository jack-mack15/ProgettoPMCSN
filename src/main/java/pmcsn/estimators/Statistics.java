package pmcsn.estimators;

import pmcsn.entities.Job;

import static java.lang.System.out;

public class Statistics {

    private static final Statistics istanza = new Statistics();

    public static Statistics getInstance() {
        return istanza;
    }

    //lista di copie di Estimator per ogni tipo di metrica
    private final Estimator responseTimeEstimator = new Estimator();
    private final Estimator throughputEstimator = new Estimator();
    private final Estimator waitTimeEstimator = new Estimator();
    private final PopulationEstimator populationEstimator = PopulationEstimator.getInstance();


    //metodo che aggiorna i vari estimator con l'arrivo di un job completato
    public void updateEstimators(Job j) {

        //tempi significativi del job
        double arrivalTime = j.getArrivalTime();
        double completeTime = j.getCompleteTime();
        double serviceTime = j.getServiceTime();
        double responseTime = completeTime-arrivalTime;
        double waitTime = responseTime-serviceTime;

        out.println("JOB di "+j.getNode()+" con a: "+arrivalTime+ " c: "+completeTime+ " s: "+serviceTime);

        //aggiornamento metriche per nodo
        responseTimeEstimator.update(j.getNode(),responseTime);
        waitTimeEstimator.update(j.getNode(),waitTime);

        //aggiornamento metriche globali
        responseTimeEstimator.update("G",responseTime);
        waitTimeEstimator.update("G",waitTime);

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
        out.println("mean response time: "+responseTimeEstimator.getMean("G"));
        out.println("mean wait time: "+waitTimeEstimator.getMean("G"));
        out.println("mean population: "+populationEstimator.getPopulationMean("SYSTEM"));
        out.println("utilizzazione: "+populationEstimator.getUtilization("SYSTEM"));
        out.println("throughput: "+populationEstimator.getThroughput("SYSTEM"));
    }
}
