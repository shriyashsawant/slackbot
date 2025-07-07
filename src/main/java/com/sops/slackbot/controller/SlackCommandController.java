package com.sops.slackbot.controller;

import com.sops.slackbot.service.JenkinsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/slack")
public class SlackCommandController {

    @Autowired
    private JenkinsService jenkinsService;

    @PostMapping("/commands")
    public ResponseEntity<String> handleSlashCommand(@RequestParam Map<String, String> params) {
        String command = params.get("command");        // e.g., /check_health
        String text = params.get("text");              // optional args
        String channelId = params.get("channel_id");   // to respond back if needed

        // Run the actual Jenkins logic in a background thread
        new Thread(() -> {
            switch (command) {
                case "/check_health":
                    jenkinsService.triggerJobAndNotify("check_health", channelId, "⏳ Checking server health...");
                    break;

                case "/logs":
                    if (text != null && text.equalsIgnoreCase("user-service")) {
                        jenkinsService.triggerJobAndNotify("logs_user_service", channelId, "📄 Fetching logs for user-service...");
                    } else {
                        try {
                            jenkinsService.sendSlackReply(channelId, "⚠️ Please specify a valid service (e.g., `/logs user-service`).");
                        } catch (Exception ignored) {}
                    }
                    break;

                case "/restart":
                    if (text != null && text.equalsIgnoreCase("frontend")) {
                        jenkinsService.triggerJobAndNotify("restart_frontend", channelId, "♻️ Restarting frontend...");
                    } else {
                        try {
                            jenkinsService.sendSlackReply(channelId, "⚠️ Please specify what to restart (e.g., `/restart frontend`).");
                        } catch (Exception ignored) {}
                    }
                    break;

                default:
                    try {
                        jenkinsService.sendSlackReply(channelId, "❓ Unknown command.");
                    } catch (Exception ignored) {}
            }
        }).start();

        // Immediate 200 OK response to Slack
        return ResponseEntity.ok("✅ Command received. Processing...");
    }
}
