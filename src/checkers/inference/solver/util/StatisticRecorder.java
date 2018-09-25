package checkers.inference.solver.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;

/**
 * Recorder for statistics.
 *
 * @author jianchu
 *
 */
public class StatisticRecorder {
    // Use atomic integer when back ends run in parallel.
    public final static AtomicInteger satSerializationTime = new AtomicInteger(0);
    public final static AtomicInteger satSolvingTime = new AtomicInteger(0);
    // statistics are sorted alphabetically by key name
    private final static Map<String, Long> statistic = new TreeMap<>();

    public static synchronized void recordSingleSerializationTime(long value) {
        satSerializationTime.addAndGet((int) value);
    }

    public static synchronized void recordSingleSolvingTime(long value) {
        satSolvingTime.addAndGet((int) value);
    }

    /**
     * Adds the given value to the statistics for the given key. If an existing value exists for the
     * given key, this method stores the sum of the new value and the existing value into the key.
     *
     * @param key
     *            a statistic key. The key is treated case-insensitive: it will always be considered
     *            in terms of its lower case equivalent.
     * @param value
     *            a value
     */
    public static void record(String key, long value) {
        synchronized (statistic) {
            // always use the lower-case version of the given key
            key = key.toLowerCase();

            if (statistic.get(key) == null || key.contentEquals("logiql_predicate_size")) {
                // LogiQL predicate size are fixed for same underlining type
                // system.
                statistic.put(key, value);
            } else {
                long oldValue = statistic.get(key);
                statistic.put(key, value + oldValue);
            }
        }
    }

    /**
     * Adds the given value to the statistics for the given key.
     *
     * This is a convenience method to eliminate the need to cast the value to long at call sites.
     *
     * @see #record(String, long)
     *
     * @param key
     *            a statistic key
     * @param value
     *            a value
     */
    public static void record(String key, int value) {
        record(key, (long) value);
    }

    /**
     * Adds a count of each kind of slot to the statistics.
     *
     * @param slots
     */
    public static void recordSlotsStatistics(final Collection<Slot> slots) {
        // Record total number of slots
        record("total_slots", slots.size());

        // Record slot counts
        Map<Class<? extends Slot>, Long> slotCounts = new LinkedHashMap<>();
        long totalConstantSlots = 0;
        long totalVariableSlots = 0;

        for (Slot slot : slots) {
            if (slot instanceof ConstantSlot) {
                totalConstantSlots++;
            } else if (slot instanceof VariableSlot) {
                totalVariableSlots++;
            }

            Class<? extends Slot> slotClass = slot.getClass();

            if (!slotCounts.containsKey(slotClass)) {
                slotCounts.put(slotClass, 1L);
            } else {
                slotCounts.put(slotClass, slotCounts.get(slotClass) + 1L);
            }
        }

        record("total_constant_slots", totalConstantSlots);
        record("total_variable_slots", totalVariableSlots);

        for (Entry<Class<? extends Slot>, Long> entry : slotCounts.entrySet()) {
            record(entry.getKey().getSimpleName(), entry.getValue());
        }
    }

    /**
     * Adds a count of each kind of constraint to the statistics.
     *
     * @param constraints
     */
    public static void recordConstraintsStatistics(final Collection<Constraint> constraints) {
        // Record total number of constraints
        record("total_constraints", constraints.size());

        // Record constraint counts
        Map<Class<? extends Constraint>, Long> constraintCounts = new LinkedHashMap<>();

        for (Constraint constraint : constraints) {
            Class<? extends Constraint> constraintClass = constraint.getClass();

            if (!constraintCounts.containsKey(constraintClass)) {
                constraintCounts.put(constraintClass, 1L);
            } else {
                constraintCounts.put(constraintClass, constraintCounts.get(constraintClass) + 1L);
            }
        }

        for (Entry<Class<? extends Constraint>, Long> entry : constraintCounts.entrySet()) {
            record(entry.getKey().getSimpleName(), entry.getValue());
        }
    }

    /**
     * Returns an immutable map of the collected statistics.
     *
     * @return the immutable map.
     */
    public static Map<String, Long> getStatistic() {
        return Collections.unmodifiableMap(statistic);
    }
}
