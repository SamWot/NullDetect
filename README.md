# NullDetect
Static analyzer for detecting redundant null-comparisons in Java class-files. For this NullDetect parses compiled class-file and aplies data-flow analysis.

## Prerequisites
- JDK 1.8
- Maven:
  - [ASM 5.2](http://asm.ow2.org/) to parse class-files and as a base for data-flow analysis
  - JUnit 4.12 for unit testing

## Build
```shell session
mvn package
```

## Usage
Analyzing several class-files:
```shell session
java -jar <jar-with-dependencies> <class-filenames>...
```
