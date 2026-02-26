package ie.ucd.comp47500.stackvalidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ExperimentRunner {
    private static final char[] OPEN = {'(', '[', '{'};
    private static final char[] CLOSE = {')', ']', '}'};
    private static final long SEED = 47500L;

    private final BracketValidator validator;
    private final Random random;

    public ExperimentRunner(BracketValidator validator) {
        this.validator = validator;
        this.random = new Random(SEED);
    }

    public void runDefaultExperiments() {
        int[] sizes = {1_000, 10_000, 100_000, 1_000_000};
        int[] depths = {8, 32, 128, 512};
        int runs = 50;
        runExperimentBlock("VALID-HEAVY DATASET", 0.00, sizes, depths, runs);
        runExperimentBlock("EARLY-FAIL DATASET", 0.10, sizes, depths, runs);
    }

    private void runExperimentBlock(
            String label,
            double invalidRate,
            int[] sizes,
            int[] depths,
            int runs
    ) {
        List<ExperimentPoint> sizePoints = new ArrayList<>();
        List<ExperimentPoint> depthPoints = new ArrayList<>();

        System.out.println();
        System.out.printf("=== %s (invalidRate=%.2f) ===%n", label, invalidRate);

        System.out.println("Experiment: runtime scaling with input size");
        System.out.println("columns: size, depthLimit, runs, avgMicros, validRate, avgMaxDepth");
        for (int size : sizes) {
            ExperimentStats stats = benchmark(size, 64, invalidRate, runs);
            printRow(size, 64, runs, stats);
            sizePoints.add(new ExperimentPoint(size, stats));
        }

        System.out.println();
        System.out.println("Experiment: runtime scaling with maximum nesting depth");
        System.out.println("columns: size, depthLimit, runs, avgMicros, validRate, avgMaxDepth");
        for (int depth : depths) {
            ExperimentStats stats = benchmark(200_000, depth, invalidRate, runs);
            printRow(200_000, depth, runs, stats);
            depthPoints.add(new ExperimentPoint(depth, stats));
        }

        printAsciiChart(
                String.format("Chart: avg runtime vs input size (invalidRate=%.2f)", invalidRate),
                "size",
                sizePoints
        );
        printAsciiChart(
                String.format("Chart: avg runtime vs depth limit (invalidRate=%.2f)", invalidRate),
                "depth",
                depthPoints
        );
    }

    private void printRow(int size, int depth, int runs, ExperimentStats stats) {
        System.out.printf(
                "%d,%d,%d,%.2f,%.2f,%.2f%n",
                size,
                depth,
                runs,
                stats.averageMicros,
                stats.validRate,
                stats.averageMaxDepth
        );
    }

    /**
     * Draws a simple text chart in terminal so results are easier to compare in reports/videos.
     */
    private void printAsciiChart(String title, String xLabel, List<ExperimentPoint> points) {
        if (points.isEmpty()) {
            return;
        }

        final int maxBarWidth = 40;
        double maxY = 0.0;
        for (ExperimentPoint point : points) {
            if (point.stats.averageMicros > maxY) {
                maxY = point.stats.averageMicros;
            }
        }
        if (maxY == 0.0) {
            maxY = 1.0;
        }

        System.out.println();
        System.out.println(title);
        for (ExperimentPoint point : points) {
            int barLength = (int) Math.round((point.stats.averageMicros / maxY) * maxBarWidth);
            if (barLength == 0 && point.stats.averageMicros > 0.0) {
                barLength = 1;
            }
            String bar = "#".repeat(barLength);
            System.out.printf(
                    "%s=%-8d | %-40s %.2f us%n",
                    xLabel,
                    point.x,
                    bar,
                    point.stats.averageMicros
            );
        }
    }

    private ExperimentStats benchmark(int length, int maxDepth, double invalidRate, int runs) {
        // Warm-up to reduce JIT distortion.
        for (int i = 0; i < 5; i++) {
            String warmup = generateBracketLikeInput(length / 5, maxDepth, invalidRate);
            validator.validate(warmup);
        }

        long totalNanos = 0L;
        int validCount = 0;
        long depthSum = 0;

        for (int i = 0; i < runs; i++) {
            String input = generateBracketLikeInput(length, maxDepth, invalidRate);
            long t1 = System.nanoTime();
            ValidationResult result = validator.validate(input);
            long t2 = System.nanoTime();

            totalNanos += (t2 - t1);
            if (result.valid()) {
                validCount++;
            }
            depthSum += result.maxDepth();
        }

        double avgMicros = (totalNanos / (double) runs) / 1_000.0;
        double validRate = validCount / (double) runs;
        double avgMaxDepth = depthSum / (double) runs;
        return new ExperimentStats(avgMicros, validRate, avgMaxDepth);
    }

    private String generateBracketLikeInput(int length, int maxDepth, double invalidRate) {
        StringBuilder sb = new StringBuilder(length);
        ArrayCharStack open = new ArrayCharStack(Math.max(16, maxDepth));

        while (sb.length() < length) {
            int remaining = length - sb.length();
            // We must leave enough slots to close all currently-open brackets.
            boolean mustCloseForBalance = !open.isEmpty() && open.size() == remaining;
            boolean forceCloseByDepth = !open.isEmpty() && open.size() >= maxDepth;
            // To open one more bracket now, we need at least one slot for this char and
            // (open.size() + 1) slots later to close all outstanding brackets.
            boolean canOpen = remaining >= open.size() + 2 && open.size() < maxDepth;

            boolean chooseClose = !open.isEmpty()
                    && (mustCloseForBalance || forceCloseByDepth || !canOpen || random.nextBoolean());

            if (chooseClose) {
                char opening = open.pop();
                char closing = matchingClosing(opening);
                if (random.nextDouble() < invalidRate) {
                    closing = randomWrongClosing(opening);
                }
                sb.append(closing);
            } else {
                char opening = OPEN[random.nextInt(OPEN.length)];
                open.push(opening);
                sb.append(opening);
            }
        }
        return sb.toString();
    }

    private char randomWrongClosing(char opening) {
        char expected = matchingClosing(opening);
        char c;
        do {
            c = CLOSE[random.nextInt(CLOSE.length)];
        } while (c == expected);
        return c;
    }

    private static char matchingClosing(char opening) {
        return switch (opening) {
            case '(' -> ')';
            case '[' -> ']';
            case '{' -> '}';
            default -> '?';
        };
    }

    private record ExperimentPoint(int x, ExperimentStats stats) {
    }

    private record ExperimentStats(double averageMicros, double validRate, double averageMaxDepth) {
    }
}
