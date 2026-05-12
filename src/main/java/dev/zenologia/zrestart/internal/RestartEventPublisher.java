package dev.zenologia.zrestart.internal;

import dev.zenologia.zrestart.countdown.CountdownState;

public interface RestartEventPublisher {
    void countdownStarted(CountdownState state);

    void countdownCancelled(CountdownState state);

    void restartExecuting(CountdownState state);
}
