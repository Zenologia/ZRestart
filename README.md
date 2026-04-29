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

---

Full list of timezones: 

Africa/Abidjan
Africa/Accra
Africa/Addis_Ababa
Africa/Algiers
Africa/Asmara
Africa/Asmera
Africa/Bamako
Africa/Bangui
Africa/Banjul
Africa/Bissau
Africa/Blantyre
Africa/Brazzaville
Africa/Bujumbura
Africa/Cairo
Africa/Casablanca
Africa/Ceuta
Africa/Conakry
Africa/Dakar
Africa/Dar_es_Salaam
Africa/Djibouti
Africa/Douala
Africa/El_Aaiun
Africa/Freetown
Africa/Gaborone
Africa/Harare
Africa/Johannesburg
Africa/Juba
Africa/Kampala
Africa/Khartoum
Africa/Kigali
Africa/Kinshasa
Africa/Lagos
Africa/Libreville
Africa/Lome
Africa/Luanda
Africa/Lubumbashi
Africa/Lusaka
Africa/Malabo
Africa/Maputo
Africa/Maseru
Africa/Mbabane
Africa/Mogadishu
Africa/Monrovia
Africa/Nairobi
Africa/Ndjamena
Africa/Niamey
Africa/Nouakchott
Africa/Ouagadougou
Africa/Porto-Novo
Africa/Sao_Tome
Africa/Timbuktu
Africa/Tripoli
Africa/Tunis
Africa/Windhoek
America/Adak
America/Anchorage
America/Anguilla
America/Antigua
America/Araguaina
America/Argentina/Buenos_Aires
America/Argentina/Catamarca
America/Argentina/ComodRivadavia
America/Argentina/Cordoba
America/Argentina/Jujuy
America/Argentina/La_Rioja
America/Argentina/Mendoza
America/Argentina/Rio_Gallegos
America/Argentina/Salta
America/Argentina/San_Juan
America/Argentina/San_Luis
America/Argentina/Tucuman
America/Argentina/Ushuaia
America/Aruba
America/Asuncion
America/Atikokan
America/Atka
America/Bahia
America/Bahia_Banderas
America/Barbados
America/Belem
America/Belize
America/Blanc-Sablon
America/Boa_Vista
America/Bogota
America/Boise
America/Buenos_Aires
America/Cambridge_Bay
America/Campo_Grande
America/Cancun
America/Caracas
America/Catamarca
America/Cayenne
America/Cayman
America/Chicago
America/Chihuahua
America/Ciudad_Juarez
America/Coral_Harbour
America/Cordoba
America/Costa_Rica
America/Coyhaique
America/Creston
America/Cuiaba
America/Curacao
America/Danmarkshavn
America/Dawson
America/Dawson_Creek
America/Denver
America/Detroit
America/Dominica
America/Edmonton
America/Eirunepe
America/El_Salvador
America/Ensenada
America/Fort_Nelson
America/Fort_Wayne
America/Fortaleza
America/Glace_Bay
America/Godthab
America/Goose_Bay
America/Grand_Turk
America/Grenada
America/Guadeloupe
America/Guatemala
America/Guayaquil
America/Guyana
America/Halifax
America/Havana
America/Hermosillo
America/Indiana/Indianapolis
America/Indiana/Knox
America/Indiana/Marengo
America/Indiana/Petersburg
America/Indiana/Tell_City
America/Indiana/Vevay
America/Indiana/Vincennes
America/Indiana/Winamac
America/Indianapolis
America/Inuvik
America/Iqaluit
America/Jamaica
America/Jujuy
America/Juneau
America/Kentucky/Louisville
America/Kentucky/Monticello
America/Knox_IN
America/Kralendijk
America/La_Paz
America/Lima
America/Los_Angeles
America/Louisville
America/Lower_Princes
America/Maceio
America/Managua
America/Manaus
America/Marigot
America/Martinique
America/Matamoros
America/Mazatlan
America/Mendoza
America/Menominee
America/Merida
America/Metlakatla
America/Mexico_City
America/Miquelon
America/Moncton
America/Monterrey
America/Montevideo
America/Montreal
America/Montserrat
America/Nassau
America/New_York
America/Nipigon
America/Nome
America/Noronha
America/North_Dakota/Beulah
America/North_Dakota/Center
America/North_Dakota/New_Salem
America/Nuuk
America/Ojinaga
America/Panama
America/Pangnirtung
America/Paramaribo
America/Phoenix
America/Port-au-Prince
America/Port_of_Spain
America/Porto_Acre
America/Porto_Velho
America/Puerto_Rico
America/Punta_Arenas
America/Rainy_River
America/Rankin_Inlet
America/Recife
America/Regina
America/Resolute
America/Rio_Branco
America/Rosario
America/Santa_Isabel
America/Santarem
America/Santiago
America/Santo_Domingo
America/Sao_Paulo
America/Scoresbysund
America/Shiprock
America/Sitka
America/St_Barthelemy
America/St_Johns
America/St_Kitts
America/St_Lucia
America/St_Thomas
America/St_Vincent
America/Swift_Current
America/Tegucigalpa
America/Thule
America/Thunder_Bay
America/Tijuana
America/Toronto
America/Tortola
America/Vancouver
America/Virgin
America/Whitehorse
America/Winnipeg
America/Yakutat
America/Yellowknife
Antarctica/Casey
Antarctica/Davis
Antarctica/DumontDUrville
Antarctica/Macquarie
Antarctica/Mawson
Antarctica/McMurdo
Antarctica/Palmer
Antarctica/Rothera
Antarctica/South_Pole
Antarctica/Syowa
Antarctica/Troll
Antarctica/Vostok
Arctic/Longyearbyen
Asia/Aden
Asia/Almaty
Asia/Amman
Asia/Anadyr
Asia/Aqtau
Asia/Aqtobe
Asia/Ashgabat
Asia/Ashkhabad
Asia/Atyrau
Asia/Baghdad
Asia/Bahrain
Asia/Baku
Asia/Bangkok
Asia/Barnaul
Asia/Beirut
Asia/Bishkek
Asia/Brunei
Asia/Calcutta
Asia/Chita
Asia/Choibalsan
Asia/Chongqing
Asia/Chungking
Asia/Colombo
Asia/Dacca
Asia/Damascus
Asia/Dhaka
Asia/Dili
Asia/Dubai
Asia/Dushanbe
Asia/Famagusta
Asia/Gaza
Asia/Harbin
Asia/Hebron
Asia/Ho_Chi_Minh
Asia/Hong_Kong
Asia/Hovd
Asia/Irkutsk
Asia/Istanbul
Asia/Jakarta
Asia/Jayapura
Asia/Jerusalem
Asia/Kabul
Asia/Kamchatka
Asia/Karachi
Asia/Kashgar
Asia/Kathmandu
Asia/Katmandu
Asia/Khandyga
Asia/Kolkata
Asia/Krasnoyarsk
Asia/Kuala_Lumpur
Asia/Kuching
Asia/Kuwait
Asia/Macao
Asia/Macau
Asia/Magadan
Asia/Makassar
Asia/Manila
Asia/Muscat
Asia/Nicosia
Asia/Novokuznetsk
Asia/Novosibirsk
Asia/Omsk
Asia/Oral
Asia/Phnom_Penh
Asia/Pontianak
Asia/Pyongyang
Asia/Qatar
Asia/Qostanay
Asia/Qyzylorda
Asia/Rangoon
Asia/Riyadh
Asia/Saigon
Asia/Sakhalin
Asia/Samarkand
Asia/Seoul
Asia/Shanghai
Asia/Singapore
Asia/Srednekolymsk
Asia/Taipei
Asia/Tashkent
Asia/Tbilisi
Asia/Tehran
Asia/Tel_Aviv
Asia/Thimbu
Asia/Thimphu
Asia/Tokyo
Asia/Tomsk
Asia/Ujung_Pandang
Asia/Ulaanbaatar
Asia/Ulan_Bator
Asia/Urumqi
Asia/Ust-Nera
Asia/Vientiane
Asia/Vladivostok
Asia/Yakutsk
Asia/Yangon
Asia/Yekaterinburg
Asia/Yerevan
Atlantic/Azores
Atlantic/Bermuda
Atlantic/Canary
Atlantic/Cape_Verde
Atlantic/Faeroe
Atlantic/Faroe
Atlantic/Jan_Mayen
Atlantic/Madeira
Atlantic/Reykjavik
Atlantic/South_Georgia
Atlantic/St_Helena
Atlantic/Stanley
Australia/ACT
Australia/Adelaide
Australia/Brisbane
Australia/Broken_Hill
Australia/Canberra
Australia/Currie
Australia/Darwin
Australia/Eucla
Australia/Hobart
Australia/LHI
Australia/Lindeman
Australia/Lord_Howe
Australia/Melbourne
Australia/NSW
Australia/North
Australia/Perth
Australia/Queensland
Australia/South
Australia/Sydney
Australia/Tasmania
Australia/Victoria
Australia/West
Australia/Yancowinna
Brazil/Acre
Brazil/DeNoronha
Brazil/East
Brazil/West
CET
CST6CDT
Canada/Atlantic
Canada/Central
Canada/Eastern
Canada/Mountain
Canada/Newfoundland
Canada/Pacific
Canada/Saskatchewan
Canada/Yukon
Chile/Continental
Chile/EasterIsland
Cuba
EET
EST5EDT
Egypt
Eire
Etc/GMT
Etc/GMT+0
Etc/GMT+1
Etc/GMT+10
Etc/GMT+11
Etc/GMT+12
Etc/GMT+2
Etc/GMT+3
Etc/GMT+4
Etc/GMT+5
Etc/GMT+6
Etc/GMT+7
Etc/GMT+8
Etc/GMT+9
Etc/GMT-0
Etc/GMT-1
Etc/GMT-10
Etc/GMT-11
Etc/GMT-12
Etc/GMT-13
Etc/GMT-14
Etc/GMT-2
Etc/GMT-3
Etc/GMT-4
Etc/GMT-5
Etc/GMT-6
Etc/GMT-7
Etc/GMT-8
Etc/GMT-9
Etc/GMT0
Etc/Greenwich
Etc/UCT
Etc/UTC
Etc/Universal
Etc/Zulu
Europe/Amsterdam
Europe/Andorra
Europe/Astrakhan
Europe/Athens
Europe/Belfast
Europe/Belgrade
Europe/Berlin
Europe/Bratislava
Europe/Brussels
Europe/Bucharest
Europe/Budapest
Europe/Busingen
Europe/Chisinau
Europe/Copenhagen
Europe/Dublin
Europe/Gibraltar
Europe/Guernsey
Europe/Helsinki
Europe/Isle_of_Man
Europe/Istanbul
Europe/Jersey
Europe/Kaliningrad
Europe/Kiev
Europe/Kirov
Europe/Kyiv
Europe/Lisbon
Europe/Ljubljana
Europe/London
Europe/Luxembourg
Europe/Madrid
Europe/Malta
Europe/Mariehamn
Europe/Minsk
Europe/Monaco
Europe/Moscow
Europe/Nicosia
Europe/Oslo
Europe/Paris
Europe/Podgorica
Europe/Prague
Europe/Riga
Europe/Rome
Europe/Samara
Europe/San_Marino
Europe/Sarajevo
Europe/Saratov
Europe/Simferopol
Europe/Skopje
Europe/Sofia
Europe/Stockholm
Europe/Tallinn
Europe/Tirane
Europe/Tiraspol
Europe/Ulyanovsk
Europe/Uzhgorod
Europe/Vaduz
Europe/Vatican
Europe/Vienna
Europe/Vilnius
Europe/Volgograd
Europe/Warsaw
Europe/Zagreb
Europe/Zaporozhye
Europe/Zurich
GB
GB-Eire
GMT
GMT0
Greenwich
Hongkong
Iceland
Indian/Antananarivo
Indian/Chagos
Indian/Christmas
Indian/Cocos
Indian/Comoro
Indian/Kerguelen
Indian/Mahe
Indian/Maldives
Indian/Mauritius
Indian/Mayotte
Indian/Reunion
Iran
Israel
Jamaica
Japan
Kwajalein
Libya
MET
MST7MDT
Mexico/BajaNorte
Mexico/BajaSur
Mexico/General
NZ
NZ-CHAT
Navajo
PRC
PST8PDT
Pacific/Apia
Pacific/Auckland
Pacific/Bougainville
Pacific/Chatham
Pacific/Chuuk
Pacific/Easter
Pacific/Efate
Pacific/Enderbury
Pacific/Fakaofo
Pacific/Fiji
Pacific/Funafuti
Pacific/Galapagos
Pacific/Gambier
Pacific/Guadalcanal
Pacific/Guam
Pacific/Honolulu
Pacific/Johnston
Pacific/Kanton
Pacific/Kiritimati
Pacific/Kosrae
Pacific/Kwajalein
Pacific/Majuro
Pacific/Marquesas
Pacific/Midway
Pacific/Nauru
Pacific/Niue
Pacific/Norfolk
Pacific/Noumea
Pacific/Pago_Pago
Pacific/Palau
Pacific/Pitcairn
Pacific/Pohnpei
Pacific/Ponape
Pacific/Port_Moresby
Pacific/Rarotonga
Pacific/Saipan
Pacific/Samoa
Pacific/Tahiti
Pacific/Tarawa
Pacific/Tongatapu
Pacific/Truk
Pacific/Wake
Pacific/Wallis
Pacific/Yap
Poland
Portugal
ROK
Singapore
SystemV/AST4
SystemV/AST4ADT
SystemV/CST6
SystemV/CST6CDT
SystemV/EST5
SystemV/EST5EDT
SystemV/HST10
SystemV/MST7
SystemV/MST7MDT
SystemV/PST8
SystemV/PST8PDT
SystemV/YST9
SystemV/YST9YDT
Turkey
UCT
US/Alaska
US/Aleutian
US/Arizona
US/Central
US/East-Indiana
US/Eastern
US/Hawaii
US/Indiana-Starke
US/Michigan
US/Mountain
US/Pacific
US/Samoa
UTC
Universal
W-SU
WET
Zulu

## Configuration (`config.yml`)

Example:

```yaml
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