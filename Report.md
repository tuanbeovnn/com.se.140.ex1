# Exercise 1: Multi-Service System Report

## Basic Platform Information

**Hardware/Virtual Machine:**
- MacBook Mini (Apple Silicon M1/M2)
- macOS (macOS Sequoia or later)

**Software Versions:**
- Docker: 27.x (Docker Desktop for Mac)
- Docker Compose: v2.x (integrated with Docker)

## System Architecture

### Services Overview
The system consists of three interconnected services:

1. **Service1** (Node.js/TypeScript)
   - **IP Address:** 172.18.0.4
   - **Port:** 8199 (externally accessible)
   - **Role:** Entry point and proxy service

2. **Service2** (Java/Spring Boot)
   - **IP Address:** 172.18.0.3
   - **Port:** 8080 (internal only)
   - **Role:** Status analysis service

3. **Storage** (Java/Spring Boot)
   - **IP Address:** 172.18.0.2
   - **Port:** 8080 (internal only)
   - **Role:** Persistent storage service

### Network Architecture
```
Internet/localhost:8199
         ↓
   [Service1] ←→ [Service2]
         ↓              ↓
     [Storage] ←←←←←←←←←←
```

### Storage Architecture
1. **Bind Mount Storage (./vstorage):**
   - Local directory bind-mounted to all containers
   - File: `./vstorage/logs.txt` (host) → `/vstorage/logs.txt` (containers)

2. **Service-based Storage:**
   - REST API provided by Storage service
   - Endpoints: POST/GET `/log`

## Status Record Analysis

### Measurements Captured

**Service1 Record Format:**
`YYYY-MM-DDTHH:MM:SS.sssZ: uptime X.XX hours, free disk in root: XXXX MBytes`

**Service2 Record Format:**
`YYYY-MM-DDTHH:MM:SS.ssssssssZ: uptime X.XX hours, free disk in root: XXXX MBytes`

### What is Actually Measured

1. **Uptime:**
   - **Service1:** System uptime (entire container host uptime)
   - **Service2:** JVM uptime (Java application runtime)
   - **Relevance:** Service2's measurement is more accurate for the actual service uptime

2. **Free Disk Space:**
   - Both services measure the root filesystem's available space
   - Measured in the container's root mount point (`/`)
   - **Relevance:** Shows available space in the container's filesystem

### Improvements Recommended

1. **Uptime Measurement:**
   - Service1 should measure container uptime, not host uptime
   - Consider using process start time instead of system uptime

2. **Disk Space Measurement:**
   - Could be more specific about which mount point is being measured
   - Consider measuring application-specific directories

3. **Timestamp Consistency:**
   - Both services now use ISO 8601 UTC format (✅ Fixed)
   - Service2 provides more precision in nanoseconds

## Persistent Storage Analysis

### Storage Solution 1: Bind Mount (./vstorage)

**Advantages:**
- ✅ Simple to implement and understand
- ✅ Direct file system access from host
- ✅ Easy to inspect and manage (`cat ./vstorage/logs.txt`)
- ✅ Shared access between containers and host
- ✅ Persistence across container restarts
- ✅ No special Docker volume management needed

**Disadvantages:**
- ❌ Direct file system access (bad practice as mentioned in requirements)
- ❌ Host-dependent (requires specific directory structure)
- ❌ Concurrent write issues without proper locking
- ❌ Limited scalability (single file)
- ❌ Host filesystem permissions may cause issues

### Storage Solution 2: Storage Service (REST API)

**Advantages:**
- ✅ Proper API abstraction
- ✅ Centralized data management
- ✅ Can implement access control and validation
- ✅ Better scalability potential
- ✅ Network-based, location-independent
- ✅ Uses proper HTTP content types (text/plain)

**Disadvantages:**
- ❌ More complex implementation
- ❌ Network latency for storage operations
- ❌ Single point of failure
- ❌ Requires additional service management

### Recommendation
The Storage service approach is architecturally superior and should be preferred for production systems, while the volume approach is acceptable for learning and development environments.

## Teacher's Instructions for Cleaning Persistent Storage

### Method 1: Direct File Clearing (Recommended)
```bash
echo > ./vstorage/logs.txt
```

### Method 2: Using Container Exec (Services Running)
```bash
docker compose exec storage sh -c 'echo > /vstorage/logs.txt'
```

### Method 3: Complete File/Directory Removal
```bash
docker compose down
rm -f ./vstorage/logs.txt
# or completely remove and recreate directory:
rm -rf ./vstorage && mkdir ./vstorage
```

## Testing Verification

The system correctly implements all required functionality:

1. ✅ **External Access:** Only Service1 accessible via localhost:8199
2. ✅ **Status Endpoint:** Returns combined status from both services
3. ✅ **Log Endpoint:** Returns persistent log content
4. ✅ **Dual Logging:** Both vStorage and Storage service receive identical data
5. ✅ **ISO 8601 Timestamps:** Proper UTC timestamp format implemented
6. ✅ **Two Lines per Request:** Each `/status` call generates exactly 2 log lines

**Verification Commands:**
```bash
curl localhost:8199/status  # Returns status from both services
curl localhost:8199/log     # Returns complete log history
cat ./vstorage/logs.txt      # Returns local file content
```

**Critical Requirement Verification:**
```bash
# These two commands MUST return identical output:
cat ./vstorage/logs.txt
curl localhost:8199/log
```

Both outputs should show 2 lines per status request made.

## Challenges and Problems Encountered

### Main Difficulties

1. **Timestamp Format Implementation:**
   - Initially missed the ISO 8601 UTC requirement
   - Had to implement proper timestamp formatting in both Node.js and Java
   - **Solution:** Used JavaScript `Date.toISOString()` and Java `Instant.now().toString()`

2. **Disk Space Calculation:**
   - Alpine Linux containers use different `df` command options
   - **Solution:** Used `df -m / | tail -1 | awk '{print $4}'` for Alpine compatibility

3. **Container Communication:**
   - Ensuring proper service discovery between containers
   - **Solution:** Used Docker Compose service names as hostnames

4. **Dual Storage Implementation:**
   - Coordinating between file-based and API-based storage
   - **Solution:** Implemented both storage mechanisms in parallel

5. **File Permission Issues:**
   - Bind mount permissions prevented containers from writing to log file
   - Error: "EACCES: permission denied, open '/vstorage/logs.txt'"
   - **Solution:** Set proper permissions with `chmod 777 ./vstorage` and `chmod 666 ./vstorage/logs.txt`

### Technical Issues Resolved

1. **Docker Compose Version Warning:**
   - Removed deprecated `version` field from docker-compose.yml

2. **Build Dependencies:**
   - Ensured proper `depends_on` configuration in docker-compose.yml

3. **Port Configuration:**
   - Correctly exposed only Service1 port (8199) to host

4. **Storage Implementation Change:**
   - Changed from Docker volume to bind mount for direct host file access
   - Required for teacher verification: `cat ./vstorage/logs.txt` must work

5. **Permission Management:**
   - Documented required permissions for bind mount functionality
   - Added troubleshooting instructions for common permission errors

## Conclusion

The system successfully meets all specified requirements:
- Three services implemented in different languages (TypeScript, Java)
- Proper network isolation with only Service1 externally accessible
- Dual persistent storage implementation
- Correct request flow and proxy functionality
- ISO 8601 timestamp format compliance
- Complete Docker containerization

The implementation demonstrates understanding of microservices architecture, container orchestration, and persistent storage patterns in a distributed system.