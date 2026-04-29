package dev.zenologia.zrestart.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReloadDeciderTest {
    private final ReloadDecider decider = new ReloadDecider();

    @Test
    void acceptsValidSnapshot() {
        ConfigLoadResult candidate = new ConfigLoadResult(null, List.of(), List.of());

        ReloadDecision decision = this.decider.decide(null, candidate);

        assertTrue(decision.accepted());
    }

    @Test
    void keepsPreviousConfigWhenSnapshotIsInvalid() {
        RestartConfig previous = new RestartConfig(null, null, null, null, null);
        ConfigLoadResult candidate = new ConfigLoadResult(null, List.of(), List.of("At least one countdown display channel must be enabled."));

        ReloadDecision decision = this.decider.decide(previous, candidate);

        assertFalse(decision.accepted());
        assertSame(previous, decision.activeConfig());
    }
}
