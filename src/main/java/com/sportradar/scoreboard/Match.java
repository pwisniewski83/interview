package com.sportradar.scoreboard;

/**
 * Represents a match between two teams, including their names and scores.
 * <p>
 * This class is implemented as a {@code record}, providing an immutable data structure
 * that holds the details of a match, including the names of the home and away teams
 * and their respective scores.
 *
 * @param homeTeam      the name of the home team; must not be {@code null}
 * @param awayTeam      the name of the away team; must not be {@code null}
 * @param homeTeamScore the score of the home team
 * @param awayTeamScore the score of the away team
 */
public record Match(String homeTeam,
                    String awayTeam,
                    int homeTeamScore,
                    int awayTeamScore) {

    /**
     * Returns a string representation of the match.
     * <p>
     * The format of the string is:
     * <pre>{@code homeTeam homeTeamScore - awayTeam awayTeamScore}</pre>
     * Example: "Poland 3 - Germany 2"
     *
     * @return a {@link String} representation of the match
     */
    @Override
    public String toString() {
        return homeTeam + " " + homeTeamScore + " - " + awayTeam + " " + awayTeamScore;
    }
}
