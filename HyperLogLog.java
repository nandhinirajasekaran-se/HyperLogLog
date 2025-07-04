import java.util.concurrent.ThreadLocalRandom;

public class HyperLogLog {
    private final int b; // precision bits (4-16)
    private final int m; // number of registers (2^b)
    private final byte[] registers;
    private final double alphaMM; // alpha * m^2

    // Alpha constants for bias correction
    private static final double[] ALPHA = {
            0.673, 0.697, 0.709, 0.715, 0.718, 0.719, 0.720, 0.721, 0.721, 0.721
    };

    public HyperLogLog(int b) {
        if (b < 4 || b > 16) {
            throw new IllegalArgumentException("Precision must be between 4 and 16");
        }

        this.b = b;
        this.m = 1 << b;
        this.registers = new byte[m];
        this.alphaMM = getAlphaMM();
    }

    private double getAlphaMM() {
        if (b <= 6) return ALPHA[b - 4] * m * m;
        return (0.7213 / (1 + 1.079 / m)) * m * m;
    }

    // High-quality 64-bit hash with proper mixing
    private long hash(int value) {
        long h = (long)value;
        h = (h ^ (h >>> 33)) * 0xff51afd7ed558ccdL;
        h = (h ^ (h >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return h ^ (h >>> 33);
    }

    public void add(int value) {
        long hashed = hash(value);

        // Get bucket from first b bits (as unsigned)
        int bucket = (int)((hashed >>> (64 - b)) & (m - 1));

        // Get remaining bits and count leading zeros
        long remaining = (hashed << b) | (1L << (b - 1));
        int leadingZeros = Long.numberOfLeadingZeros(remaining) + 1;

        if (leadingZeros > registers[bucket]) {
            registers[bucket] = (byte)leadingZeros;
        }
    }

    public long count() {
        double sum = 0;
        int zeroRegisters = 0;

        for (byte r : registers) {
            sum += 1.0 / (1L << r);
            if (r == 0) zeroRegisters++;
        }

        double estimate = alphaMM / sum;

        // Small range correction
        if (estimate <= 2.5 * m) {
            if (zeroRegisters > 0) {
                estimate = m * Math.log(m / (double)zeroRegisters);
            }
        }
        // Large range correction
        else if (estimate > Integer.MAX_VALUE / 30.0) {
            estimate = -Integer.MAX_VALUE * Math.log(1.0 - estimate / Integer.MAX_VALUE);
        }

        return Math.round(estimate);
    }

    public static void main(String[] args) {
        int b = 14; // 4096 registers
        HyperLogLog hll = new HyperLogLog(b);

        int actualDistinct = 2000;
        int totalElements = 4000;

        // Generate exactly 2000 distinct values
        int[] distinctValues = new int[actualDistinct];
        for (int i = 0; i < actualDistinct; i++) {
            distinctValues[i] = i;
        }

        // Shuffle and add duplicates
        shuffleArray(distinctValues);
        for (int i = 0; i < totalElements; i++) {
            hll.add(distinctValues[i % actualDistinct]);
        }

        long estimated = hll.count();
        System.out.println("HyperLogLog with " + (1 << b) + " registers");
        System.out.println("Estimated distinct elements: " + estimated);
        System.out.println("Actual distinct elements: " + actualDistinct);

        double error = Math.abs(estimated - actualDistinct) / (double)actualDistinct * 100;
        System.out.printf("Error: %.2f%%\n", error);
    }

    // Fisher-Yates shuffle for better test data
    private static void shuffleArray(int[] array) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
}