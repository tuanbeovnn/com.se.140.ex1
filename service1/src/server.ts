
import axios from 'axios';
import { exec } from 'child_process';
import express from 'express';
import os from 'os';
import { promisify } from 'util';

const app = express();
const execAsync = promisify(exec);


// Function to get info from Service2
const getService2Info = async (): Promise<string> => {
    try {
        const response = await axios.get('http://service2:8080/status');
        return response.data;
    } catch (error: any) {
        console.error('Error fetching Service2 info:', error.message);
        return `Error fetching Service2 info: ${error.message}`;
    }
};

// Function to log to Storage service
const logToStorage = async (record: string) => {
    try {
        await axios.post('http://storage:8080/log', record, {
            headers: { 'Content-Type': 'text/plain' },
        });
    } catch (err) {
        console.error('Failed to write to Storage service:', err);
    }
};


// /status endpoint
app.get('/status', async (req: express.Request, res: express.Response) => {
    try {
        // 1. Service1 analyses its status and creates the record
        const uptimeInSeconds = os.uptime();
        const hours = (uptimeInSeconds / 3600).toFixed(2);

        // Get disk space - use df command that works in Alpine Linux
        let freeDiskMB = 0;
        try {
            const diskSpaceRaw = await execAsync("df -m / | tail -1 | awk '{print $4}'");
            freeDiskMB = parseInt(diskSpaceRaw.stdout.trim(), 10);
        } catch (error) {
            console.error('Error getting disk space:', error);
            freeDiskMB = 0;
        }

        const timestamp = new Date().toISOString();
        const record1 = `${timestamp}: uptime ${hours} hours, free disk in root: ${freeDiskMB} MBytes`;

        // 2. Service1 sends the created record to Storage (HTTP POST Storage)
        await logToStorage(record1);

        // 4. Service1 forwards the request to Service2 (/status)
        const record2 = await getService2Info();

        // 9. Service1 combines the records and returns as response
        return res.type('text/plain').send(`${record1}\n${record2}`);
    } catch (error: any) {
        return res.status(500).type('text/plain').send('Unable to retrieve status');
    }
});

// /log endpoint
app.get('/log', async (req: express.Request, res: express.Response) => {
    try {
        // 1. Service1 forwards the request to Storage
        const response = await axios.get('http://storage:8080/log', {
            headers: { 'Accept': 'text/plain' },
        });
        // 2. Returns the content of the log in text/plain
        return res.type('text/plain').send(response.data);
    } catch (error: any) {
        return res.status(500).type('text/plain').send('Unable to retrieve log');
    }
});


app.listen(8199, () => {
    console.log('Service1 is running on port 8199');
});
