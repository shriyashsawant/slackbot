package com.sops.slackbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SlackService {
    @Value("${jenkins.url:}")
private String jenkinsUrl;

@Value("${slack.bot.token:}")
private String slackBotToken;

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

            case "/triggerjob":
                // Example: trigger Jenkins job named "user-service"
                try {
                    jenkinsService.triggerJobAndNotify("user-service", "#general", "✅ Jenkins job triggered by /triggerjob");
                    return "🟢 Jenkins job 'user-service' triggered successfully.";
                } catch (Exception e) {
                    return "❌ Failed to trigger Jenkins job: " + e.getMessage();
                }

            default:
                return "Unknown command: " + command;
        }
    }
}
