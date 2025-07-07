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

        switch (command) {
            case "/check_health":
                jenkinsService.triggerJobAndNotify("check_health", channelId, "⏳ Checking server health...");
                break;

            case "/logs":
                if (text != null && text.equalsIgnoreCase("user-service")) {
                    jenkinsService.triggerJobAndNotify("logs_user_service", channelId, "📄 Fetching logs for user-service...");
                } else {
                    return ResponseEntity.ok("⚠️ Please specify a valid service (e.g., `/logs user-service`).");
                }
                break;

            case "/restart":
                if (text != null && text.equalsIgnoreCase("frontend")) {
                    jenkinsService.triggerJobAndNotify("restart_frontend", channelId, "♻️ Restarting frontend...");
                } else {
                    return ResponseEntity.ok("⚠️ Please specify what to restart (e.g., `/restart frontend`).");
                }
                break;

            default:
                return ResponseEntity.ok("❓ Unknown command.");
        }

        return ResponseEntity.ok("✅ Command received. Processing...");
    }
}
