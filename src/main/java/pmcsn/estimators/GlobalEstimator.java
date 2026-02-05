package pmcsn.estimators;

import pmcsn.entities.Job;
import pmcsn.files.OutputFileGenerator;

import java.util.ArrayList;

import static java.lang.System.out;

//classe estimator per sistema globale
public class GlobalEstimator {
    private Welford responseTimeEstimator;
    private ArrayList<Job> list;

    public GlobalEstimator() {
        this.list = new ArrayList<>();
        this.responseTimeEstimator = new Welford();
    }

    //metodo che viene invocato all'arrivo o all'uscita di un job nel sistema.
    //in realtà un nuovo job viene aggiunto alla lista di questa classe quando A completa tale job. Non è un problema perchè il job possiede
    //l'istante di arrivo
    public void update(Job job) {
        if(job.getJobClass() == -1) {
            //arrivo nel sistema
            onArrival(job);
        } else if (job.getJobClass() == 2) {
            //uscita dal sistema
            onExit(job);
        }
    }

    private void onArrival(Job job) {
        //quando arriva un job lo salvo nella lista
        list.add(job);
    }

    //all'uscita del job dal sistema utilizzo due job: uno che viene ricevuto tramite argomento e l'altro si trova
    //nella lista list. questo perchè il primo job citato possiede il corretto istante di uscita mentre il secondo
    //possiede l'istante di arrivo nel sistema. utilzzo l'id del job per ottenere la coppia corretta
    private void onExit(Job job) {
        for (Job j: list) {
            if (j.getId() == job.getId()) {
                //il job ha completato
                //i due job sono differenti in java ma concettualmente stesso job
                double responseTime = job.getCompleteTime() - j.getArrivalTime();
                if (responseTime < 0.0) {
                    responseTime = 0.1;
                }
                responseTimeEstimator.addData(responseTime);
                OutputFileGenerator.getInstance().logRecordACS(responseTime);
                list.remove(j);
                return;
            }
        }
    }

    public void onRemove(long id){
        /*for (Job job: list) {
            if (id == job.getId()) {
                list.remove(job);
            }
        }*/
    }
    public void resetGlobalEstimator() {
        responseTimeEstimator = new Welford();
    }

    public double getResponseTimeMean() {
        return responseTimeEstimator.getMean();
    }
    public double getResponseTimeStandardDeviation() {return responseTimeEstimator.getStandardDeviation(); }
}
