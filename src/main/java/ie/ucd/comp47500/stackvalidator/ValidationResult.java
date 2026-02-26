package ie.ucd.comp47500.stackvalidator;

public record ValidationResult(
        boolean valid,
        int errorIndex,
        String message,
        int maxDepth,
        int processedLength
) {
    public static ValidationResult valid(int maxDepth, int processedLength) {
        return new ValidationResult(true, -1, "Expression is balanced", maxDepth, processedLength);
    }

    public static ValidationResult invalid(int errorIndex, String message, int maxDepth, int processedLength) {
        return new ValidationResult(false, errorIndex, message, maxDepth, processedLength);
    }
}
