<p align="center">
  <img src="icons/allay-chan-480x.png" alt="Allay IDE Plugin Logo" width="200" height="200">
</p>

<h1 align="center">Allay IDE Plugin</h1>

<p align="center">
  IntelliJ IDEA plugin that provides comprehensive support for Allay Gradle Plugin development.
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#installation">Installation</a> •
  <a href="#usage">Usage</a> •
  <a href="#development">Development</a> •
  <a href="#license">License</a>
</p>

## Features

### Project Creation
- One-click Allay plugin project creation with wizard
- Pre-configured Gradle build structure
- Java 21 support with automatic JDK selection
- Git repository initialization (optional)

### Kotlin DSL Support
- Smart code completion for `allay {}` configuration block
- Syntax highlighting and validation
- Quick documentation for Allay API

### Development Tools
- Automatic version checking for Allay API updates
- Plugin descriptor validation and inspections
- Integration with IntelliJ IDEA's project system

## Installation

### From Source

1. Clone the repository:
```bash
git clone https://github.com/yourusername/AllayIdePlugin.git
cd AllayIdePlugin
```

2. Build the plugin:
```bash
./gradlew build
```

3. Install the plugin:
   - Open IntelliJ IDEA
   - Go to `Settings/Preferences` → `Plugins` → `⚙️` → `Install Plugin from Disk...`
   - Select the generated JAR file from `build/libs/`

### From JetBrains Marketplace

Coming soon.

## Usage

### Creating a New Allay Plugin Project

1. Open IntelliJ IDEA
2. Select `File` → `New` → `Project`
3. Choose `Allay Plugin` from the generators list
4. Configure your project:
   - **Name**: Your plugin name
   - **Location**: Project directory
   - **JDK**: Select or download JDK 21
   - **Group ID**: Java package name (e.g., `com.example`)
   - **Plugin Version**: Initial version (e.g., `1.0.0`)
   - **Plugin Description**: Brief description of your plugin
   - **Author**: Your name
   - **Allay API Version**: Target Allay API version
   - **Main Class Name**: Entry point class name
5. Click `Create`

### Working with Allay DSL

The plugin provides intelligent code completion and validation for Allay's Gradle DSL:

```kotlin
allay {
    api = "0.15.0"
    apiOnly = true
    
    plugin {
        name = "MyPlugin"
        entrance = ".MyPlugin"
        version = "1.0.0"
        description = "My awesome Allay plugin"
        authors += "YourName"
        api = ">= 0.15.0"
    }
}
```

### Version Checking

The plugin automatically checks for new Allay API versions on project startup and notifies you when updates are available.

## Development

### Requirements

- IntelliJ IDEA 2025.1.4.1 or later
- JDK 21
- Gradle 8.9 or later

### Project Structure

```
AllayIdePlugin/
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── miroshka/
│       │       └── allayideplugin/
│       │           ├── project/          # Project generation
│       │           ├── version/          # Version checking
│       │           ├── AllayDslAnnotator.kt
│       │           ├── AllayDslCompletionContributor.kt
│       │           └── AllayReferenceContributor.kt
│       └── resources/
│           ├── META-INF/
│           │   └── plugin.xml           # Plugin descriptor
│           └── icons/
├── build.gradle.kts
└── README.md
```

### Building from Source

```bash
./gradlew build
```

### Running in Development Mode

```bash
./gradlew runIde
```

This will launch a new IntelliJ IDEA instance with the plugin installed.

### Testing

```bash
./gradlew test
```

## Technology Stack

- **Kotlin**: Main development language
- **IntelliJ Platform SDK**: Plugin development framework
- **Gradle**: Build system
- **Kotlin Serialization**: JSON parsing for version checking

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Related Projects

- [AllayMC](https://github.com/AllayMC) - The Allay Minecraft Server
- [AllayGradle](https://github.com/AllayMC/AllayGradle) - Gradle plugin for Allay plugin development

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- AllayMC Team for creating the Allay server platform
- JetBrains for the IntelliJ Platform SDK
- The Minecraft community

---

<p align="center">Made for the Allay community</p>

