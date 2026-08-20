# Hometask — Load Velocity Limits

A Spring Boot service that processes fund-load attempts from a file and decides whether it should be accepted or declined based on a customer's load
velocity (how much/how often they're loading money).

## Input

Input is a line-delimited JSON file (one load attempt per line):

```json
{"id":"12345","customer_id":"528","load_amount":"$3318.47","time":"2000-01-01T00:00:00Z"}
```

## Configuration

```yaml
hometask:
  input-path: classpath:input.txt
  output-path: output/output.txt

velocity:
  limits:
    max-daily-load-amount: 5000.00
    max-weekly-load-amount: 20000.00
    max-daily-load-count: 3
```

## Running the service

```bash
./gradlew bootRun
```

Reads src/main/resources/input.txt and writes to output/output.txt by default.

## Testing

```bash
./gradlew test
```
