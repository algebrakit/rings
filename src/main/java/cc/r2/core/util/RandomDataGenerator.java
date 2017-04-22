package cc.r2.core.util;


import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class RandomDataGenerator extends org.apache.commons.math3.random.RandomDataGenerator {
    public RandomDataGenerator(RandomGenerator rand) {
        super(rand);
    }

    @Override
    public int nextInt(int lower, int upper) throws NumberIsTooLargeException {
        if (lower == upper)
            return lower;
        return super.nextInt(lower, upper);
    }

    @Override
    public long nextLong(long lower, long upper) throws NumberIsTooLargeException {
        if (lower == upper)
            return lower;
        return super.nextLong(lower, upper);
    }
}
