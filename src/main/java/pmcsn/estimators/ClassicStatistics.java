package pmcsn.estimators;

import pmcsn.entities.Job;
import pmcsn.files.OutputFileGenerator;

import static java.lang.System.out;

public class ClassicStatistics {

    private Estimator responseTimeEstimator = new Estimator();
    private Estimator waitTimeEstimator = new Estimator();
    private PopulationEstimator populationEstimator = PopulationEstimator.getInstance();
    private  GlobalEstimator globalEstimator = new GlobalEstimator();

    //metodo che aggiorna gli indici locali
    public void updateStatistics(String node, Job job) {
        //tempi significativi del job
        double arrivalTime = job.getArrivalTime();
        double completeTime = job.getCompleteTime();
        double serviceTime = job.getServiceTime();
        double responseTime = completeTime-arrivalTime;
        double waitTime = responseTime-serviceTime;

        //aggiornamento metriche per nodo
        responseTimeEstimator.update(node,responseTime);
        waitTimeEstimator.update(node,waitTime);

        //se il completamento viene da A ed è di classe 2, job uscente -> aggiorno global
        if (node == "A" && (job.getJobClass() == 2 || job.getJobClass() == -1)) {
            updateGlobalStatistics(job);
        }
    }

    public void logSampling(double time, long seed) {

        //response time
        double rtA = responseTimeEstimator.getMean("A");
        double rtB = responseTimeEstimator.getMean("B");
        double rtP = responseTimeEstimator.getMean("P");
        double rtSystem = globalEstimator.getResponseTimeMean();

        OutputFileGenerator.getInstance().logRecordTransient(time,"ResponseTime",rtA,seed,"A");
        OutputFileGenerator.getInstance().logRecordTransient(time,"ResponseTime",rtB,seed,"B");
        OutputFileGenerator.getInstance().logRecordTransient(time,"ResponseTime",rtP,seed,"P");
        OutputFileGenerator.getInstance().logRecordTransient(time,"ResponseTime",rtSystem,seed,"System");

        //popolazione
        double popA = populationEstimator.getPopulationMean("A",time,false);
        double popB = populationEstimator.getPopulationMean("B",time,false);
        double popP = populationEstimator.getPopulationMean("P",time,false);
        double popSystem = populationEstimator.getPopulationMean("System",time,false);

        OutputFileGenerator.getInstance().logRecordTransient(time,"Population",popA,seed,"A");
        OutputFileGenerator.getInstance().logRecordTransient(time,"Population",popB,seed,"B");
        OutputFileGenerator.getInstance().logRecordTransient(time,"Population",popP,seed,"P");
        OutputFileGenerator.getInstance().logRecordTransient(time,"Population",popSystem,seed,"System");

        //throughput
        double thrA = populationEstimator.getThroughput("A",time,false);
        double thrB = populationEstimator.getThroughput("B",time,false);
        double thrP = populationEstimator.getThroughput("P",time,false);
        double thrSystem = populationEstimator.getThroughput("System",time,false);

        OutputFileGenerator.getInstance().logRecordTransient(time,"Throughput",thrA,seed,"A");
        OutputFileGenerator.getInstance().logRecordTransient(time,"Throughput",thrB,seed,"B");
        OutputFileGenerator.getInstance().logRecordTransient(time,"Throughput",thrP,seed,"P");
        OutputFileGenerator.getInstance().logRecordTransient(time,"Throughput",thrSystem,seed,"System");

        //utilizzazione
        double utA = populationEstimator.getUtilization("A",time,false);
        double utB = populationEstimator.getUtilization("B",time,false);
        double utP = populationEstimator.getUtilization("P",time,false);
        double utSystem = populationEstimator.getUtilization("System",time,false);

        OutputFileGenerator.getInstance().logRecordTransient(time,"Utilization",utA,seed,"A");
        OutputFileGenerator.getInstance().logRecordTransient(time,"Utilization",utB,seed,"B");
        OutputFileGenerator.getInstance().logRecordTransient(time,"Utilization",utP,seed,"P");
        OutputFileGenerator.getInstance().logRecordTransient(time,"Utilization",utSystem,seed,"System");

        //waitime
        double wA = waitTimeEstimator.getMean("A");
        double wB = waitTimeEstimator.getMean("B");
        double wP = waitTimeEstimator.getMean("P");

        OutputFileGenerator.getInstance().logRecordTransient(time,"WaitTime",wA,seed,"A");
        OutputFileGenerator.getInstance().logRecordTransient(time,"WaitTime",wB,seed,"B");
        OutputFileGenerator.getInstance().logRecordTransient(time,"WaitTime",wP,seed,"P");
    }

    //metodo che aggiorna gli indici globali
    private void updateGlobalStatistics(Job job) {
        globalEstimator.update(job);
    }

    //metodo per resettare gli estimators
    public void resetStatistics() {
        responseTimeEstimator = new Estimator();
        waitTimeEstimator = new Estimator();
        populationEstimator.hardReset();
        globalEstimator = new GlobalEstimator();
    }

    public void printStatistics(double finish) {

        out.println("NODO A: STATISTICHE--------------------");
        out.println("mean response time: "+responseTimeEstimator.getMean("A"));
        out.println("mean wait time: "+waitTimeEstimator.getMean("A"));
        out.println("mean population: "+populationEstimator.getPopulationMean("A",finish,false));
        out.println("utilizzazione: "+populationEstimator.getUtilization("A",finish,false));
        out.println("throughput: "+populationEstimator.getThroughput("A",finish,false));
        out.println("\n\n");


        out.println("NODO B: STATISTICHE--------------------");
        out.println("mean response time: "+responseTimeEstimator.getMean("B"));
        out.println("mean wait time: "+waitTimeEstimator.getMean("B"));
        out.println("mean population: "+populationEstimator.getPopulationMean("B",finish,false));
        out.println("utilizzazione: "+populationEstimator.getUtilization("B",finish,false));
        out.println("throughput: "+populationEstimator.getThroughput("B",finish,false));
        out.println("\n\n");


        out.println("NODO P: STATISTICHE--------------------");
        out.println("mean response time: "+responseTimeEstimator.getMean("P"));
        out.println("mean wait time: "+waitTimeEstimator.getMean("P"));
        out.println("mean population: "+populationEstimator.getPopulationMean("P",finish,false));
        out.println("utilizzazione: "+populationEstimator.getUtilization("P",finish,false));
        out.println("throughput: "+populationEstimator.getThroughput("P",finish,false));
        out.println("\n\n");

        out.println("SISTEMA: STATISTICHE--------------------");
        out.println("mean response time: "+globalEstimator.getResponseTimeMean());
        out.println("mean population: "+populationEstimator.getPopulationMean("System",finish,false));
        out.println("utilizzazione: "+populationEstimator.getUtilization("System",finish,false));
        out.println("throughput: "+populationEstimator.getThroughput("System",finish,false));

        /*
        out.println("\n\nSTAMPA VERIFICA-------------------------------------------");
        double temp = responseTimeEstimator.getMean("A")*3+responseTimeEstimator.getMean("B")+responseTimeEstimator.getMean("P");
        out.println("media response time ricalcolata: "+temp);
        temp = populationEstimator.getPopulationMean("A",finish)+populationEstimator.getPopulationMean("B",finish)+populationEstimator.getPopulationMean("P",finish);
        out.println("media popolazione ricalcolata: "+temp);
        */
    }
}
