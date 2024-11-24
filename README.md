# Scoreboard Project

This project implements a scoreboard system for managing sports matches, including functionality for starting, updating,
finishing matches, and retrieving summaries of ongoing games.

## Features

- **Start a New Match**: Begin a new match between two teams.
- **Update Match Score**: Update the scores for both teams during a match.
- **Finish a Match**: End a match and finalize the results.
- **Get Match Summary**: Retrieve a list of ongoing matches sorted by score and start time.

## Requirements

- Java 23 or higher
- SLF4J for logging
- JUnit for testing (if running tests)

## How It Works

### Scoreboard

The `Scoreboard` class is the central component of the project, managing all matches.
It provides the following functionality:

1. **Start a New Match**
    - You can start a new match by providing names for the home and away teams. Both team names must be non-null.
    - The method will throw an `IllegalStateException` if a match between the two teams is already in progress.

2. **Update Match Score**
    - Update the score of an ongoing match by providing the home and away team names and the new scores for each team.
    - It throws an `IllegalStateException` if the match is not found in the scoreboard.

3. **Finish a Match**
    - Finish the ongoing match by providing the home and away team names.
    - The match will be removed from the scoreboard when finished, and if not found, an `IllegalStateException`
      will be thrown.

4. **Get Match Summary**
    - Retrieve a summary of ongoing matches, sorted first by the sum of the scores (highest score first) and then
      by the match start time (later matches first).
    - The list may be empty if no matches are ongoing.

### Thread Safety

The methods of the `Scoreboard` class are thread-safe, allowing them to be called concurrently from multiple threads.
Synchronization is handled internally to ensure thread safety during match updates.

### Other assumptions

1. Team names cannot be null, but they may be empty strings. Data validation should be handled at a higher level.
2. Negative scores are allowed. Data validation should be handled at a higher level.
3. It is possible for the home and away teams to have the same name.

## Usage

### Example Code

```java
Scoreboard scoreboard = new Scoreboard();

// Start a new match
scoreboard.startNewMatch("Poland", "Germany");

// Update the score
scoreboard.updateScore("Poland", "Germany", 1, 1);
scoreboard.updateScore("Poland", "Germany", 2, 1);

// Get the match summary
List<Match> summary = scoreboard.getSummary();
summary.forEach(System.out::println);

// Finish the match
scoreboard.finishMatch("Poland", "Germany");
```
### Building

```shell
./gradlew clean build
```

###  Running Tests

```shell
./gradlew test
```

### Generating Documentation
```shell
./gradlew javadoc
```