package com.sportradar.scoreboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScoreboardTest {

    private Scoreboard scoreboard;

    @BeforeEach
    public void setup() {
        scoreboard = new Scoreboard();
    }

    @Test
    @DisplayName("Should allow to start a new match with zero starting score")
    public void shouldStartNewMatch() {
        Match match = scoreboard.startNewMatch("France", "Germany");

        assertEquals(new Match("France", "Germany", 0, 0), match);
    }

    @Test
    @DisplayName("Should throw an exception when starting the match with null team name")
    public void shouldThrowExceptionWhenStartingMatchWithNullTeamNames() {
        assertThrows(NullPointerException.class,
                () -> scoreboard.startNewMatch(null, "Germany"),
                "Cannot start match with a null home team name");

        assertThrows(NullPointerException.class,
                () -> scoreboard.startNewMatch("France", null),
                "Cannot start match with a null away team name");
    }

    @Test
    @DisplayName("Should throw an exception when starting the same match twice")
    public void shouldThrowExceptionWhenStartingTheSameMatchTwice() {
        scoreboard.startNewMatch("Croatia", "Poland");

        assertThrows(IllegalStateException.class,
                () -> scoreboard.startNewMatch("Croatia", "Poland"),
                "Starting the same match twice should not be possible");
    }

    @Test
    @DisplayName("Should allow to start two different matches")
    public void shouldStartTwoDifferentMatches() {
        Match match1 = scoreboard.startNewMatch("Croatia", "Poland");
        Match match2 = scoreboard.startNewMatch("", "Italy");

        assertEquals(new Match("Croatia", "Poland", 0, 0), match1);
        assertEquals(new Match("", "Italy", 0, 0), match2);
    }

    @Test
    @DisplayName("Should allow to update the score of an ongoing match")
    public void shouldUpdateScoreOfOngoingMatch() {
        scoreboard.startNewMatch("Croatia", "Poland");
        Match match = scoreboard.updateScore("Croatia", "Poland", 1, 2);

        assertEquals(new Match("Croatia", "Poland", 1, 2), match);
    }

    @Test
    @DisplayName("Should throw an exception when updating the score for a non-existing match")
    public void shouldThrowExceptionWhenUpdatingScoreForNonExistentMatch() {
        assertThrows(IllegalStateException.class,
                () -> scoreboard.updateScore("Croatia", "Poland", 0, 3));
    }

    @Test
    @DisplayName("Should throw an exception when updating the score with null team name")
    public void shouldThrowExceptionWhenUpdatingScoreWithNullTeamNames() {
        assertThrows(NullPointerException.class,
                () -> scoreboard.updateScore(null, "Poland", 1, 2));

        assertThrows(NullPointerException.class,
                () -> scoreboard.updateScore("Hungary", null, 1, 2));
    }

    @Test
    @DisplayName("Should allow the match to end with the correct score")
    public void shouldAllowMatchToEndWithCorrectScore() {
        scoreboard.startNewMatch("France", "Spain");
        scoreboard.updateScore("France", "Spain", 1, 2);
        Match match = scoreboard.finishMatch("France", "Spain");

        Match expected = new Match("France", "Spain", 1, 2);
        assertEquals(expected, match);
    }

    @Test
    @DisplayName("Should throw an exception when ending non-existent match")
    public void shouldThrowExceptionWhenFinishingNonExistentMatch() {
        assertThrows(IllegalStateException.class,
                () -> scoreboard.finishMatch("Austria", "Slovakia"));
    }

    @Test
    @DisplayName("Should throw an exception when ending match with null team names")
    public void shouldThrowExceptionWhenFinishingWithNullTeamNames() {
        assertThrows(NullPointerException.class,
                () -> scoreboard.finishMatch(null, "Poland"));

        assertThrows(NullPointerException.class,
                () -> scoreboard.finishMatch("", null));
    }

    @Test
    @DisplayName("Should return empty (non-null) summary when no match is played")
    public void shouldReturnEmptyNonNullSummary() {
        List<Match> matches = scoreboard.getSummary();

        assertThat(matches).isEmpty();
    }

    @Test
    @DisplayName("Should return summary of ongoing matches in correct order")
    public void shouldReturnSummaryInCorrectOrder() {
        scoreboard.startNewMatch("Mexico", "Canada");
        scoreboard.updateScore("Mexico", "Canada", 0, 2);
        scoreboard.startNewMatch("Spain", "Brazil");
        scoreboard.startNewMatch("Germany", "France");
        scoreboard.updateScore("Spain", "Brazil", 1, 2);
        scoreboard.startNewMatch("Poland", "Hungary");
        scoreboard.updateScore("Poland", "Hungary", 1, 1);
        scoreboard.updateScore("Germany", "France", 1, 1);
        scoreboard.updateScore("Mexico", "Canada", 0, 5);
        scoreboard.startNewMatch("Uruguay", "Italy");
        scoreboard.updateScore("Spain", "Brazil", 4, 2);
        scoreboard.updateScore("Uruguay", "Italy", 2, 4);
        scoreboard.finishMatch("Poland", "Hungary");
        scoreboard.updateScore("Germany", "France", 1, 2);
        scoreboard.startNewMatch("Argentina", "Australia");
        scoreboard.updateScore("Uruguay", "Italy", 6, 6);
        scoreboard.updateScore("Germany", "France", 2, 2);
        scoreboard.updateScore("Argentina", "Australia", 2, 0);
        scoreboard.startNewMatch("Portugal", "");
        scoreboard.updateScore("Spain", "Brazil", 10, 2);
        scoreboard.updateScore("Argentina", "Australia", 3, 1);
        scoreboard.finishMatch("Portugal", "");

        List<Match> matches = scoreboard.getSummary();
        assertThat(matches).isEqualTo(List.of(
                new Match("Uruguay", "Italy", 6, 6),
                new Match("Spain", "Brazil", 10, 2),
                new Match("Mexico", "Canada", 0, 5),
                new Match("Argentina", "Australia", 3, 1),
                new Match("Germany", "France", 2, 2)
        ));
    }

}
