# Exercise 1: Multi-Service System with Shared Storage

## Repository Content Checklist
- Source code for Service1 (Node.js/TypeScript), Service2 (Java/Spring Boot), and Storage (Java/Spring Boot)
- Dockerfile for each service
- `docker-compose.yml`
- `docker-status.txt` (output of `docker container ls` and `docker network ls` when services are running)
- `Report.pdf` (your report, converted from .md if needed)
- (Optional) `llm.txt`
- **No extra files**

## How to Test the System

1. **Clone the repository:**
   ```sh
   git clone -b exercise1 <your-git-url>
   cd <repo-folder>
   ```

2. **Build and start the system:**
   ```sh
   docker compose up --build -d
   # or, if using older Docker Compose:
   # docker-compose up --build -d
   ```

3. **Wait 10 seconds for services to start.**

4. **Test the status endpoint:**
   ```sh
   curl localhost:8199/status
   ```
   - Each request appends two lines to the log (one from Service1, one from Service2).

5. **Test the log endpoint:**
   ```sh
   curl localhost:8199/log
   ```
   - The output should match the contents of the log file.


6. **Check the log file directly (local file):**
   ```sh
   cat ./vstorage/logs.txt
   ```

**Important:**
- The output of the following two commands should be identical:
  - `cat ./vstorage/logs.txt`
  - `curl localhost:8199/log`
- Both outputs should contain two (2) lines for every `/status` request you have made (one from Service1, one from Service2).

7. **Shut down the system:**
   ```sh
   docker compose down
   ```

8. **Clean up persistent logs (if requested):**
   ```sh
   echo > ./vstorage/logs.txt
   ```

## Notes
- Only Service1 is accessible from outside; Service2 and Storage are internal.
- All logs are persistent and shared via the Docker bind mount.
- The `vstorage` directory should be created automatically, but may need proper permissions.

## Troubleshooting

If you encounter permission errors when the containers try to write to the log file:

```sh
# Set proper permissions on the directory and file
chmod 777 ./vstorage
chmod 666 ./vstorage/logs.txt
```

This ensures that containers running as different users can write to the shared log file.

---
