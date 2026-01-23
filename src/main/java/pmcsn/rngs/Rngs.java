package pmcsn.rngs;


/* -------------------------------------------------------------------------
 * This is an Java library for multi-stream random number generation.
 * The use of this library is recommended as a replacement for the Java
 * class Random, particularly in simulation applications where the
 * statistical 'goodness' of the random number generator is important.
 * The library supplies 256 streams of random numbers; use
 * selectStream(s) to switch between streams indexed s = 0,1,...,255.
 *
 * The streams must be initialized.  The recommended way to do this is by
 * using the function plantSeeds(x) with the value of x used to initialize
 * the default stream and all other streams initialized automatically with
 * values dependent on the value of x.  The following convention is used
 * to initialize the default stream:
 *    if x > 0 then x is the state
 *    if x < 0 then the state is obtained from the system clock
 *    if x = 0 then the state is to be supplied interactively.
 *
 * The generator used in this library is a so-called 'Lehmer random number
 * generator' which returns a pseudo-random number uniformly distributed
 * 0.0 and 1.0.  The period is (m - 1) where m = 2,147,483,647 and the
 * smallest and largest possible values are (1 / m) and 1 - (1 / m)
 * respectively.  For more details see:
 *
 *       "Random Number Generators: Good Ones Are Hard To Find"
 *                   Steve Park and Keith Miller
 *              Communications of the ACM, October 1988
 *
 * Name            : Rngs.java  (Random Number Generation - Multiple Streams)
 * Authors         : Steve Park & Dave Geyer
 * Translated by   : Jun Wang & Richard Dutton
 * Language        : Java
 * Latest Revision : 6-10-04
 * -------------------------------------------------------------------------
 */

public class Rngs {

    //modulo m
    private final long m = 2147483647;
    //moltiplier a
    private final long a = 48271;
    //default per il seed 0
    private final long DEFAULT = 123456789L;
    //numero di stream massimi
    private final int STREAMS = 256;
    //jump multiplier j
    private final long j = 22925;


    //array di sstream
    private long[] streams;

    //array di seed iniziali degli stream
    private  long[] seeds;

    //cursor degli stream
    private int  stream = 0;
    int initialize = 0;

    //indica l'indice del seed iniziale della run. Ad ogni run viene aggiornato
    private int runCursor;





    public Rngs (long x) {
        streams = new long[STREAMS];
        seeds = new long[STREAMS];
        runCursor = 0;
        plantSeeds(x);
    }

    //funzione che ritorna un numero reale in modo uniformemente distribuito tra 0.0 e 1.0
    public double random() {

        long Q = m / a;
        long R = m % a;
        long t;

        t = a * (streams[stream] % Q) - R * (streams[stream] / Q);
        if (t > 0)
            streams[stream] = t;
        else
            streams[stream] = t + m;
        return ((double) streams[stream] / m);
    }

    //funzione che setta tutti i seed degli stream a partire dal valore specificato x
    //invocata SOLO dal costruttore per evitare problemi
    private void plantSeeds(long x) {

        long Q = m / j;
        long R = m % j;

        putSeed(x);

        for (int j = 1; j < STREAMS; j++) {
            x = this.j * (streams[j - 1] % Q) - R * (streams[j - 1] / Q);
            if (x > 0) {
                streams[j] = x;
                seeds[j] = x;
            }
            else {
                streams[j] = x + m;
                seeds[j] = x + m;
            }
        }
    }

    //funzione che imposta il primo seed, elemento 0-esimo
    //se x è troppo grande si prende il x mod m.
    //se x è negativo o pari a 0, utilizzo il default
    private void putSeed(long x) {

        if (x > 0)
            x = x % m;
        if (x <= 0) {
            x = DEFAULT;
        }
        streams[0] = x;
        seeds[0] = x;
    }

    //ritorna l'indice dell'array iniziale della run
    public int getRunCursor() {
        return runCursor;
    }

    public void setRunCursor() {
        runCursor += 6;
    }

    //attento a come lo usi eh
    public void setRunCursorByIndex(int index) {
        runCursor = index;
    }

    //funzione che permette di selezionare uno stream. La successiva invocazione di random
    //utilizzerà lo stream selezionato con selectStream().
    public void selectStream(String node) {
        int index = getIndex(node);
        stream = (index + runCursor) % STREAMS;
    }

    public long[] getSeeds() {
        long[] currentSeeds = new long[6];

        currentSeeds[0] = seeds[runCursor];
        currentSeeds[1] = seeds[runCursor+1];
        currentSeeds[2] = seeds[runCursor+2];
        currentSeeds[3] = seeds[runCursor+3];
        currentSeeds[4] = seeds[runCursor+4];
        currentSeeds[5] = seeds[runCursor+5];

        return currentSeeds;
    }

    public int getIndex(String node) {
        switch (node) {
            case "A_1":
                return 1;
            case "A_2":
                return 2;
            case "A_3":
                return 3;
            case "B":
                return 4;
            case "P":
                return 5;
            case "EXT":
                return 0;
            default:
                return -1;
        }
    }
}