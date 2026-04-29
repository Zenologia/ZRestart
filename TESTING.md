# ZRestart Testing Guide *Developed by Zenologia*

Use this guide to verify a complete ZRestart build on a Paper 26.1.2 server running Java 25.

---

## ✅ Test environment

| Item | Required value |
|---|---:|
| Server | Paper 26.1.2 |
| Java | 25+ |
| Plugin descriptor | `paper-plugin.yml` only |
| Restart method | `Bukkit.restart()` |
| Optional dependency | PlaceholderAPI 2.12.2+ |

Before runtime testing:

- Confirm the JAR does not include `plugin.yml`.
- Confirm the JAR includes `paper-plugin.yml`.
- Confirm `spigot.yml` has a real `settings.restart-script`.
- Keep console open so warnings can be verified.

---

## 🧪 Unit test checklist

| Area | Expected coverage |
|---|---|
| Duration parsing | Raw seconds, `HOUR:MINUTE`, lowercase units, uppercase units, spaced units, duplicate parts |
| Duration rejection | Zero, negative, malformed, invalid minute values, overflow |
| Schedule parsing | `Daily`, weekdays, case-insensitive names, optional reasons |
| Schedule rejection | Invalid day, hour, minute, malformed entries |
| Next restart | Daily, weekly, earliest future selection, same-day passed handling |
| DST behavior | Gap moves to next valid time, overlap uses later occurrence |
| Warning behavior | De-dupe and repeated warnings after delay |
| Reload behavior | Invalid snapshot keeps the previous valid config |

Run when ready:

```text
./gradlew test
```

---

## 🚀 Startup tests

| Test | Steps | Expected result |
|---|---|---|
| First boot | Start server with ZRestart installed | `config.yml` and `messages.yml` generate |
| Paper-only descriptor | Check startup logs | Plugin loads as a Paper plugin |
| PlaceholderAPI absent | Start without PlaceholderAPI | Plugin loads and logs internal-placeholder behavior |
| Restart script valid | Configure `settings.restart-script` | No missing-script warning |
| Restart script missing | Remove or blank restart script | Configurable warning is logged |

---

## ⌨️ Command tests

Run these in game or console:

```text
/zrestart
/zrestart time
/zrestart now 90 Test raw seconds
/zrestart stop
/zrestart now 1:30 Test hour-minute duration
/zrestart delay 5m
/zrestart stop
/zrestart now 0:30 Test zero-hour duration
/zrestart stop
/zrestart now 30m Test readable duration
/zrestart stop
/zrestart now 30M Test uppercase unit
/zrestart stop
/zrestart now 1h30m15m Test duplicate parts
/zrestart reload
```

Expected results:

- `/zrestart` shows all commands, including admin commands.
- `/zrestart time` works for normal players by default.
- Every documented interval format starts the correct countdown.
- `/zrestart now` replaces any active countdown.
- `/zrestart delay` moves the active target later without editing `config.yml`.
- `/zrestart stop` cancels the active countdown and removes boss bars.
- `/zrestart reload` reloads both config files.

---

## 🗓️ Schedule tests

Use this schedule block:

```yaml
schedule:
  enabled: true
  entries:
    - "Daily;05;00;Daily maintenance"
    - "daily;06;00;Lowercase daily"
    - "monday;11;00;Lowercase weekday"
    - "Friday;22;00"
    - "BadDay;99;99;Should warn and skip"
```

Expected results:

- Valid entries are accepted.
- Lowercase `daily` and `monday` are accepted.
- Multiple entries can exist on the same day.
- Invalid entries are skipped with configurable warnings.
- `/zrestart reload` recalculates the next scheduled restart immediately.

---

## 🌗 DST tests

Use `America/New_York` for these checks.

| Case | Example entry | Expected result |
|---|---|---|
| DST gap | `Sunday;02;30;Gap test` on spring-forward day | Warns and moves to next valid later time |
| DST overlap | `Sunday;01;30;Overlap test` on fall-back day | Warns and uses the later occurrence |

Expected result:

