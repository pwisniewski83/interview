package com.sportradar.scoreboard;

import org.junit.jupiter.api.BeforeEach;
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
    public void shouldStartNewMatch() {
        Match match = scoreboard.startNewMatch("France", "Germany");

        assertEquals(new Match("France", "Germany", 0, 0), match);
    }

    @Test
    public void shouldThrowExceptionWhenStartingMatchWithNullTeamNames() {
        assertThrows(NullPointerException.class,
                () -> scoreboard.startNewMatch(null, "Germany"),
                "Cannot start match with a null home team name");

        assertThrows(NullPointerException.class,
                () -> scoreboard.startNewMatch("France", null),
                "Cannot start match with a null away team name");
    }

    @Test
    public void shouldThrowExceptionWhenStartingTheSameMatchTwice() {
        scoreboard.startNewMatch("Croatia", "Poland");

        assertThrows(IllegalStateException.class,
                () -> scoreboard.startNewMatch("Croatia", "Poland"),
                "Starting the same match twice should not be possible");
    }

    @Test
    public void shouldStartTwoDifferentMatches() {
        Match match1 = scoreboard.startNewMatch("Croatia", "Poland");
        Match match2 = scoreboard.startNewMatch("", "Italy");

        assertEquals(new Match("Croatia", "Poland", 0, 0), match1);
        assertEquals(new Match("", "Italy", 0, 0), match2);
    }

    @Test
    public void shouldUpdateScoreOfOngoingMatch() {
        scoreboard.startNewMatch("Croatia", "Poland");
        Match match = scoreboard.updateScore("Croatia", "Poland", 1, 2);

        assertEquals(new Match("Croatia", "Poland", 1, 2), match);
    }

    @Test
    public void shouldAllowUpdatingScoreWithSameValues() {
        scoreboard.startNewMatch("Croatia", "Poland");
        scoreboard.updateScore("Croatia", "Poland", 1, 2);
        scoreboard.updateScore("Croatia", "Poland", 1, 2);
    }

    @Test
    public void shouldThrowExceptionWhenUpdatingScoreForNonExistentMatch() {
        assertThrows(IllegalStateException.class,
                () -> scoreboard.updateScore("Croatia", "Poland", 0, 3));
    }

    @Test
    public void shouldThrowExceptionWhenUpdatingScoreWithNullTeamNames() {
        assertThrows(NullPointerException.class,
                () -> scoreboard.updateScore(null, "Poland", 1, 2));

        assertThrows(NullPointerException.class,
                () -> scoreboard.updateScore("Hungary", null, 1, 2));
    }

    @Test
    public void shouldAllowFinishingMatchAfterUpdateWithCorrectResult() {
        scoreboard.startNewMatch("France", "Spain");
        scoreboard.updateScore("France", "Spain", 1, 2);
        Match match = scoreboard.finishMatch("France", "Spain");

        Match expected = new Match("France", "Spain", 1, 2);
        assertEquals(expected, match);
    }

    @Test
    public void shouldThrowExceptionWhenFinishingNonExistentMatch() {
        assertThrows(IllegalStateException.class,
                () -> scoreboard.finishMatch("Austria", "Slovakia"));
    }

    @Test
    public void shouldThrowExceptionWhenFinishingWithNullTeamNames() {
        assertThrows(NullPointerException.class,
                () -> scoreboard.finishMatch(null, "Poland"));

        assertThrows(NullPointerException.class,
                () -> scoreboard.finishMatch("", null));
    }

    @Test
    public void shouldReturnEmptyNonNullSummary() {
        List<Match> matches = scoreboard.getSummary();

        assertThat(matches).isEmpty();
    }

    @Test
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
