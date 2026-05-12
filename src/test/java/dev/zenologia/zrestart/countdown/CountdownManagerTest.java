package dev.zenologia.zrestart.countdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.zenologia.zrestart.internal.RestartEventPublisher;
import dev.zenologia.zrestart.schedule.NextRestart;
import dev.zenologia.zrestart.schedule.ScheduleDay;
import dev.zenologia.zrestart.schedule.ScheduleEntry;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CountdownManagerTest {
    @Test
    void manualReplacementPublishesCancellationBeforeNewStart() {
        RecordingPublisher publisher = new RecordingPublisher();
        CountdownManager manager = newManager(publisher, state -> true, state -> {
        });

        manager.startManual(Duration.ofMinutes(10), "First");
        manager.startManual(Duration.ofMinutes(5), "Second");

        assertEquals(List.of("started:First", "cancelled:First", "started:Second"), publisher.events);
        assertEquals("Second", manager.activeState().orElseThrow().reason());
    }

    @Test
    void scheduledReplacementPublishesCancellationBeforeNewStart() {
        RecordingPublisher publisher = new RecordingPublisher();
        CountdownManager manager = newManager(publisher, state -> true, state -> {
        });

        manager.startScheduled(nextRestart("Old scheduled", Instant.now().plus(Duration.ofMinutes(10))));
        assertTrue(manager.cancelActive().isPresent());
        manager.startScheduled(nextRestart("New scheduled", Instant.now().plus(Duration.ofMinutes(20))));

        assertEquals(List.of("started:Old scheduled", "cancelled:Old scheduled", "started:New scheduled"), publisher.events);
        assertEquals("New scheduled", manager.activeState().orElseThrow().reason());
    }

    @Test
    void abortedExecutionAllowsHandlerToStartNextScheduledCountdown() {
        RecordingPublisher publisher = new RecordingPublisher();
        List<String> aborts = new ArrayList<>();
        CountdownManager[] holder = new CountdownManager[1];
        holder[0] = newManager(
            publisher,
            state -> false,
            state -> {
                aborts.add(state.reason());
                holder[0].startScheduled(nextRestart("Next scheduled", Instant.now().plus(Duration.ofMinutes(30))));
            }
        );

        holder[0].startScheduled(nextRestart("Expired scheduled", Instant.now().minus(Duration.ofSeconds(1))));
        holder[0].tick();

        assertEquals(List.of("Expired scheduled"), aborts);
        assertEquals("Next scheduled", holder[0].activeState().orElseThrow().reason());
        assertEquals(List.of("started:Expired scheduled", "started:Next scheduled"), publisher.events);
    }

    private static CountdownManager newManager(
        RecordingPublisher publisher,
        java.util.function.Function<CountdownState, Boolean> restartExecutor,
        java.util.function.Consumer<CountdownState> abortHandler
    ) {
        return new CountdownManager(null, new WarningDispatcher(), restartExecutor, publisher, abortHandler);
    }

    private static NextRestart nextRestart(String reason, Instant instant) {
        ScheduleEntry entry = new ScheduleEntry(ScheduleDay.DAILY, 5, 0, reason, "Daily;05;00;" + reason);
        return new NextRestart(entry, instant.atZone(ZoneOffset.UTC), List.of());
    }

    private static final class RecordingPublisher implements RestartEventPublisher {
        private final List<String> events = new ArrayList<>();

        @Override
        public void countdownStarted(CountdownState state) {
            this.events.add("started:" + state.reason());
        }

        @Override
        public void countdownCancelled(CountdownState state) {
            this.events.add("cancelled:" + state.reason());
        }

        @Override
        public void restartExecuting(CountdownState state) {
            this.events.add("executing:" + state.reason());
        }
    }
}
