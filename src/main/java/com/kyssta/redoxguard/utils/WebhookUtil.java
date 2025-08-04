package com.kyssta.redoxguard.utils;

import com.kyssta.redoxguard.RedoxGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class WebhookUtil {

    /**
     * Send a violation alert to the Discord webhook
     * @param playerName The name of the player
     * @param checkName The name of the check
     * @param details Additional details about the violation
     * @param vl The violation level
     */
    public static void sendWebhookAlert(String playerName, String checkName, String details, int vl) {
        // Check if webhook is enabled and URL is set
        if (RedoxGuard.getInstance() == null || 
            RedoxGuard.getInstance().getConfigManager() == null || 
            !RedoxGuard.getInstance().getConfigManager().isWebhookEnabled() || 
            RedoxGuard.getInstance().getConfigManager().getWebhookUrl().isEmpty()) {
            return;
        }
        
        // Get webhook URL
        String webhookUrl = RedoxGuard.getInstance().getConfigManager().getWebhookUrl();
        
        // Create message
        String message = playerName + " failed " + checkName + " " + details + " (VL: " + vl + ")";
        
        // Create JSON payload
        String jsonPayload = String.format("{\"content\":\"%s\",\"username\":\"RedoxGuard\",\"avatar_url\":\"https://i.imgur.com/4M34hi2.png\"}", 
                                          escapeJson(message));
        
        // Send webhook asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "RedoxGuard/1.0");
                connection.setDoOutput(true);
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                int responseCode = connection.getResponseCode();
                if (responseCode != 204) {
                    LogUtil.warning("Failed to send webhook alert: HTTP " + responseCode);
                }
                
                connection.disconnect();
            } catch (Exception e) {
                LogUtil.warning("Failed to send webhook alert: " + e.getMessage());
            }
        });
    }
    
    /**
     * Escape special characters in a string for JSON
     * @param input The input string
     * @return The escaped string
     */
    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}