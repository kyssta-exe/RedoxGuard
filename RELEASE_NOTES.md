# RedoxGuard v1.0.0 - Initial Release

## 🛡️ Advanced Anti-Cheat System

RedoxGuard is a comprehensive anti-cheat solution designed for Minecraft servers running Spigot 1.16-1.21.8. This plugin provides robust detection against various types of cheating while maintaining fair gameplay through ping compensation and configurable thresholds.

## ✨ Features

### Combat Checks
- **KillAura Detection**: Detects players attacking multiple entities simultaneously
- **Reach Check**: Monitors attack distance to prevent extended reach
- **Hitbox Check**: Ensures attacks are within proper hitbox boundaries
- **CrystalAura Detection**: Detects automated crystal placement and breaking
- **AutoCrystal Check**: Monitors crystal interaction timing and patterns
- **AutoAnchor Check**: Detects automated anchor usage
- **TriggerBot Detection**: Identifies instant attack responses

### Movement Checks
- **Fly Detection**: Monitors for impossible flight patterns
- **Speed Check**: Detects movement speed violations with potion effect compensation

### Player Checks
- **AutoTotem Detection**: Monitors automatic totem usage
- **FastBreak Check**: Detects block breaking speed violations
- **FastPlace Check**: Monitors block placement timing
- **Inventory Check**: Tracks inventory manipulation patterns
- **Simulation Check**: Validates player actions against server simulation

## 🔧 Technical Details

- **Compatibility**: Spigot 1.16-1.21.8
- **Java Version**: 17+
- **Performance**: Optimized for minimal server impact
- **Configuration**: Comprehensive YAML-based configuration
- **Logging**: Detailed logging and debugging capabilities

## 📦 Installation

1. Download the `redoxguard-1.0.0.jar` file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure the plugin via `plugins/RedoxGuard/config.yml`

## 🚀 What's New in v1.0.0

### Initial Release Features
- Complete anti-cheat system with 12 different detection modules
- Configurable detection thresholds for each check
- Ping compensation system for fair detection
- Webhook integration for real-time alerts
- Comprehensive logging system
- Bypass permission system for trusted players

### Technical Improvements
- Fixed all compilation issues for Spigot 1.21.1 compatibility
- Updated Bukkit API constants for latest version
- Optimized performance and memory usage
- Added proper error handling and recovery

## 📋 Configuration

The plugin creates a comprehensive configuration file with settings for:
- Individual check enable/disable toggles
- Detection thresholds and sensitivity
- Punishment commands and actions
- Webhook URLs for alerts
- Debug and logging options

## 🛠️ Commands

- `/redoxguard reload` - Reload the plugin configuration
- `/redoxguard info` - Display plugin information
- `/redoxguard debug <player>` - Toggle debug mode for a player

## 📞 Support

For support, bug reports, or feature requests, please visit the GitHub repository or contact the development team.

---

**Note**: This is the initial release of RedoxGuard. Future updates will include additional detection methods, improved performance, and enhanced configuration options. 