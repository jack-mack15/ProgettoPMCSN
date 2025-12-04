package pmcsn.estimators;

import static java.lang.System.out;

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

    public void resetCopyEstimator() {
        this.numCopy = 1;
        this.startTime = 0.0;
        this.area = 0.0;
        this.finishTime = 0.0;
        this.lastUpdate = 0.0;
    }

    public void onCreation(double now) {
        updateAreas(now);
        numCopy++;
        //out.println("per ora ci sono queste copie parallele "+numCopy);
    }

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