- The server logs `console.dst-adjustment`.
- The restart is still scheduled rather than rejected.

---

## 📣 Display tests

| Test | Config | Expected result |
|---|---|---|
| Chat only | `chat.enabled: true`, title/boss bar false | Chat warnings fire |
| Title only | `title.enabled: true`, chat/boss bar false | Title/subtitle warnings fire |
| Boss bar only | `boss-bar.enabled: true`, chat/title false | Boss bar appears, shrinks, and clears |
| Boss show-from | `show-from: 2m` with no `2m` warning | Boss bar still appears at 2 minutes |
| All channels | Enable chat, title, boss bar | All channels fire from the same warning list |
| Join mid-countdown | Join while boss bar is visible | Joining player sees the boss bar immediately |
| Delay repeat | Fire 5m warning, delay above 5m, wait | 5m warning fires again |

---

## 🧩 PlaceholderAPI tests

| Test | Steps | Expected result |
|---|---|---|
| PAPI absent | Remove PlaceholderAPI and restart | ZRestart still loads |
| PAPI present | Install PlaceholderAPI and restart | Placeholder support logs enabled |
| Player-bound chat | Add a PAPI placeholder to countdown chat | Placeholder resolves per player |
| Player-bound title | Add a PAPI placeholder to title/subtitle | Placeholder resolves per player |
| Player-bound boss bar | Add a PAPI placeholder to boss-bar text | Placeholder resolves per player |
| Console output | Add a PAPI placeholder to console message | Console uses internal placeholders only |

---

## 🔁 Reload tests

| Test | Steps | Expected result |
|---|---|---|
| Valid reload | Edit config and run `/zrestart reload` | New config applies |
| Invalid schedule | Add one bad schedule entry | Bad entry warns; valid entries remain active |
| Invalid snapshot | Disable all display channels and reload | Reload fails and old config remains active |
| Invalid timezone | Set bad timezone and valid fallback | Warning logs and fallback timezone is used |
| Bad fallback timezone | Set timezone and fallback both invalid | Reload fails and old config remains active |
| Manual countdown reload | Start `/zrestart now 30m Test`, then reload | Manual countdown continues |

---

## 🔥 Restart execution tests

| Test | Config | Expected result |
|---|---|---|
| Native restart | Valid restart script | Countdown reaches zero and calls `Bukkit.restart()` |
| Player/world saves | Default config | Player and world saves are attempted before restart |
| Pre-commands continue | `failure-behavior: CONTINUE` with one bad command | Failure logs and restart continues |
| Pre-commands abort | `failure-behavior: ABORT` with one bad command | Failure logs, abort broadcast sends, restart does not run |

---

## ✅ Acceptance pass

- [ ] Plugin loads on Paper 26.1.2 with Java 25.
- [ ] Plugin uses `paper-plugin.yml` and no `plugin.yml`.
- [ ] `ZRestartBootstrap` registers commands through Paper lifecycle.
- [ ] `/zrestart` help works and shows admin commands.
- [ ] `/zrestart time` works for default players.
- [ ] Manual countdowns accept every documented interval format.
- [ ] Manual countdowns replace active countdowns.
- [ ] Delay allows warning thresholds to fire again.
- [ ] Stop cancels countdowns and removes boss bars.
- [ ] Reload keeps old config when new config is invalid.
- [ ] Schedule parsing accepts valid entries and skips invalid ones.
- [ ] DST gap/overlap cases warn and resolve safely.
- [ ] Chat, title, and boss bar warnings work independently.
- [ ] Boss bar appears at `show-from` even outside `warning-times`.
- [ ] PlaceholderAPI is optional and never required for startup.
- [ ] ZRestart does not register a PlaceholderAPI expansion.
- [ ] Pre-restart commands obey `CONTINUE` and `ABORT`.
- [ ] Restart execution calls `Bukkit.restart()`.

---

## 👤 Author

- **Zenologia**
- [GitHub Repository](REPO_LINK_PLACEHOLDER)
- [License](LICENSE_LINK_PLACEHOLDER)
