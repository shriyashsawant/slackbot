package com.sops.slackbot.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.stream.Collectors;

@Service
public class ScriptExecutor {

    public String runScript(String scriptName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "./scripts/" + scriptName);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.lines().collect(Collectors.joining("\n"));

        } catch (IOException e) {
            return "Error executing script: " + e.getMessage();
        }
    }
}

