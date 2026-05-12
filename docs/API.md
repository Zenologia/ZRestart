# ZRestart Public API

ZRestart 1.4 exposes a small notification-only API for Paper plugins that need to know whether a restart is pending or executing. The intended use case is a companion plugin, such as a player recording plugin, refusing to start work when a restart is too close.

The API is available from the ZRestart plugin JAR. There is no separate API artifact in this release.

## Consumer dependency

Add ZRestart as a server dependency in the consuming plugin's `paper-plugin.yml`.

Use `required: true` when the consuming plugin cannot run without ZRestart:

```yaml
dependencies:
  server:
    ZRestart:
      load: BEFORE
      required: true
      join-classpath: true
```

Use `required: false` when ZRestart support is optional:

```yaml
dependencies:
  server:
    ZRestart:
      load: BEFORE
      required: false
      join-classpath: true
```

For optional support, keep direct ZRestart API imports in a small bridge/listener class and only load that class after confirming ZRestart is enabled. This keeps the consuming plugin safe when ZRestart is not installed.

## Loading the service

ZRestart registers `dev.zenologia.zrestart.api.ZRestartApi` through Bukkit's services manager.

Consumer plugins should query this service during their own `onEnable()` and then use events for runtime changes. A dependent plugin may load after ZRestart has already activated its initial scheduled countdown, so querying on enable is the reliable way to discover startup state.

```java
import dev.zenologia.zrestart.api.ZRestartApi;
import java.time.Duration;
import org.bukkit.Bukkit;

ZRestartApi zrestart = Bukkit.getServicesManager().load(ZRestartApi.class);
if (zrestart != null && zrestart.isRestartWithin(Duration.ofMinutes(10))) {
    // Refuse to start recording.
}
```

The service is intended for normal server-thread use. Event handlers are called synchronously by Bukkit.

## Query API

`ZRestartApi` methods:

```java
Optional<RestartCountdown> currentCountdown();
Optional<RestartCountdown> nextRestart();
boolean isRestartWithin(Duration window);
boolean isManualRestartPending();
boolean isRestartExecuting();
```

`currentCountdown()` returns the active scheduled or manual countdown, if one exists.

`nextRestart()` returns the active countdown when present. If there is no active countdown, it returns the next known scheduled restart projection when one is available. Projected snapshots use `id == 0`.

`isRestartWithin(Duration window)` is the main recorder-plugin helper. It returns `true` when ZRestart knows about a restart whose target time is in the future and no farther away than the supplied window. A negative window throws `IllegalArgumentException`.

`isManualRestartPending()` returns `true` only while a manual `/zrestart now` countdown is active.

`isRestartExecuting()` returns `true` after ZRestart has begun final restart execution.

## Countdown snapshot

`RestartCountdown` is immutable:

```java
public record RestartCountdown(
    long id,
    RestartKind kind,
    Instant targetTime,
    Duration totalDuration,
    String reason,
    String scheduleEntryRaw
)
```

Fields:

| Field | Meaning |
|---|---|
| `id` | Stable active countdown id; `0` when the snapshot is only a non-active schedule projection |
| `kind` | `SCHEDULED` or `MANUAL` |
| `targetTime` | Instant when the restart is expected to execute |
| `totalDuration` | Original active countdown duration; for projections, remaining duration at snapshot creation |
| `reason` | Configured or manually supplied restart reason, or empty string |
| `scheduleEntryRaw` | Raw schedule entry for scheduled restarts, or empty string |

Helpers:

```java
Duration remainingFrom(Instant now);
boolean isWithin(Duration window, Instant now);
boolean manual();
```

`remainingFrom(...)` can return a negative duration if the target has already passed. `isWithin(...)` only returns `true` for pending future restarts inside the supplied window.

Treat `id == 0` as projected schedule state, not an active countdown. Projection snapshots are useful for refusing work before a future scheduled restart, but their `totalDuration` is the current remaining duration at the time the snapshot was created.

## Events

All events are notification-only and are not cancellable.

Listen for countdown starts:

```java
import dev.zenologia.zrestart.api.RestartKind;
import dev.zenologia.zrestart.api.event.ZRestartCountdownStartedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class RestartListener implements Listener {
    @EventHandler
    public void onRestartCountdownStarted(ZRestartCountdownStartedEvent event) {
        if (event.countdown().kind() == RestartKind.MANUAL) {
            // A manual restart was triggered with /zrestart now.
        }
    }
}
```

Listen for cancellations:

```java
import dev.zenologia.zrestart.api.event.ZRestartCountdownCancelledEvent;
import org.bukkit.event.EventHandler;

@EventHandler
public void onRestartCountdownCancelled(ZRestartCountdownCancelledEvent event) {
    // event.countdown() is the countdown that was cancelled.
}
```

Listen for final execution:

```java
import dev.zenologia.zrestart.api.event.ZRestartExecutingEvent;
import org.bukkit.event.EventHandler;

@EventHandler
public void onRestartExecuting(ZRestartExecutingEvent event) {
    // ZRestart is saving data and calling Bukkit.restart().
}
```

Event timing:

| Event | Fired when |
|---|---|
| `ZRestartCountdownStartedEvent` | A scheduled countdown is activated or `/zrestart now` starts a manual countdown |
| `ZRestartCountdownCancelledEvent` | An active countdown is cancelled, including `/zrestart stop` and reloads that clear or replace an automatic scheduled countdown |
| `ZRestartExecutingEvent` | The countdown reaches zero and pre-restart commands have allowed restart execution to continue |

When `/zrestart now` replaces an existing countdown, ZRestart fires `ZRestartCountdownCancelledEvent` for the previous countdown, then `ZRestartCountdownStartedEvent` for the new manual countdown.

Manual countdowns continue across `/zrestart reload`. Automatic scheduled countdowns may be cancelled and replaced when reload recalculates the configured schedule; in that case ZRestart fires the cancellation event for the old scheduled countdown before any started event for the recalculated countdown.

## Recorder plugin pattern

For a recording plugin, check the service immediately before starting a recording:

```java
private boolean restartTooClose(Duration minimumWindow) {
    ZRestartApi zrestart = Bukkit.getServicesManager().load(ZRestartApi.class);
    return zrestart != null && zrestart.isRestartWithin(minimumWindow);
}
```

Then use the events to stop or mark in-progress recordings if an administrator manually triggers a restart after recording has already started.
