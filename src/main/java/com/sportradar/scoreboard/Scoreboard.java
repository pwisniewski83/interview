package com.sportradar.scoreboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A thread-safe scoreboard for managing matches and their scores.
 * <p>
 * The {@code Scoreboard} class provides functionality to start, update, finish matches,
 * and retrieve a summary of ongoing matches. Matches are identified by a unique combination
 * of home and away team names.
 * <p>
 * This class ensures thread safety for all operations, making it suitable for use in multithreaded environments.
 */
public class Scoreboard {

    private static final Logger LOG = LoggerFactory.getLogger(Scoreboard.class);

    private static final int INITIAL_SCORE = 0;

    /**
     * A {@link Comparator} used to compare {@link Match} objects based on the total score
     * of both the home and away teams.
     */
    private static final Comparator<Match> MATCH_COMPARATOR = Comparator.comparing(m ->
            m.homeTeamScore() + m.awayTeamScore());

    /**
     * A map that stores the ongoing matches, with the match key being a
     * combination of the home and away team names.
     * <p>
     * The map is implemented as a {@link LinkedHashMap} to maintain the insertion order
     * of the matches, ensuring that the order in which they were added is preserved.
     * <p>
     * The {@code matches} map is also used as a locking object to synchronize access to the matches during operations,
     * ensuring that match-related operations are safely performed in a multithreaded environment.
     */
    private final Map<MatchKey, Match> matches = new LinkedHashMap<>();

    /**
     * Represents a unique key used to identify a match in the scoreboard.
     *
     * @param homeTeam the name of the home team; must not be {@code null}
     * @param awayTeam the name of the away team; must not be {@code null}
     */
    record MatchKey(String homeTeam,
                    String awayTeam) {
    }

    /**
     * Constructs a new, empty {@code Scoreboard} instance.
     */
    public Scoreboard() {
    }

    /**
     * Starts a new match between the specified home and away teams.
     * <p>
     * This method creates a new match instance with the provided team names,
     * adds the match to the scoreboard, and returns the created match.
     * Both {@code homeTeam} and {@code awayTeam} must not be {@code null}.
     * Additionally, a match between the same teams cannot already be in progress.
     * <p>
     * This method is thread-safe and can be safely called from multiple threads simultaneously.
     *
     * @param homeTeam the name of the home team; must not be {@code null}
     * @param awayTeam the name of the away team; must not be {@code null}
     * @return the newly created {@link Match} object representing the match
     * @throws NullPointerException  if either {@code homeTeam} or {@code awayTeam} is {@code null}
     * @throws IllegalStateException if a match between the specified teams is already in progress
     */
    public Match startNewMatch(String homeTeam, String awayTeam) {
        Objects.requireNonNull(homeTeam, "homeTeam cannot be null");
        Objects.requireNonNull(awayTeam, "awayTeam cannot be null");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting new match: {} - {}", homeTeam, awayTeam);
        }

        MatchKey key = new MatchKey(homeTeam, awayTeam);

        synchronized (matches) {
            if (matches.containsKey(key)) {
                throw new IllegalStateException("match already exists");
            }
            Match match = new Match(homeTeam, awayTeam, INITIAL_SCORE, INITIAL_SCORE);
            matches.put(key, match);
            return match;
        }
    }

    /**
     * Updates the score of an ongoing match between the specified home and away teams.
     * <p>
     * This method updates the score of the match identified by the given team names.
     * Both {@code homeTeam} and {@code awayTeam} must not be {@code null},
     * and a match between these teams must exist in the scoreboard.
     * <p>
     * This method is thread-safe and can be safely called from multiple threads simultaneously.
     *
     * @param homeTeam      the name of the home team; must not be {@code null}
     * @param awayTeam      the name of the away team; must not be {@code null}
     * @param homeTeamScore the new score for the home team
     * @param awayTeamScore the new score for the away team
     * @return the updated {@link Match} object representing the match with the new scores
     * @throws NullPointerException  if either {@code homeTeam} or {@code awayTeam} is {@code null}
     * @throws IllegalStateException if a match between the specified teams is not found on the scoreboard
     */
    public Match updateScore(String homeTeam, String awayTeam, int homeTeamScore, int awayTeamScore) {
        Objects.requireNonNull(homeTeam, "homeTeam cannot be null");
        Objects.requireNonNull(awayTeam, "awayTeam cannot be null");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating score: {} - {} {} : {}", homeTeam, awayTeam, homeTeamScore, awayTeamScore);
        }

        MatchKey key = new MatchKey(homeTeam, awayTeam);

        synchronized (matches) {
            if (!matches.containsKey(key)) {
                throw new IllegalStateException("match does not exist");
            }
            Match match = new Match(homeTeam, awayTeam, homeTeamScore, awayTeamScore);
            matches.put(key, match);
            return match;
        }
    }

    /**
     * Ends an ongoing match between the specified home and away teams.
     * <p>
     * This method marks the match between the given teams as finished. Both {@code homeTeam}
     * and {@code awayTeam} must not be {@code null}, and the match must exist in the scoreboard.
     * After calling this method, the match is no longer considered active.
     * <p>
     * This method is thread-safe and can be safely called from multiple threads simultaneously.
     *
     * @param homeTeam the name of the home team; must not be {@code null}
     * @param awayTeam the name of the away team; must not be {@code null}
     * @return the {@link Match} object representing the finished match
     * @throws NullPointerException  if either {@code homeTeam} or {@code awayTeam} is {@code null}
     * @throws IllegalStateException if a match between the specified teams is not found on the scoreboard
     */
    public Match finishMatch(String homeTeam, String awayTeam) {
        Objects.requireNonNull(homeTeam, "homeTeam cannot be null");
        Objects.requireNonNull(awayTeam, "awayTeam cannot be null");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Finishing match: {} - {}", homeTeam, awayTeam);
        }

        MatchKey key = new MatchKey(homeTeam, awayTeam);

        Match match;
        synchronized (matches) {
            match = matches.remove(key);
        }
        if (match == null) {
            throw new IllegalStateException("match does not exist");
        }
        return match;
    }

    /**
     * Retrieves a summary of all currently ongoing matches, sorted by specific criteria.
     * <p>
     * The list is sorted in descending order, first by the total score of both teams
     * (higher total score comes first). If two matches have the same total score,
     * the match started later appears higher in the list.
     * <p>
     * The returned list is never {@code null}; it may be empty if there are no ongoing matches.
     * <p>
     * This method is thread-safe and can be safely called from multiple threads simultaneously.
     *
     * @return a {@link List} of {@link Match} objects representing the ongoing matches,
     * sorted by total score and start time
     */
    public List<Match> getSummary() {
        synchronized (matches) {
            return matches.values()
                    .stream()
                    .sorted(MATCH_COMPARATOR)
                    .toList()
                    .reversed();
        }
    }

}
