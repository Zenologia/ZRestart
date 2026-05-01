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
| Sound resolution | Bukkit sound names, namespaced Minecraft keys, invalid key rejection |
| Reload behavior | Invalid snapshot keeps the previous valid config |
| File migration | Old config/message versions migrate, preserve admin values, add defaults/comments, and create backups |

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
| Old config version | Remove `config-version` and restart | File migrates and `config.yml.bak-*` is created |
| Old messages version | Remove `messages-version` and restart | File migrates and `messages.yml.bak-*` is created |

---

## Versioned file migration tests

Use these tests after changing bundled `config.yml` or `messages.yml`.

| Test | Steps | Expected result |
|---|---|---|
| Missing config version | Remove `config-version` from installed `config.yml`, then restart | Treated as version `0`, migrated to bundled version, backup created |
| Missing messages version | Remove `messages-version` from installed `messages.yml`, then restart | Treated as version `0`, migrated to bundled version, backup created |
| Lower version | Set installed version lower than bundled version | File rewrites from latest template and preserves matching admin values |
| Same version | Set installed version equal to bundled version | File is not rewritten |
| Higher version | Set installed version higher than bundled version | File is not downgraded |
| New default key | Add a new bundled key and bump version | Existing admin file receives the new default key |
| Admin value preservation | Change `settings.timezone`, then migrate | Admin value remains after migration |
| Template comments | Add/update bundled comments and bump version | Updated comments appear after migration |
| Backup safety | Migrate any older file | Original file exists as `config.yml.bak-*` or `messages.yml.bak-*` |

Maintainer rule:

- Bump `config-version` only for config schema/default/comment updates.
- Bump `messages-version` only for message key/default/comment updates.
- Do not bump either version for Java-only changes.

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
- `/zrestart now` shows the configured restart scheduled title popup to online players.
- `/zrestart delay` moves the active target later without editing `config.yml`.
- `/zrestart stop` cancels the active countdown and removes boss bars.
- `/zrestart stop` shows the configured restart cancelled title popup when it cancels a countdown.
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
| Manual start popup | Run `/zrestart now 30m Maintenance` | Online players see `now.popup-title` and `now.popup-subtitle` |
| Cancel popup | Run `/zrestart stop` during an active countdown | Online players see `stop.popup-title` and `stop.popup-subtitle` |
| Invalid manual start | Run `/zrestart now bad` | No player popup appears |
| No active cancel | Run `/zrestart stop` with no countdown | No player popup appears |
| Boss bar only | `boss-bar.enabled: true`, chat/title false | Boss bar appears, shrinks, and clears |
| Sound only | `sounds.enabled: true`, chat/title/boss bar false, valid sound entries | Sounds play at configured warning times |
| Sound categories | Change `sounds.category` through valid values | Sound remains audible through the selected client category |
| Bukkit sound name | Use `BLOCK_NOTE_BLOCK_PLING` | Sound plays without validation warnings |
| Minecraft sound key | Use `minecraft:block.note_block.pling` | Sound plays without validation warnings |
| Custom sound key | Use a resource-pack key like `custom:restart_ping` | Key is accepted and plays for clients with the resource pack |
| Sound time mismatch | Set a sound entry `time` not present in `warning-times` | Entry is skipped with a console warning |
| Boss show-from | `show-from: 2m` with no `2m` warning | Boss bar still appears at 2 minutes |
| All channels | Enable chat, title, boss bar, and sounds | All channels fire from the same warning list |
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
| Sound-only valid snapshot | Disable chat/title/boss bar, enable valid sounds, and reload | Reload succeeds |
| Sound-only invalid snapshot | Disable chat/title/boss bar, enable sounds with no valid entries, and reload | Reload fails and old config remains active |
| Invalid sound category | Set `sounds.category` to a bad value and reload | Warning logs and `MASTER` is used |
| Invalid sound entry | Set a bad sound name or invalid namespaced key and reload | Entry is skipped with a console warning |
| Invalid timezone | Set bad timezone and valid fallback | Warning logs and fallback timezone is used |
| Bad fallback timezone | Set timezone and fallback both invalid | Reload fails and old config remains active |
| Manual countdown reload | Start `/zrestart now 30m Test`, then reload | Manual countdown continues |
| File version reload | Change `config-version` while server is running and run `/zrestart reload` | Version migration does not run during reload; reload only validates and applies current files |

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
- [ ] Manual countdown starts show the configured title popup to online players.
- [ ] Delay allows warning thresholds to fire again.
- [ ] Stop cancels countdowns and removes boss bars.
- [ ] Stop shows the configured cancellation title popup only after a successful cancellation.
- [ ] Reload keeps old config when new config is invalid.
- [ ] Old `config.yml` and `messages.yml` versions auto-update on startup with backups.
- [ ] Same-version config/message files are not rewritten.
- [ ] Schedule parsing accepts valid entries and skips invalid ones.
- [ ] DST gap/overlap cases warn and resolve safely.
- [ ] Chat, title, and boss bar warnings work independently.
- [ ] Sound warnings work independently with Bukkit names and namespaced keys.
- [ ] Sound entries only fire at matching `warning-times`.
- [ ] Invalid sound entries warn and do not break valid entries.
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
