package pmcsn.estimators;

import pmcsn.entities.Job;

import java.util.ArrayList;

public class GlobalEstimator {
    private Welford responseTimeEstimator;
    private ArrayList<Job> list;

    public GlobalEstimator() {
        this.list = new ArrayList<>();
        this.responseTimeEstimator = new Welford();
    }

    public void update(Job job) {
        if(job.getJobClass() == -1 && job.getNode() == "A") {
            //arrivo nel sistema
            onArrival(job);
        } else if (job.getJobClass() == 2 && job.getNode() == "A") {
            //uscita dal sistema
            onExit(job);
        }
    }

    private void onArrival(Job job) {
        //quando arriva un job lo salvo nella lista
        list.add(job);
    }

    private void onExit(Job job) {
        for (Job j: list) {
            if (j.getId() == job.getId()) {
                //il job ha completato
                //i due job sono differenti in java ma concettualmente stesso job
                double responseTime = job.getCompleteTime() - j.getArrivalTime();
                responseTimeEstimator.addData(responseTime);
                list.remove(j);
                return;
            }
        }
    }

    public double getResponseTimeMean() {
        return responseTimeEstimator.getMean();
    }
    public double getResponseTimeStandardDeviation() {return responseTimeEstimator.getStandardDeviation(); }
}
