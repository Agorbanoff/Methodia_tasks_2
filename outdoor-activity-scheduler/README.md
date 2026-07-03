# Outdoor Activity Scheduler

Spring Boot backend application that reads activity preferences from Excel, checks WeatherAPI forecasts, and writes suitable outdoor activity intervals to an Excel output file. There is no REST API and no frontend.

## Input And Output

Input file:

```text
input/activity-input.xlsx
```

Output file:

```text
output/activity-results.xlsx
```

If `app.excel.create-template-if-missing=true`, the app creates `input/activity-input.xlsx` automatically when it does not exist.

## Excel Input Format

The input workbook has two sheets: `Settings` and `Sports`.

### Settings

Columns: `field | value`

| field | value |
| --- | --- |
| location | Sofia |
| daysAhead | 2 |

`location` is required and is used for the WeatherAPI request, Excel output flow, email text, and Google Calendar event location. Blank or missing `daysAhead` uses `app.days-ahead` from `application.yml`.

### Sports

Each row after the header is one sport.

| name | displayName | minTempC | maxTempC | maxGustKph | maxChanceOfRain | requiresDaylight | minConsecutiveHours | preferWeekend | preferCloudAbove |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| badminton | Федербал | 12 | 28 | 18 | 30 | true | 2 | true | 30 |
| football | Футбол | 5 | 32 | 30 | 60 | true | 2 | true | |

`preferCloudAbove` may be blank; blank is treated as `null`.

## Modes

Modes are controlled by `application.yml`, not command-line flags.

```yaml
app:
  excel:
    input-path: input/activity-input.xlsx
    output-path: output/activity-results.xlsx
    run-on-startup: true
    dry-run: true
    create-template-if-missing: true
  schedule:
    enabled: false
    cron: "0 0 * * * *"
```

Once mode: `app.excel.run-on-startup=true`.

Dry-run mode: `app.excel.dry-run=true`. The app reads Excel, calculates results, writes Excel output, and adds a `PlannedActions` sheet. It does not send email, create calendar events, or write notification history.

Watch mode: `app.schedule.enabled=true`. Spring runs `ScheduledActivityChecker` with `app.schedule.cron`; each trigger reads Excel and writes output. If dry-run is false, notifications are processed.

## Notifications

When `app.excel.dry-run=false`, notification processing is allowed:

- `app.email.enabled=true` sends email for new intervals.
- `app.calendar.enabled=true` creates Google Calendar events for new intervals.
- `NotificationHistoryService` prevents duplicate notifications.

If both email and calendar are disabled, notification history is not updated.

## Environment And Secrets

Required environment variable:

```powershell
$env:WEATHER_API_KEY = "your-weatherapi-key"
```

Google Calendar credentials and tokens, if calendar integration is enabled:

```text
credentials/credentials.json
tokens/
```

Do not commit real API keys, Google credentials, OAuth tokens, `.env` files, logs, notification history, or generated Excel output files.

## Run

From this module directory:

```powershell
mvn spring-boot:run
```

If Maven is not on PATH:

```powershell
D:\apache-maven-3.9.14\bin\mvn.cmd spring-boot:run
```

Build:

```powershell
mvn clean package
```

Run the packaged jar:

```powershell
java -jar target/outdoor-activity-scheduler-1.0-SNAPSHOT.jar
```
