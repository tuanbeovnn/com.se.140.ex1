package com.devops.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogController {
    private static final String LOG_PATH = "/vstorage/logs.txt";

    @PostMapping(value = "/log", consumes = MediaType.TEXT_PLAIN_VALUE)
    public void appendLog(@RequestBody String body) throws IOException {
        try (FileWriter fw = new FileWriter(LOG_PATH, true)) {
            fw.write(body + "\n");
        }
    }

    @GetMapping(value = "/log", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getLog() throws IOException {
        File file = new File(LOG_PATH);
        if (!file.exists())
            return "";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
