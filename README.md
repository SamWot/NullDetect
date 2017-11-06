# NullDetect
Static analyzer for detecting redundant null-comparisons in Java class-files. For this NullDetect parses compiled class-file and aplies data-flow analysis.

## Prerequisites
- JDK 1.8
- Maven:
  - [ASM 5.2](http://asm.ow2.org/) to parse class-files and as a base for data-flow analysis
  - JUnit 4.12 for unit testing
  - TestFX 4.0.8 (Core and JUnit) for GUI testing

## Build
```shell session
mvn package
```

## Usage
Run GUI with:
```shell session
java -jar <jar-with-dependencies>
```
To analyze select directory with class-files and press Start button. You can cancel background analysis by pressing Cancel button or selecting new directory with help of Brwose button.

## ToDo:
- GUI testing
- Improve documentation
- Migrate to ASM 6.0
- Migrate to JUnit 5 and use dynamic tests for individual test resources
- Separate CSS from GUI
- Improve analysis
- Simplify AnalyzerTask (AnalyzerTask could simply run AnalyzerJobs. Individual AnalyzerJobs could write their results directly into observable concurrent map)
- Add automatic compilation of test resources
