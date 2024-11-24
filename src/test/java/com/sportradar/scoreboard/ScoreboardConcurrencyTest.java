package com.sportradar.scoreboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScoreboardConcurrencyTest {

    private static final Logger LOG = LoggerFactory.getLogger(ScoreboardConcurrencyTest.class);

    private static final int NUMBER_OF_MATCHES = 100;
    private static final int FINAL_SCORE = 99;
    private static final int NUMBER_OF_THREADS = 10;

    private Scoreboard scoreboard;

    @BeforeEach
    public void setup() {
        scoreboard = new Scoreboard();
    }

    @Test
    public void shouldPerformAllExecutionsInSingleThread() throws InterruptedException {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            doTest(executor);
        }
    }

    @Test
    public void shouldPerformAllExecutionsConcurrently() throws InterruptedException {
        try (ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)) {
            doTest(executor);
        }
    }

    /**
     * Tests the functionality of starting, updating, and finishing matches using the provided {@code executor}.
     * <p>
     * The method verifies that:
     * <ul>
     *     <li>The correct number of matches is processed, with half of the matches being finished.
     *     <li>The score of each match is updated to the specified final value.
     *     <li>Matches that are supposed to be finished are removed from the scoreboard,
     *         so the resulting summary will contain half the number of the initially started matches.
     *     <li>All threads complete their execution within the specified time limit.
     * </ul>
     *
     * @param executor the {@link ExecutorService} used to run the matches in parallel; it should be provided by the caller
     * @throws InterruptedException if the current thread is interrupted while waiting for the threads to complete
     */
    private void doTest(ExecutorService executor) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(NUMBER_OF_MATCHES);

        for (int i = 1; i <= NUMBER_OF_MATCHES; i++) {

            String homeTeam = "Home Team #" + i;
            String awayTeam = "Away Team #" + i;
            boolean finish = i % 2 == 0;

            executor.submit(() -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Starting match {} - {}", homeTeam, awayTeam);
                }
                scoreboard.startNewMatch(homeTeam, awayTeam);
                Match lastUpdate = null;
                for (int score = 1; score <= FINAL_SCORE; score++) {
                    lastUpdate = scoreboard.updateScore(homeTeam, awayTeam, score, score);
                }
                if (finish) {
                    lastUpdate = scoreboard.finishMatch(homeTeam, awayTeam);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("The match is over: {}", lastUpdate);
                    }
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("All updates has been completed: {}", lastUpdate);
                }
                latch.countDown();
            });
        }

        boolean threadsFinishedInTime = latch.await(5, TimeUnit.SECONDS);
        assertTrue(threadsFinishedInTime, "Waiting time elapsed before count reached zero: " + latch.getCount());

        List<Match> summary = scoreboard.getSummary();

        assertThat(summary).hasSize(NUMBER_OF_MATCHES / 2);

        assertThat(summary)
                .withFailMessage("All teams in all matches should score %d points", FINAL_SCORE)
                .allMatch(m -> m.homeTeamScore() == FINAL_SCORE && m.awayTeamScore() == FINAL_SCORE);
    }

}
