# RedoxGuard

An advanced anticheat solution for Minecraft servers, developed by Kyssta.

## Features

- **Movement Checks**: Detects speed hacks, fly hacks, and other movement-related cheats
- **Combat Checks**: Detects reach hacks, kill aura, and other combat-related cheats
- **Player Checks**: Detects inventory hacks and other player-related cheats
- **Latency Compensation**: Adjusts checks based on player ping to reduce false positives
- **Customizable Configuration**: Fine-tune all checks to suit your server's needs
- **Staff Notifications**: Alerts staff members when cheats are detected
- **Violation Logging**: Logs all violations for later review

## Requirements

- Java 17 or higher
- Bukkit/Spigot/Paper server (1.13 - 1.21.x)

## Version Compatibility

RedoxGuard is designed to work across Minecraft versions 1.13 to 1.21.x with automatic feature adaptation:

- **Respawn Anchors**: The AutoAnchor check is automatically disabled on versions before 1.16 where respawn anchors don't exist
- **Newer Flowers**: FastBreak check automatically adapts to handle flowers added in newer versions (Cornflower, Lily of the Valley, Wither Rose)
- **Core Features**: All core anti-cheat functionality works consistently across all supported versions

## Installation

1. Download the latest version of RedoxGuard from [GitHub](https://github.com/kyssta-exe/RedoxGuard/releases)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Edit the configuration files in `plugins/RedoxGuard` to suit your needs

## Commands

- `/redoxguard help` - Show help message
- `/redoxguard reload` - Reload the configuration
- `/redoxguard info` - Show plugin information
- `/redoxguard checks` - List all checks
- `/redoxguard toggle <check>` - Toggle a check on/off
- `/redoxguard violations [player]` - Show violations for a player
- `/redoxguard debug` - Toggle debug mode

## Permissions

- `redoxguard.admin` - Access to all RedoxGuard commands
- `redoxguard.notify` - Receive notifications when cheats are detected
- `redoxguard.bypass` - Bypass all RedoxGuard checks

## Configuration

The main configuration file is located at `plugins/RedoxGuard/config.yml`. Check-specific configurations are located in the `plugins/RedoxGuard/checks` directory.

### Discord Integration

RedoxGuard can send violation alerts to a Discord channel using webhooks. To set this up:

1. Create a webhook in your Discord server (Server Settings > Integrations > Webhooks)
2. Copy the webhook URL
3. In `config.yml`, set `discord-webhook.enabled` to `true` and `discord-webhook.url` to your webhook URL

### Default Checks

#### Movement Checks
- **Speed**: Detects players moving faster than allowed
- **Fly**: Detects players flying without permission

#### Combat Checks
- **Reach**: Detects players attacking from too far away
- **KillAura**: Detects players using kill aura hacks
- **Hitbox**: Detects players hitting entities outside their bounding box
- **AutoCrystal**: Detects players using auto crystal hacks
- **AutoAnchor**: Detects players using auto anchor/anchor aura hacks (Minecraft 1.16+)
- **CrystalAura**: Detects players using crystal aura hacks
- **TriggerBot**: Detects players using trigger bot hacks (automatically attacks when crosshair is over an entity)

#### Player Checks
- **Inventory**: Detects inventory hacks
- **FastBreak**: Detects players breaking blocks too quickly (with version-specific material handling)
- **FastPlace**: Detects players placing blocks too quickly (with special handling for end portal frames and crystals)
- **AutoTotem**: Detects players using auto totem hacks
- **Simulation**: Detects players using simulation/prediction hacks

## Support

If you encounter any issues or have suggestions, please open an issue on [GitHub](https://github.com/kyssta-exe/RedoxGuard/issues).

## License

RedoxGuard is licensed under the MIT License. See the LICENSE file for details.

## Credits

- Developed by Kyssta