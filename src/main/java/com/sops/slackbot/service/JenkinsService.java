package com.sops.slackbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Service
public class JenkinsService {

    @Value("${jenkins.url}")
    private String JENKINS_URL;

    @Value("${jenkins.user}")
    private String JENKINS_USER;

    @Value("${jenkins.token}")
    private String JENKINS_API_TOKEN;

    @Value("${jenkins.trigger.token}")
    private String JENKINS_JOB_TRIGGER_TOKEN;

    @Value("${slack.bot.token}")
    private String SLACK_BOT_TOKEN;


    


    public void triggerJobAndNotify(String jobName, String channel, String slackMessage) {
        try {
            triggerJenkinsJob(jobName);
            sendSlackReply(channel, slackMessage);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                sendSlackReply(channel, "❌ Failed to trigger Jenkins job: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }

    private void triggerJenkinsJob(String jobName) throws Exception {
        String jobUrl = JENKINS_URL + "/job/" + jobName + "/build?token=" + JENKINS_JOB_TRIGGER_TOKEN;
        HttpURLConnection conn = (HttpURLConnection) new URL(jobUrl).openConnection();
        conn.setRequestMethod("POST");

        String auth = JENKINS_USER + ":" + JENKINS_API_TOKEN;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

        conn.getResponseCode(); // Trigger job
    }

   public void sendSlackReply(String channel, String message) throws Exception {
    URL url = new URL("https://slack.com/api/chat.postMessage");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Authorization", "Bearer " + SLACK_BOT_TOKEN);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setDoOutput(true);

    String jsonPayload = String.format("{\"channel\":\"%s\",\"text\":\"%s\"}", channel, message);

    System.out.println("🟡 Posting to Slack...");
    System.out.println("Channel: " + channel);
    System.out.println("Payload: " + jsonPayload);

    try (OutputStream os = conn.getOutputStream()) {
        byte[] input = jsonPayload.getBytes("utf-8");
        os.write(input, 0, input.length);
    }

    int responseCode = conn.getResponseCode();
    System.out.println("🔵 Slack API response code: " + responseCode);

    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        System.out.println("🟢 Slack API response body: " + content.toString());
    } catch (Exception ex) {
        System.out.println("🔴 Failed to read Slack response: " + ex.getMessage());
        }
    }
    
    public String getLastBuildStatus(String jobName) {
    try {
        String apiUrl = JENKINS_URL + "/job/" + jobName + "/lastBuild/api/json";
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();

        String auth = JENKINS_USER + ":" + JENKINS_API_TOKEN;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            return "Failed to fetch build status (HTTP " + responseCode + ")";
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder json = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            json.append(inputLine);
        }
        in.close();

        String jsonStr = json.toString();
        if (jsonStr.contains("\"result\":\"")) {
            return jsonStr.split("\"result\":\"")[1].split("\"")[0];
        } else {
            return "Build result not found";
        }

    } catch (Exception e) {
        e.printStackTrace();
        return "Error: " + e.getMessage();
    }
}


}

