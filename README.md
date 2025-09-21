<div align="center">

# 🛡️ RedoxGuard

**Advanced Anti-Cheat Solution for Minecraft Servers**

*Developed by Kyssta*

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.13--1.21.x-green.svg)](https://minecraft.net/)
[![GitHub release](https://img.shields.io/github/release/kyssta-exe/RedoxGuard.svg)](https://github.com/kyssta-exe/RedoxGuard/releases)

---

</div>

## 📋 Overview

RedoxGuard is a cutting-edge anti-cheat plugin designed to protect your Minecraft server from various forms of cheating and exploitation. Built with performance and accuracy in mind, it provides comprehensive protection while minimizing false positives through advanced detection algorithms and latency compensation.

## ✨ Key Features

### 🎯 Detection Systems
- **🏃 Movement Checks**: Advanced detection for speed hacks, fly hacks, and movement exploits
- **⚔️ Combat Checks**: Comprehensive protection against reach hacks, kill aura, and combat cheats
- **👤 Player Checks**: Monitors inventory manipulation, fast break/place, and player behavior
- **🤖 Automation Detection**: Identifies auto-crystal, auto-totem, trigger bots, and other automation

### 🔧 Advanced Features
- **📡 Latency Compensation**: Smart ping-based adjustments to minimize false positives
- **⚙️ Highly Configurable**: Fine-tune every aspect to match your server's needs
- **👮 Staff Notifications**: Real-time alerts for administrators and moderators
- **📊 Comprehensive Logging**: Detailed violation tracking and analysis
- **🔗 Discord Integration**: Webhook support for external notifications
- **🔄 Version Adaptive**: Automatically adapts features based on server version

## 📋 Requirements

| Component | Version |
|-----------|----------|
| **Java** | 17+ |
| **Server Software** | Bukkit/Spigot/Paper |
| **Minecraft Version** | 1.13 - 1.21.x |

## 🔄 Version Compatibility

RedoxGuard intelligently adapts to your server version:

- **🎯 Smart Feature Detection**: Automatically enables/disables features based on available game mechanics
- **⚓ Respawn Anchors**: AutoAnchor check auto-disabled on pre-1.16 servers
- **🌸 Version-Specific Materials**: FastBreak adapts to handle newer flowers and blocks
- **🛡️ Core Protection**: All essential anti-cheat features work across all supported versions

## 🚀 Quick Start

### Installation
1. **Download** the latest release from [GitHub Releases](https://github.com/kyssta-exe/RedoxGuard/releases)
2. **Place** the JAR file in your server's `plugins/` directory
3. **Restart** your server to generate configuration files
4. **Configure** settings in `plugins/RedoxGuard/config.yml` as needed
5. **Enjoy** enhanced server protection!

### First Time Setup
```yaml
# Essential configuration in config.yml
discord-webhook:
  enabled: true  # Enable Discord notifications
  url: "your-webhook-url-here"

staff-notifications: true  # Alert staff in-game
violation-logging: true    # Log all violations
```

## 💻 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/redoxguard help` | Display help information | `redoxguard.admin` |
| `/redoxguard reload` | Reload configuration files | `redoxguard.admin` |
| `/redoxguard info` | Show plugin version and status | `redoxguard.admin` |
| `/redoxguard checks` | List all available checks | `redoxguard.admin` |
| `/redoxguard toggle <check>` | Enable/disable specific check | `redoxguard.admin` |
| `/redoxguard violations [player]` | View violation history | `redoxguard.admin` |
| `/redoxguard debug` | Toggle debug mode | `redoxguard.admin` |

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|----------|
| `redoxguard.admin` | Full access to all commands and features | OP |
| `redoxguard.notify` | Receive in-game violation notifications | OP |
| `redoxguard.bypass` | Bypass all anti-cheat checks | OP |

> **⚠️ Security Note**: Be cautious when granting bypass permissions. Only trusted staff should have access to these permissions.

## ⚙️ Configuration

### 📁 File Structure
```
plugins/RedoxGuard/
├── config.yml          # Main configuration
├── checks/             # Individual check settings
└── logs/              # Violation logs
```

### 🔗 Discord Integration

Connect RedoxGuard to your Discord server for real-time notifications:

1. **Create Webhook**: Go to Server Settings → Integrations → Webhooks → New Webhook
2. **Copy URL**: Save the webhook URL
3. **Configure Plugin**:
   ```yaml
   discord-webhook:
     enabled: true
     url: "https://discord.com/api/webhooks/your-webhook-url"
   ```
4. **Test**: Trigger a violation to verify notifications work

### 🎛️ Advanced Configuration

- **Violation Thresholds**: Customize when actions are taken
- **Latency Compensation**: Adjust for high-ping players
- **Check Sensitivity**: Fine-tune detection algorithms
- **Exemption Systems**: Configure bypass conditions

## 🛡️ Detection Modules

### 🏃 Movement Checks
| Check | Description | Detects |
|-------|-------------|----------|
| **Speed** | Movement velocity analysis | Speed hacks, bhop, timer |
| **Fly** | Gravity and flight detection | Fly hacks, jetpack, glide |

### ⚔️ Combat Checks
| Check | Description | Detects |
|-------|-------------|----------|
| **Reach** | Attack distance validation | Extended reach, long-range attacks |
| **KillAura** | Multi-target attack patterns | Kill aura, multi-aura |
| **Hitbox** | Entity boundary verification | Hitbox expansion, impossible hits |
| **TriggerBot** | Automated attack detection | Trigger bots, auto-clickers |
| **AutoCrystal** | End crystal automation | Auto crystal placement/detonation |
| **CrystalAura** | Crystal combat patterns | Crystal aura, crystal bot |
| **AutoAnchor** | Respawn anchor automation | Auto anchor (1.16+ only) |

### 👤 Player Behavior Checks
| Check | Description | Detects |
|-------|-------------|----------|
| **Inventory** | Inventory manipulation | Inventory hacks, chest steal |
| **FastBreak** | Block breaking speed | Fast break, insta-break |
| **FastPlace** | Block placement speed | Fast place, scaffold |
| **AutoTotem** | Totem automation | Auto totem, totem aura |
| **Simulation** | Movement prediction | Simulation hacks, prediction |

> **💡 Pro Tip**: Each check can be individually configured and toggled. Adjust sensitivity based on your server's needs and player base.

## 🆘 Support & Community

### 🐛 Bug Reports & Feature Requests
Encountered an issue or have a great idea? We'd love to hear from you!

- **GitHub Issues**: [Report bugs or request features](https://github.com/kyssta-exe/RedoxGuard/issues)
- **Discussions**: Share ideas and get help from the community

### 📚 Documentation
- **Wiki**: Comprehensive guides and tutorials (coming soon)
- **Configuration Examples**: Sample configs for different server types
- **API Documentation**: For developers wanting to integrate

---

## 📄 License

RedoxGuard is open-source software licensed under the **MIT License**.

```
MIT License - Copyright (c) 2025 Kyssta

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files...
```

See the [LICENSE](LICENSE) file for complete terms.

---

<div align="center">

## 👨‍💻 About the Developer

**RedoxGuard** is proudly developed by **Kyssta**

*Dedicated to creating secure and fair gaming environments*

[![GitHub](https://img.shields.io/badge/GitHub-kyssta--exe-black?style=flat&logo=github)](https://github.com/kyssta-exe)

---

### ⭐ Show Your Support

If RedoxGuard helps protect your server, consider giving it a star on GitHub!

**Made with ❤️ for the Minecraft community**

</div>