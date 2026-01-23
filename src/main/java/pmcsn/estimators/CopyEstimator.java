package pmcsn.estimators;

//classe che implementa la logica per aggiornare e mantenere metrica numero di copie medie
public class CopyEstimator {

    private int numCopy;
    private double startTime;
    private double lastUpdate;

    //mi serve per calcolare l'integrale di N(t) e poi per ottenere la media
    private double area;

    private double finishTime;

    private static final CopyEstimator istance = new CopyEstimator();

    public static CopyEstimator getInstance() { return istance;}
    private CopyEstimator() {
        numCopy = 1;
        startTime = 0.0;
        area = 0.0;
        lastUpdate = 0.0;
    }

    //reset utile per fine batch means
    public void hardReset() {
        this.numCopy = 1;
        this.startTime = 0.0;
        this.area = 0.0;
        this.finishTime = 0.0;
        this.lastUpdate = 0.0;
    }

    //reset utile per batch means
    public void resetForBatch(int currNum, double newTime) {
        this.numCopy = currNum;
        this.startTime = newTime;
        this.area = 0.0;
        this.finishTime = 0.0;
        this.lastUpdate = newTime;
    }

    //aggiorna metriche quando arrriiva creation
    public void onCreation(double now) {
        updateAreas(now);
        numCopy++;
    }

    // aggiorna metriche quando arriva DESTROY
    public void onDestroy(double now) {
        updateAreas(now);
        numCopy--;
    }

    private void updateAreas(double now) {
        double delta = now - lastUpdate;
        if (delta > 0.0) {
            area += numCopy * delta;
        }
        lastUpdate = now;
    }

    public double getNumCopyMean() {
        updateAreas(finishTime);
        double observationTime = finishTime - startTime;
        return area / observationTime;
    }

    public void setFinishTime(double time) {
        this.finishTime = time;
    }
}
