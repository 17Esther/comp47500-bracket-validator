# COMP47500 Assignment 1 — Stack ADT

## Project: Stack-Based Bracket Validator + Experiments
This project implements a **Stack ADT** (array-backed) and uses it to solve a real-world validation problem: checking whether brackets in structured text are properly balanced and nested.

Supported brackets: `()`, `[]`, `{}`  
Non-bracket characters can be ignored (editor-like behavior).

## Repository Structure
- `src/main/java/ie/ucd/comp47500/stackvalidator/`
  - `ArrayCharStack.java`: Stack ADT implementation (push/pop/peek, dynamic resizing)
  - `BracketValidator.java`: validation algorithm (single pass, early error detection)
  - `ValidationResult.java`: immutable result record (valid/invalid + metrics)
  - `ExperimentRunner.java`: benchmark experiments + ASCII bar charts
  - `Main.java`: CLI entry point
- `docs/uml.puml`: UML class diagram (PlantUML)

## Build & Run
### Requirements
- **JDK 17+** recommended (this project uses Java `record`).

### Compile
From the repo root:

```bash
mkdir -p out
javac -d out src/main/java/ie/ucd/comp47500/stackvalidator/*.java
```

### Run demo (no arguments)
```bash
java -cp out ie.ucd.comp47500.stackvalidator.Main
```

### Validate a string
```bash
java -cp out ie.ucd.comp47500.stackvalidator.Main validate "([{}])"
```

### Validate a file
```bash
java -cp out ie.ucd.comp47500.stackvalidator.Main file ./input.txt
```

### Run experiments
```bash
java -cp out ie.ucd.comp47500.stackvalidator.Main experiment
```

## Experiments Output (How to Read It)
The experiment mode prints CSV-like rows:

`size, depthLimit, runs, avgMicros, validRate, avgMaxDepth`

- **size**: generated input length (characters)
- **depthLimit**: maximum allowed nesting depth during generation
- **runs**: repeats per setting (averaged)
- **avgMicros**: average runtime per validation (microseconds)
- **validRate**: fraction of valid inputs in the runs
- **avgMaxDepth**: average of maximum stack depth reached (nesting)

It also prints ASCII bar charts to quickly visualize runtime differences.

## Notes (Practical Efficiency)
Efficiency tuning decisions used in this implementation:
- **Primitive stack (`char[]`)** avoids boxing overhead compared to `Stack<Character>`.
- **Dynamic resizing (doubling)** provides amortized `O(1)` push.
- **Early exit on mismatch** reduces work for invalid inputs.

