package pmcsn.rngs;

public class Exponential {

    //ATTENZIONE: usare questo metodo con lo stream corretto:
    //0 per arrivi esterni nodo A
    //1 per tempi servizio nodo A classe 1
    //2 per tempi servizio nodo A classe 2
    //3 per tempi servizio nodo B
    //4 per tempi servizio nodo P
    public static double exponential(double m, Rngs r) {
        /* ---------------------------------------------------
         * generate an Exponential random variate, use m > 0.0
         * ---------------------------------------------------
         */
        return (-m * Math.log(1.0 - r.random()));
    }
}
