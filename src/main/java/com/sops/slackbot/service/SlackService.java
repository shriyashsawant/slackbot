package com.sops.slackbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SlackService {

    @Autowired
    private ScriptExecutor scriptExecutor;

    @Autowired
    private JenkinsService jenkinsService;

    public String handleCommand(String command, String userText) {
        switch (command) {
            case "/checkserver":
                return scriptExecutor.runScript("health_check.sh");
            case "/restartfrontend":
                return scriptExecutor.runScript("restart_service.sh");
            case "/logs":
                return scriptExecutor.runScript("fetch_logs.sh");
            default:
                return "Unknown command: " + command;
        }
    }
}
