package mutation.g3;

/**
 *
 * @author group3
 */
public class LogProbability {

    public static final double LOG_ZERO_PROB = Double.NEGATIVE_INFINITY;

    public static double p2log(double p) {
        return Math.log(p);
    }

    public static double logPAdd(double p1, double p2) {
        if (p1 == LOG_ZERO_PROB) {
            return p2;
        } else if (p2 == LOG_ZERO_PROB) {
            return p1;
        } else {
            return p1 + Math.log1p(Math.exp(p2 - p1));
        }
    }

    public static double logPMult(double p1, double p2) {
        if (p1 == LOG_ZERO_PROB || p2 == LOG_ZERO_PROB) {
            return LOG_ZERO_PROB;
        }
        return p1 + p2;
    }
}
