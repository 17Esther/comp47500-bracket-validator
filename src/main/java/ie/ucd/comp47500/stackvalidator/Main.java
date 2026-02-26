package ie.ucd.comp47500.stackvalidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        BracketValidator validator = new BracketValidator();

        if (args.length == 0) {
            printUsage();
            runQuickDemo(validator);
            return;
        }

        switch (args[0]) {
            case "validate" -> runValidate(validator, args);
            case "file" -> runFile(validator, args);
            case "experiment" -> runExperiments(validator);
            default -> {
                printUsage();
                System.out.println("Unknown command: " + args[0]);
            }
        }
    }

    private static void runValidate(BracketValidator validator, String[] args) {
        if (args.length < 2) {
            System.out.println("Missing expression after 'validate'");
            return;
        }
        ValidationResult result = validator.validate(args[1]);
        printResult(args[1], result);
    }

    private static void runFile(BracketValidator validator, String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Missing file path after 'file'");
            return;
        }
        Path path = Path.of(args[1]);
        String content = Files.readString(path);
        ValidationResult result = validator.validate(content, true);
        printResult(path.toString(), result);
    }

    private static void runExperiments(BracketValidator validator) {
        ExperimentRunner runner = new ExperimentRunner(validator);
        runner.runDefaultExperiments();
    }


    private static void runQuickDemo(BracketValidator validator) {
        String[] examples = {"([]){}", "([)]", "(()", "())", "{ [ ( ) ] }"};
        for (String example : examples) {
            ValidationResult result = validator.validate(example, true);
            printResult(example, result);
        }
    }

    private static void printResult(String label, ValidationResult result) {
        if (result.valid()) {
            System.out.printf(
                    "Input: %s%nVALID | maxDepth=%d | processed=%d%n%n",
                    label, result.maxDepth(), result.processedLength()
            );
        } else {
            System.out.printf(
                    "Input: %s%nINVALID | index=%d | reason=%s | maxDepth=%d | processed=%d%n%n",
                    label, result.errorIndex(), result.message(), result.maxDepth(), result.processedLength()
            );
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java -cp out ie.ucd.comp47500.stackvalidator.Main validate \"([{}])\"");
        System.out.println("  java -cp out ie.ucd.comp47500.stackvalidator.Main file ./input.txt");
        System.out.println("  java -cp out ie.ucd.comp47500.stackvalidator.Main experiment");
        System.out.println();
    }
}
