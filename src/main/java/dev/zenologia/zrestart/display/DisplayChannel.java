package dev.zenologia.zrestart.display;

import dev.zenologia.zrestart.countdown.CountdownState;
import java.time.Duration;

public interface DisplayChannel {
    void showWarning(CountdownState state, Duration remaining);

    default void showWarning(CountdownState state, Duration warningTime, Duration remaining) {
        showWarning(state, remaining);
    }

    default void tick(CountdownState state, Duration remaining) {
    }

    default void clear() {
    }
}
