# ZRestart *Developed by Zenologia*

A lightweight Paper-only automatic restart scheduler for Minecraft Java 26.x servers. ZRestart schedules native Paper/Bukkit restarts, shows configurable countdown warnings, supports manual restart control, and keeps all player/admin-facing text in `messages.yml`.

## What it does (and doesn’t) do

- ✅ Schedules restarts with UAR-style entries: `DAY;HOUR;MINUTE[;REASON]`.
- ✅ Supports manual restart countdowns, delays, cancellations, and reloads under `/zrestart`.
- ✅ Sends countdown warnings through chat, titles, and boss bars from one shared warning list.
- ✅ Calls `Bukkit.restart()` directly and attempts player/world saves before restart.
- ✅ Runs optional pre-restart console commands with `CONTINUE` or `ABORT` failure behavior.
- ✅ Consumes PlaceholderAPI placeholders in player-bound messages when PlaceholderAPI is installed.
- ❌ Does not support Spigot, Folia, Velocity, BungeeCord, proxies, or old Minecraft versions.
- ❌ Does not expose a ZRestart PlaceholderAPI expansion.
- ❌ Does not kick players, skip empty-server restarts, or use external restart commands as the restart mechanism.

---

## ⚙️ FEATURES

- ⏱️ Automatic restart scheduling using `Daily` or weekday entries
- Manual `/zrestart now <interval> [reason]` countdowns
- Manual `/zrestart delay <interval>` and `/zrestart stop`
- Shared configurable warning thresholds like `30m`, `1:30`, `3600`, and `1h 30m`
- Chat, title, and boss bar warning channels
- MiniMessage and legacy `&` color support
- Configurable timezone with DST gap/overlap handling
- Soft PlaceholderAPI support for player-bound messages
- Versioned `config.yml` and `messages.yml` auto-updates with backups
- Restart-script diagnostic warning for unsafe `Bukkit.restart()` setups
- Atomic reload behavior that keeps the previous valid runtime config on reload failure
- Focused parser, scheduler, warning, and reload unit tests

---

## Requirements

- Paper 26.1.2+
- Java 25+
- Gradle 9.5.0+ for building from source
- Optional: PlaceholderAPI 2.12.2+

---

## Installation

1. Build the plugin JAR.
2. Drop `ZRestart.jar` into your server’s `plugins/` folder.
3. Start the server once so `config.yml` and `messages.yml` generate.
4. Confirm `spigot.yml` has a valid `settings.restart-script`.
5. Edit `plugins/ZRestart/config.yml` for schedules, channels, timezone, and restart commands.
6. Edit `plugins/ZRestart/messages.yml` for all user/admin/console text.
7. Reload after config changes:

```text
/zrestart reload
```

Important notes:

- `Bukkit.restart()` may stop the server instead of restarting if Paper/Spigot restart scripting is not configured.
- ZRestart intentionally targets Paper 26.x and newer compatible Paper APIs only.
- PlaceholderAPI is optional and must not be bundled with the plugin.
- Changing schedules with `/zrestart now`, `/zrestart delay`, or `/zrestart stop` never rewrites `config.yml`.
- Plugin updates may rewrite old `config.yml` and `messages.yml` files from the latest bundled templates, but matching admin values are carried over and timestamped backups are created first.

---

## Config and message auto-updates

ZRestart uses schema version numbers at the top of each file:

```yaml
config-version: 1
```

```yaml
messages-version: 1
```

When the plugin starts, it compares the installed file version to the bundled file version.

| Case | Result |
|---|---|
| Installed version is missing | Treated as version `0` and migrated |
| Installed version is lower | Backup is created, file is rewritten from the latest template, matching admin values are preserved |
| Installed version is the same | File is left alone |
| Installed version is higher | File is left alone and never downgraded |

Backups are saved beside the original files:

```text
config.yml.bak-YYYYMMDD-HHMMSS
messages.yml.bak-YYYYMMDD-HHMMSS
```

For plugin maintainers:

- Bump `config-version` when adding config keys, changing defaults, or pushing updated config comments.
- Bump `messages-version` when adding message keys, changing message defaults, or pushing updated message comments.
- Do not bump these versions for Java-only changes.

