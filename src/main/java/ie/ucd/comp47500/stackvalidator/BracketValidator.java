package ie.ucd.comp47500.stackvalidator;

public final class BracketValidator {
    public ValidationResult validate(String input, boolean ignoreNonBracketChars) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }

        ArrayCharStack stack = new ArrayCharStack(Math.max(16, input.length() / 4));
        int maxDepth = 0;
        int processed = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (isOpeningBracket(c)) {
                stack.push(c);
                processed++;
                if (stack.size() > maxDepth) {
                    maxDepth = stack.size();
                }
                continue;
            }

            if (isClosingBracket(c)) {
                processed++;
                if (stack.isEmpty()) {
                    return ValidationResult.invalid(i, "Closing bracket without opening bracket: " + c, maxDepth, processed);
                }
                char opening = stack.pop();
                if (!isMatchingPair(opening, c)) {
                    return ValidationResult.invalid(
                            i,
                            "Mismatched bracket pair: expected " + expectedClosing(opening) + " but got " + c,
                            maxDepth,
                            processed
                    );
                }
                continue;
            }

            if (!ignoreNonBracketChars) {
                return ValidationResult.invalid(i, "Unsupported character encountered: '" + c + "'", maxDepth, processed);
            }
        }

        if (!stack.isEmpty()) {
            return ValidationResult.invalid(input.length(), "Input ended before closing all opening brackets", maxDepth, processed);
        }
        return ValidationResult.valid(maxDepth, processed);
    }

    public ValidationResult validate(String input) {
        return validate(input, true);
    }

    private static boolean isOpeningBracket(char c) {
        return c == '(' || c == '[' || c == '{';
    }

    private static boolean isClosingBracket(char c) {
        return c == ')' || c == ']' || c == '}';
    }

    private static boolean isMatchingPair(char opening, char closing) {
        return (opening == '(' && closing == ')')
                || (opening == '[' && closing == ']')
                || (opening == '{' && closing == '}');
    }

    private static char expectedClosing(char opening) {
        return switch (opening) {
            case '(' -> ')';
            case '[' -> ']';
            case '{' -> '}';
            default -> '?';
        };
    }
}
