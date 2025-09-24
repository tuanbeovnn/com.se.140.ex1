# Log Erasure Instructions

To erase the persistent log for all services:

## Method 1: Direct file clearing (services can be running)
```bash
echo > ./vstorage/logs.txt
```
This directly clears the local bind-mounted log file.

## Method 2: Using container exec (services must be running)
```bash
docker compose exec storage sh -c 'echo > /vstorage/logs.txt'
```
This clears the log file from inside the running storage container.

## Method 3: Complete volume/directory removal (services must be stopped)
```bash
docker compose down
rm -f ./vstorage/logs.txt
# or
rm -rf ./vstorage
mkdir ./vstorage
```
This completely removes the log file and optionally recreates the directory.

**Note:** Method 1 is the simplest and most reliable approach.
