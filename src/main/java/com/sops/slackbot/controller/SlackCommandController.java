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
    String command = params.get("command");
    String text = params.get("text");
    String channelId = params.get("channel_id");

    // Handle /status separately (does not need to run inside thread)
    if ("/status".equals(command)) {
        if (text != null && !text.isEmpty()) {
            new Thread(() -> {
                String jobResult = jenkinsService.getLastBuildStatus(text.trim());
                try {
                    jenkinsService.sendSlackReply(channelId, "📊 Job `" + text + "` last build: " + jobResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            return ResponseEntity.ok("🔍 Checking status of job: " + text);
        } else {
            return ResponseEntity.ok("⚠️ Please provide a Jenkins job name. Example: `/status check_health`");
        }
    }

    // Run the rest of the Jenkins triggers in background
    new Thread(() -> {
        switch (command) {
            case "/check_health":
                jenkinsService.triggerJobAndNotify("check_health", channelId, "⏳ Checking server health...");
                break;

            case "/logs":
                if ("user-service".equalsIgnoreCase(text)) {
                    jenkinsService.triggerJobAndNotify("logs_user_service", channelId, "📄 Fetching logs for user-service...");
                } else {
                    try {
                        jenkinsService.sendSlackReply(channelId, "⚠️ Please specify a valid service (e.g., `/logs user-service`).");
                    } catch (Exception ignored) {}
                }
                break;

            case "/restart":
                if ("frontend".equalsIgnoreCase(text)) {
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

    return ResponseEntity.ok("✅ Command received. Processing...");
}
}
