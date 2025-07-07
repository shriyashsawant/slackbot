package com.sops.slackbot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sops.slackbot.service.JenkinsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/slack")
public class SlackEventController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JenkinsService jenkinsService;

    @PostMapping("/events")
    public ResponseEntity<String> handleSlackEvent(@RequestBody String payload) {
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.get("type").asText();

            // URL verification from Slack
            if ("url_verification".equals(type)) {
                return ResponseEntity.ok(jsonNode.get("challenge").asText());
            }

            // Event callback (e.g., message sent)
            if ("event_callback".equals(type)) {
                String text = jsonNode.at("/event/text").asText();
                String channel = jsonNode.at("/event/channel").asText();

                if (text.contains("/check_health")) {
                    jenkinsService.triggerJobAndNotify("check_health", channel, "⏳ Checking server health...");
                } else if (text.contains("/logs user-service")) {
                    jenkinsService.triggerJobAndNotify("logs_user_service", channel, "📄 Fetching logs for user-service...");
                } else if (text.contains("/restart frontend")) {
                    jenkinsService.triggerJobAndNotify("restart_frontend", channel, "♻️ Restarting frontend...");
                }
            }

            return ResponseEntity.ok("Handled");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error processing event");
        }
    }
}