---

Full list of timezones: 

Etc/GMT+12
Pacific/Pago_Pago
Pacific/Niue
Pacific/Honolulu
Pacific/Tahiti
America/Adak
Pacific/Marquesas
Pacific/Gambier
America/Anchorage
Pacific/Pitcairn
America/Los_Angeles
America/Vancouver
America/Phoenix
America/Denver
Pacific/Galapagos
America/Mexico_City
Pacific/Easter
America/Chicago
America/Lima
America/Jamaica
America/Havana
America/New_York
America/Caracas
America/Santo_Domingo
America/Santiago
America/Halifax
America/St_Johns
America/Sao_Paulo
America/Miquelon
America/Noronha
America/Nuuk
Atlantic/Cape_Verde
Atlantic/Azores
UTC
Africa/Abidjan
Europe/London
Europe/Lisbon
Antarctica/Troll
Africa/Algiers
Africa/Lagos
Europe/Dublin
Africa/Casablanca
Europe/Paris
Africa/Maputo
Africa/Tripoli
Africa/Johannesburg
Europe/Athens
Africa/Cairo
Asia/Beirut
Asia/Gaza
Asia/Jerusalem
Europe/Istanbul
Africa/Nairobi
Europe/Moscow
Asia/Tehran
Asia/Dubai
Asia/Kabul
Asia/Tashkent
Asia/Karachi
Asia/Colombo
Asia/Kolkata
Asia/Kathmandu
Asia/Dhaka
Asia/Yangon
Asia/Bangkok
Asia/Jakarta
Asia/Singapore
Australia/Perth
Asia/Shanghai
Asia/Hong_Kong
Asia/Manila
Asia/Makassar
Australia/Eucla
Asia/Chita
Asia/Tokyo
Asia/Seoul
Asia/Jayapura
Australia/Darwin
Australia/Adelaide
Asia/Vladivostok
Australia/Brisbane
Pacific/Guam
Australia/Sydney
Australia/Lord_Howe
Pacific/Bougainville
Pacific/Norfolk
Asia/Kamchatka
Pacific/Auckland
Pacific/Chatham
Pacific/Tongatapu
Pacific/Kiritimati

## Configuration (`config.yml`)

Example:

```yaml
config-version: 1

settings:
  timezone: "America/New_York"
  fallback-timezone: "UTC"
  check-restart-script: true
  papi-placeholders: true

schedule:
  enabled: true
  entries:
    - "Daily;05;00;Daily maintenance"
    - "Monday;11;00;Weekly maintenance"
    - "Friday;22;00"

countdown:
  warning-times:
    - 30m
    - 15m
    - 5m
    - 1m
    - 30s
    - 5s
    - 4s
    - 3s
    - 2s
    - 1s

  chat:
    enabled: true

  title:
    enabled: false
    fade-in: 10
    stay: 40
    fade-out: 10

  boss-bar:
    enabled: false
    color: RED
    overlay: PROGRESS
    show-from: 5m
    progress: true

formatting:
  minimessage: true
  legacy-ampersand-colors: true
  empty-reason: "No reason provided"
  time-format:
    include-days: true
    include-hours: true
    include-minutes: true
    include-seconds: true
    compact: false

pre-restart-commands:
  enabled: false
  failure-behavior: CONTINUE
  commands:
    - "save-all"
```

### Notes

- `timezone` must be an IANA timezone ID, such as `America/New_York` or `UTC`.
- If `timezone` is invalid, ZRestart falls back to `fallback-timezone` and logs a warning.
- Schedule entries accept only `Daily` or full weekday names.
- Schedule entry reasons are optional and may contain spaces or semicolons after the third `;`.
- Invalid schedule entries are skipped with configurable console warnings.
- At least one countdown display channel should be enabled.
- Boss bar `show-from` does not need to appear in `warning-times`.

---

## Configuration (`messages.yml`)

All player, admin, and console-facing messages live in `messages.yml`.

Example:

