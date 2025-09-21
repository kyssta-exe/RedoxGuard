package com.kyssta.redoxguard.utils;

import com.kyssta.redoxguard.RedoxGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for managing Discord webhook integrations in RedoxGuard.
 * <p>
 * This class provides functionality for:
 * <ul>
 *   <li>Sending violation alerts to Discord channels via webhooks</li>
 *   <li>Asynchronous HTTP communication to prevent server lag</li>
 *   <li>JSON payload formatting and escaping for webhook compatibility</li>
 *   <li>Error handling and logging for webhook failures</li>
 *   <li>Configuration-based webhook enabling/disabling</li>
 * </ul>
 * <p>
 * All webhook operations are performed asynchronously using CompletableFuture
 * to ensure that network operations do not block the main server thread.
 * The class integrates with the RedoxGuard configuration system to respect
 * user preferences for webhook notifications.
 * 
 * @author RedoxGuard Team
 * @version 1.0
 * @since 1.0.0
 */
public class WebhookUtil {

    /**
     * Sends a violation alert to the configured Discord webhook asynchronously.
     * <p>
     * This method performs the following operations:
     * <ul>
     *   <li>Validates webhook configuration and availability</li>
     *   <li>Formats the violation message with player and check information</li>
     *   <li>Creates a JSON payload compatible with Discord webhook API</li>
     *   <li>Sends the HTTP POST request asynchronously to prevent server lag</li>
     *   <li>Handles response codes and logs any errors encountered</li>
     * </ul>
     * <p>
     * The webhook alert is only sent when:
     * <ul>
     *   <li>The RedoxGuard instance is available</li>
     *   <li>The ConfigManager is initialized</li>
     *   <li>Webhook functionality is enabled in the configuration</li>
     *   <li>A valid webhook URL is configured</li>
     * </ul>
     * <p>
     * The Discord message includes a custom username "RedoxGuard" and avatar
     * for consistent branding across all webhook notifications.
     * 
     * @param playerName The name of the player who triggered the violation
     * @param checkName The name of the anti-cheat check that was violated
     * @param details Additional contextual information about the violation
     * @param vl The current violation level for this player and check type
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
     * Escapes special characters in a string to ensure JSON compatibility.
     * <p>
     * This method handles the following character escaping:
     * <ul>
     *   <li>Backslashes (\) are escaped to double backslashes (\\)</li>
     *   <li>Double quotes (") are escaped to \"</li>
     *   <li>Newlines (\n) are escaped to \\n</li>
     * </ul>
     * <p>
     * This escaping is essential for preventing JSON parsing errors when
     * violation messages contain special characters that could break the
     * webhook payload structure.
     * 
     * @param input The input string that may contain special characters
     * @return The escaped string safe for inclusion in JSON payloads
     */
    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}