```yaml
messages-version: 1

prefix: "<gray>[<red>ZRestart</red>]</gray> "

commands:
  no-permission: "{prefix}<red>You do not have permission: {permission}</red>"
  invalid-usage: "{prefix}<red>Usage: {command}</red>"
  unknown-subcommand: "{prefix}<red>Unknown subcommand.</red>"

countdown:
  chat: "{prefix}<gray>Server restart in <red>{time_formatted}</red>. Reason: <red>{reason}</red></gray>"
  title: "<red>Server Restart</red>"
  subtitle: "<gray>Restarting in <red>{time_formatted}</red></gray>"
  boss-bar: "Server restart in {time_formatted}"
```

### Message placeholders

| Placeholder | Meaning |
|---|---|
| `{prefix}` | Configurable message prefix |
| `{time}` | Remaining time in readable format |
| `{time_formatted}` | Same as `{time}` in this release |
| `{seconds}` | Remaining seconds |
| `{reason}` | Restart reason or empty-reason fallback |
| `{restart_time}` | Local restart time |
| `{restart_day}` | Local restart day |
| `{timezone}` | Active timezone ID |
| `{entry}` | Raw schedule/config entry |
| `{error}` | Validation or command error |
| `{command}` | Command usage or failed command |
| `{permission}` | Missing permission node |

---

## Commands

| Command | Description | Permission |
|---|---:|---|
| `/zrestart` | Show help | `zrestart.time` |
| `/zrestart time` | Show active or next restart status | `zrestart.time` |
| `/zrestart now <interval> [reason]` | Start a manual restart countdown | `zrestart.admin` |
| `/zrestart delay <interval>` | Delay the active countdown | `zrestart.admin` |
| `/zrestart stop` | Cancel the active countdown | `zrestart.admin` |
| `/zrestart reload` | Reload `config.yml` and `messages.yml` | `zrestart.reload` |

Tab-completion is included for subcommands, interval examples, and common/configured reasons.

---

## Permissions

| Node | Default | Effect |
|---|---:|---|
| `zrestart.time` | `true` | Allows `/zrestart` and `/zrestart time` |
| `zrestart.admin` | `op` | Allows manual now/delay/stop actions |
| `zrestart.reload` | `op` | Allows config/message reload |

---

## Interval formats

| Input | Meaning |
|---|---:|
| `3600` | 3600 seconds |
| `1:30` | 1 hour 30 minutes |
| `0:30` | 30 minutes |
| `30m` | 30 minutes |
| `30M` | 30 minutes |
| `1h30m` | 1 hour 30 minutes |
| `1h 30m` | 1 hour 30 minutes |
| `1h30m15m` | 1 hour 45 minutes |

---

## Common scenarios

**Start a 30-minute manual maintenance restart:**

```text
/zrestart now 30m Maintenance
```

**Delay the active restart by 5 minutes:**

```text
/zrestart delay 5m
```

**Cancel the current restart countdown:**

```text
/zrestart stop
```

**Check what restart is active or scheduled:**

```text
/zrestart time
```

---

## Troubleshooting

- Restart stops instead of restarts: verify `settings.restart-script` in `spigot.yml`.
- PlaceholderAPI placeholders do not change: confirm PlaceholderAPI is installed, enabled, and `settings.papi-placeholders` is `true`.
- A schedule entry is skipped: check day spelling, hour range `0-23`, and minute range `0-59`.
- Reload fails: ZRestart keeps the previous valid runtime config and logs the reason.
- Boss bar never appears: enable `countdown.boss-bar.enabled` and make sure the countdown is inside `show-from`.
- After a plugin update, review `config.yml.bak-*` or `messages.yml.bak-*` if an admin needs to compare pre-update values/comments.

---

## Testing

For a full admin-friendly verification pass, see [TESTING.md](./TESTING.md).

---

## Uninstall

1. Remove the JAR from `plugins/`.
2. Delete `plugins/ZRestart/` if you no longer need the configs.
3. Restart the server.

---

## 👤 Author

- **Zenologia**
- [GitHub Repository]https://github.com/Zenologia/ZRestart
- [License]https://github.com/Zenologia/ZRestart/blob/main/LICENSE
