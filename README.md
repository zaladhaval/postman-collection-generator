# Postman Collection Generator Spring Boot Starter

A Spring Boot starter library that automatically generates Postman Collection v2.0.0 JSON files from your REST API endpoints. This service-only library provides programmatic access to collection generation without imposing any REST endpoints on your application.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [How It Works](#how-it-works)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [Advanced Usage](#advanced-usage)
- [API Reference](#api-reference)
- [Troubleshooting](#troubleshooting)
- [Build from Source](#build-from-source)
- [Contributing](#contributing)

## 🎯 Overview

This library introspects your Spring Boot application's REST endpoints using Spring's `RequestMappingHandlerMapping` and generates a Postman Collection v2.0.0 compatible JSON file. The library provides **only** a service bean (`PostmanCollectionService`) for programmatic access - it does not expose any REST endpoints, giving you complete control over how and when to generate collections.

### What Makes This Library Different?

- **Service-Only Approach**: No REST endpoints imposed on your application
- **Full Control**: You decide when and how to trigger collection generation
- **Auto-Configuration**: Automatically configures itself when added to your project
- **Smart Introspection**: Analyzes `@RequestBody`, `@RequestParam`, and path variables
- **Type-Aware**: Generates appropriate default values for different parameter types

## ✨ Key Features

### Core Capabilities

- ✅ **Automatic Endpoint Discovery** - Discovers all `@RestController` endpoints using Spring's handler mapping
- ✅ **Smart Parameter Analysis** - Extracts and analyzes `@RequestBody` and `@RequestParam` annotations
- ✅ **Type-Aware Default Values** - Generates appropriate defaults for primitives, Strings, Collections, and Maps
- ✅ **Authorization Header Support** - Configurable authorization headers with Postman variable support
- ✅ **Postman Collection v2.0.0** - Standard format compatible with Postman desktop and web
- ✅ **Pretty-Printed JSON** - Human-readable output with proper formatting
- ✅ **Directory Auto-Creation** - Automatically creates output directories if they don't exist

### Technical Features

- ✅ **Spring Boot 3.x Compatible** - Built and tested with Spring Boot 3.5.6
- ✅ **Java 17** - Modern Java features and performance
- ✅ **Conditional Auto-Configuration** - Only activates for web applications
- ✅ **Customizable ObjectMapper** - Use your own Jackson configuration
- ✅ **Zero Code Changes Required** - Works out of the box with sensible defaults
- ✅ **Service-Only Design** - No controllers, no endpoints, just a service bean

## 🔍 How It Works

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Your Spring Boot App                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────┐         ┌────────────────────────┐   │
│  │  Your Controller │────────▶│ PostmanCollectionService│   │
│  │  or Service      │         │                         │   │
│  └──────────────────┘         └────────────────────────┘   │
│                                          │                   │
│                                          ▼                   │
│                          ┌───────────────────────────┐      │
│                          │ RequestMappingHandler     │      │
│                          │ Mapping (Spring)          │      │
│                          └───────────────────────────┘      │
│                                          │                   │
│                                          ▼                   │
│                          ┌───────────────────────────┐      │
│                          │ Endpoint Introspection    │      │
│                          │ & Collection Building     │      │
│                          └───────────────────────────┘      │
│                                          │                   │
│                                          ▼                   │
│                          ┌───────────────────────────┐      │
│                          │ Postman Collection JSON   │      │
│                          │ (collection.json)         │      │
│                          └───────────────────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### Process Flow

1. **Auto-Configuration Activation**
   - Library detects it's in a web application context
   - Checks if `postman.collection.generator.enabled=true` (default)
   - Creates `PostmanCollectionService` bean

2. **Service Injection**
   - You inject `PostmanCollectionService` into your controller/service
   - Call `generateCollection(baseUrl)` when you want to generate

3. **Endpoint Discovery**
   - Service retrieves `RequestMappingHandlerMapping` from Spring context
   - Extracts all registered `RequestMappingInfo` and `HandlerMethod` pairs

4. **Parameter Analysis**
   - For each endpoint, analyzes method parameters
   - Extracts `@RequestBody` fields using reflection
   - Extracts `@RequestParam` with names and default values
   - Generates type-appropriate default values

5. **Collection Building**
   - Creates Postman Collection v2.0.0 structure
   - Builds request items with:
     - HTTP method (GET, POST, PUT, DELETE, etc.)
     - URL with path variables and query parameters
     - Request body with sample JSON (for POST/PUT/PATCH)
     - Authorization headers (if configured)

6. **File Output**
   - Serializes collection to pretty-printed JSON
   - Creates output directory if needed
   - Deletes existing file (if present)
   - Writes new collection file

## 📦 Installation

### Maven

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.collection</groupId>
    <artifactId>postman-collection-generator</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle

Add this dependency to your `build.gradle`:

```gradle
implementation 'com.collection:postman-collection-generator:1.0.0-SNAPSHOT'
```

### Requirements

- **Java**: 17 or higher
- **Spring Boot**: 3.x (tested with 3.5.6)
- **Maven**: 3.6+ or **Gradle**: 7.0+

### Dependencies

The library uses the following dependencies (all managed automatically):

- **Spring Boot Starter Web** - For web application support
- **Jackson Databind** - For JSON serialization
- **Google Guava** 33.0.0-jre - Utility functions
- **Apache Commons Lang3** 3.18.0 - String and reflection utilities
- **Apache Commons Collections4** 4.4 - Collection utilities
- **Apache Commons IO** 2.15.1 - File operations

## 🚀 Quick Start

### Step 1: Add the Dependency

Add the library to your project (see [Installation](#installation)).

### Step 2: Create a Controller

Inject `PostmanCollectionService` and use it:

```java
import com.collection.creator.service.PostmanCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private PostmanCollectionService collectionService;

    @GetMapping("/generate-postman-collection")
    public String generateCollection() {
        try {
            String outputPath = collectionService.generateCollection("http://localhost:8080");
            return "✅ Collection generated at: " + outputPath;
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
}
```

### Step 3: Run Your Application

```bash
mvn spring-boot:run
```

### Step 4: Generate the Collection

```bash
curl http://localhost:8080/api/admin/generate-postman-collection
```

**Output:**
```
✅ Collection generated at: /path/to/your/project/collection.json
```

### Step 5: Import to Postman

1. Open Postman
2. Click **Import** button
3. Select **File** tab
4. Choose the generated `collection.json` file
5. Click **Import**

🎉 **Done!** Your API collection is now available in Postman with all endpoints, parameters, and sample requests.

## ⚙️ Configuration

All configuration is optional. The library works with sensible defaults out of the box.

### Configuration Properties

```properties
# Enable/disable the library (default: true)
postman.collection.generator.enabled=true

# Base URL for all endpoints (optional, can be passed to generateCollection())
postman.collection.generator.base-url=http://localhost:8080

# Output file location
postman.collection.generator.output.directory=./
postman.collection.generator.output.filename=collection.json

# Collection metadata
postman.collection.generator.collection.name=API Collection
postman.collection.generator.collection.schema=https://schema.getpostman.com/json/collection/v2.0.0/collection.json

# Authorization header
postman.collection.generator.authorization.enabled=true
postman.collection.generator.authorization.header-name=Authorization
postman.collection.generator.authorization.header-value={{logintoken}}
postman.collection.generator.authorization.header-type=text
```

### Default Values

| Property | Default Value | Description |
|----------|---------------|-------------|
| `enabled` | `true` | Enable/disable the library |
| `base-url` | `""` (empty) | Base URL for endpoints |
| `output.directory` | `./` | Output directory |
| `output.filename` | `collection.json` | Output filename |
| `collection.name` | `API Collection` | Collection name in Postman |
| `collection.schema` | `https://schema.getpostman.com/json/collection/v2.0.0/collection.json` | Postman schema version |
| `authorization.enabled` | `true` | Add authorization header |
| `authorization.header-name` | `Authorization` | Header name |
| `authorization.header-value` | `{{logintoken}}` | Header value (supports Postman variables) |
| `authorization.header-type` | `text` | Header type |

### Environment-Specific Configuration

#### Development (`application-dev.properties`)

```properties
postman.collection.generator.enabled=true
postman.collection.generator.output.directory=./postman-collections/dev
postman.collection.generator.output.filename=dev-api-collection.json
postman.collection.generator.collection.name=My API - Development
postman.collection.generator.base-url=http://localhost:8080
postman.collection.generator.authorization.header-value=Bearer dev-token-12345
```

#### Staging (`application-staging.properties`)

```properties
postman.collection.generator.enabled=true
postman.collection.generator.output.directory=./postman-collections/staging
postman.collection.generator.output.filename=staging-api-collection.json
postman.collection.generator.collection.name=My API - Staging
postman.collection.generator.base-url=https://staging-api.myapp.com
postman.collection.generator.authorization.header-value=Bearer {{stagingToken}}
```

#### Production (`application-prod.properties`)

```properties
# Disable in production for security
postman.collection.generator.enabled=false
```

## 📚 Usage Examples

### Example 1: Basic Controller Integration

```java
import com.collection.creator.service.PostmanCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private PostmanCollectionService collectionService;

    @GetMapping("/generate-collection")
    public String generateCollection(@RequestParam(required = false) String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:8080";
        }

        try {
            String outputPath = collectionService.generateCollection(baseUrl);
            return "Collection generated successfully at: " + outputPath;
        } catch (Exception e) {
            return "Error generating collection: " + e.getMessage();
        }
    }
}
```

### Example 2: Secured Endpoint with Spring Security

```java
import com.collection.creator.service.PostmanCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class SecuredAdminController {

    @Autowired
    private PostmanCollectionService collectionService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/generate-collection")
    public ResponseEntity<Map<String, String>> generateCollection(
            @RequestBody Map<String, String> request) {

        String baseUrl = request.getOrDefault("baseUrl", "http://localhost:8080");
        Map<String, String> response = new HashMap<>();

        try {
            String outputPath = collectionService.generateCollection(baseUrl);
            response.put("status", "success");
            response.put("path", outputPath);
            response.put("message", "Collection generated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
```

### Example 3: Scheduled Collection Generation

```java
import com.collection.creator.service.PostmanCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ScheduledCollectionGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledCollectionGenerator.class);

    @Autowired
    private PostmanCollectionService collectionService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // Generate collection every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyCollection() {
        logger.info("Starting scheduled collection generation");
        try {
            String path = collectionService.generateCollection(baseUrl);
            logger.info("Collection generated successfully at: {}", path);
        } catch (Exception e) {
            logger.error("Failed to generate scheduled collection", e);
        }
    }

    // Generate collection every hour
    @Scheduled(cron = "0 0 * * * ?")
    public void generateHourlyCollection() {
        logger.info("Starting hourly collection generation");
        try {
            String path = collectionService.generateCollection(baseUrl);
            logger.info("Hourly collection generated at: {}", path);
        } catch (Exception e) {
            logger.error("Failed to generate hourly collection", e);
        }
    }
}
```

### Example 4: Event-Driven Generation (On Application Startup)

```java
import com.collection.creator.service.PostmanCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ApplicationStartupListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);

    @Autowired
    private PostmanCollectionService collectionService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application started - generating Postman collection...");
        try {
            String path = collectionService.generateCollection("http://localhost:8080");
            logger.info("Startup collection generated at: {}", path);
        } catch (Exception e) {
            logger.error("Failed to generate startup collection", e);
        }
    }
}
```

### Example 5: Service Layer Integration

```java
import com.collection.creator.service.PostmanCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiDocumentationService {

    @Autowired
    private PostmanCollectionService collectionService;

    public String generateApiDocumentation(String environment) {
        String baseUrl = switch (environment.toLowerCase()) {
            case "dev" -> "http://localhost:8080";
            case "staging" -> "https://staging-api.myapp.com";
            case "prod" -> "https://api.myapp.com";
            default -> throw new IllegalArgumentException("Unknown environment: " + environment);
        };

        try {
            return collectionService.generateCollection(baseUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate API documentation for " + environment, e);
        }
    }

    public void generateForAllEnvironments() {
        for (String env : List.of("dev", "staging", "prod")) {
            try {
                String path = generateApiDocumentation(env);
                System.out.println(env + " collection: " + path);
            } catch (Exception e) {
                System.err.println("Failed for " + env + ": " + e.getMessage());
            }
        }
    }
}
```

### Example 6: CI/CD Integration

```java
import com.collection.creator.service.PostmanCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CiCdCollectionGenerator {

    @Autowired
    private PostmanCollectionService collectionService;

    @Value("${ci.generate-collection:false}")
    private boolean generateInCi;

    @Value("${ci.base-url:http://localhost:8080}")
    private String baseUrl;

    @EventListener(ApplicationReadyEvent.class)
    public void generateForCi() {
        if (generateInCi) {
            try {
                String path = collectionService.generateCollection(baseUrl);
                System.out.println("CI Collection generated: " + path);
                // Could upload to artifact repository here
                // uploadToArtifactory(path);
            } catch (Exception e) {
                System.err.println("CI collection generation failed: " + e.getMessage());
                System.exit(1); // Fail the build
            }
        }
    }
}
```

## 🛠️ Advanced Usage

### Custom ObjectMapper Configuration

The library uses a default `ObjectMapper` for JSON serialization. You can provide your own:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfiguration {

    @Bean
    @Primary
    public ObjectMapper customObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Exclude null values
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Pretty print
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Custom date format
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        return mapper;
    }
}
```

### Conditional Generation Based on Profile

```java
import com.collection.creator.service.PostmanCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "staging"}) // Only active in dev and staging
public class ProfileBasedGenerator {

    @Autowired
    private PostmanCollectionService collectionService;

    @EventListener(ApplicationReadyEvent.class)
    public void generateOnStartup() {
        try {
            collectionService.generateCollection("http://localhost:8080");
        } catch (Exception e) {
            // Log but don't fail the application
            System.err.println("Collection generation failed: " + e.getMessage());
        }
    }
}
```

### Dynamic Output Path

```java
import com.collection.creator.service.PostmanCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class VersionedCollectionService {

    @Autowired
    private PostmanCollectionService collectionService;

    public String generateVersionedCollection(String baseUrl) {
        // Generate with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        // Note: You'll need to configure output path in properties
        // or modify the service to accept output path parameter
        try {
            return collectionService.generateCollection(baseUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate versioned collection", e);
        }
    }
}
```

## 📖 API Reference

### PostmanCollectionService

The main service class for generating Postman collections.

#### Method: `generateCollection(String apiPostFix)`

Generates a Postman collection and saves it to the configured output location.

**Parameters:**
- `apiPostFix` (String): Base URL to prepend to all endpoints. If `postman.collection.generator.base-url` is configured, that value takes precedence.

**Returns:**
- `String`: Absolute path to the generated collection file

**Throws:**
- `IOException`: If file writing fails

**Example:**
```java
@Autowired
private PostmanCollectionService service;

public void generate() throws IOException {
    String path = service.generateCollection("http://localhost:8080");
    System.out.println("Generated at: " + path);
}
```

### PostmanCollectionProperties

Configuration properties class.

#### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable/disable the library |
| `baseUrl` | String | `""` | Base URL for endpoints |
| `output.directory` | String | `"./"` | Output directory |
| `output.filename` | String | `"collection.json"` | Output filename |
| `collection.name` | String | `"API Collection"` | Collection name |
| `collection.schema` | String | `"https://schema.getpostman.com/json/collection/v2.0.0/collection.json"` | Schema URL |
| `authorization.enabled` | boolean | `true` | Add auth header |
| `authorization.headerName` | String | `"Authorization"` | Header name |
| `authorization.headerValue` | String | `"{{logintoken}}"` | Header value |
| `authorization.headerType` | String | `"text"` | Header type |

## 🔧 Troubleshooting

### Issue 1: Service Bean Not Found

**Error:**
```
NoSuchBeanDefinitionException: No qualifying bean of type 'PostmanCollectionService'
```

**Possible Causes:**
1. Library is disabled in configuration
2. Application is not a web application
3. Required classes not on classpath

**Solutions:**

1. Check if library is enabled:
```properties
postman.collection.generator.enabled=true
```

2. Ensure you have `spring-boot-starter-web` dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

3. Check application logs for auto-configuration messages:
```
INFO: Postman Collection Generator Auto-Configuration activated
INFO: Creating PostmanCollectionService bean for programmatic access
```

### Issue 2: File Not Created

**Error:**
```
IOException: No such file or directory
```

**Solutions:**

1. Ensure output directory exists or use absolute path:
```properties
postman.collection.generator.output.directory=/home/user/postman-collections
```

2. Check file permissions:
```bash
mkdir -p ./postman-collections
chmod 755 ./postman-collections
```

3. Verify the path in logs - the service logs the output path

### Issue 3: Empty Collection

**Problem:** Generated collection has no endpoints

**Possible Causes:**
1. No `@RestController` classes in application
2. Controllers not scanned by Spring
3. Application not fully started when generation triggered

**Solutions:**

1. Ensure you have REST controllers:
```java
@RestController
@RequestMapping("/api")
public class MyController {
    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
```

2. Check component scanning:
```java
@SpringBootApplication
@ComponentScan(basePackages = "com.myapp")
public class Application {
    // ...
}
```

3. If generating on startup, use `ApplicationReadyEvent` instead of `@PostConstruct`

### Issue 4: Authorization Header Not Added

**Problem:** Authorization header missing from requests

**Solution:**

Enable authorization in configuration:
```properties
postman.collection.generator.authorization.enabled=true
postman.collection.generator.authorization.header-name=Authorization
postman.collection.generator.authorization.header-value=Bearer {{token}}
```

### Issue 5: IOException - Permission Denied

**Error:**
```
IOException: Permission denied
```

**Solutions:**

1. Check directory permissions:
```bash
chmod 755 ./postman-collections
```

2. Run application with appropriate user permissions

3. Use a directory where the application has write access:
```properties
postman.collection.generator.output.directory=/tmp/postman-collections
```

### Issue 6: Wrong Artifact ID

**Problem:** Dependency not found

**Solution:**

Use the correct artifact ID from `pom.xml`:
```xml
<dependency>
    <groupId>com.collection</groupId>
    <artifactId>postman-collection-generator</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Note:** The artifact ID is `postman-collection-generator`, not `postman-collection-generator-spring-boot-starter`.

## 🏗️ Build from Source

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

### Build Steps

1. **Clone the repository:**
```bash
git clone <repository-url>
cd creator
```

2. **Build the project:**
```bash
mvn clean install
```

**Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time:  6.018 s
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

3. **Verify installation:**
```bash
ls -lh ~/.m2/repository/com/collection/postman-collection-generator/1.0.0-SNAPSHOT/
```

**Expected files:**
- `postman-collection-generator-1.0.0-SNAPSHOT.jar` - Main library
- `postman-collection-generator-1.0.0-SNAPSHOT-sources.jar` - Source code
- `postman-collection-generator-1.0.0-SNAPSHOT-javadoc.jar` - Javadoc
- `postman-collection-generator-1.0.0-SNAPSHOT.pom` - POM file

4. **Run tests:**
```bash
mvn test
```

### Project Structure

```
creator/
├── src/
│   ├── main/
│   │   ├── java/com/collection/creator/
│   │   │   ├── autoconfigure/
│   │   │   │   └── PostmanCollectionAutoConfiguration.java
│   │   │   ├── config/
│   │   │   │   └── PostmanCollectionProperties.java
│   │   │   ├── model/
│   │   │   │   ├── Body.java
│   │   │   │   ├── Header.java
│   │   │   │   ├── Info.java
│   │   │   │   ├── Item.java
│   │   │   │   ├── Options.java
│   │   │   │   ├── PostmanRequest.java
│   │   │   │   ├── Raw.java
│   │   │   │   └── Request.java
│   │   │   └── service/
│   │   │       └── PostmanCollectionService.java
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   ├── spring/
│   │       │   │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   │       │   └── spring-configuration-metadata.json
│   │       └── application.properties.example
│   └── test/
│       └── java/com/collection/creator/
│           └── CreatorApplicationTests.java
├── pom.xml
└── README.md
```

### Running Tests

The project includes 4 tests:

1. **contextLoads** - Verifies Spring context loads
2. **autoConfigurationShouldLoadService** - Verifies service bean is created
3. **autoConfigurationShouldLoadProperties** - Verifies properties are loaded
4. **propertiesShouldHaveDefaultValues** - Verifies default property values

```bash
mvn test
```

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

### How to Contribute

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Make your changes**
4. **Run tests**
   ```bash
   mvn test
   ```
5. **Commit your changes**
   ```bash
   git commit -m 'Add some amazing feature'
   ```
6. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
7. **Open a Pull Request**

### Development Guidelines

- Follow existing code style
- Add tests for new features
- Update documentation
- Ensure all tests pass
- Keep commits atomic and well-described

### Reporting Issues

If you find a bug or have a feature request:

1. Check if the issue already exists
2. Create a new issue with:
   - Clear title and description
   - Steps to reproduce (for bugs)
   - Expected vs actual behavior
   - Environment details (Java version, Spring Boot version, etc.)

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🎯 Best Practices

1. **Disable in Production** - Unless specifically needed, disable the library in production:
   ```properties
   postman.collection.generator.enabled=false
   ```

2. **Secure Your Endpoints** - Add authentication/authorization to endpoints that use the service:
   ```java
   @PreAuthorize("hasRole('ADMIN')")
   @GetMapping("/generate-collection")
   public String generate() { ... }
   ```

3. **Use Environment-Specific Configuration** - Different settings for dev/staging/prod

4. **Handle Exceptions** - Always wrap service calls in try-catch blocks

5. **Log Generation Events** - Use proper logging for debugging and monitoring

6. **Version Your Collections** - Include version numbers or timestamps in filenames

7. **Automate Generation** - Use scheduled tasks or CI/CD integration for regular updates

8. **Test Before Deploying** - Verify generated collections work in Postman

## 🚀 What's Next?

Planned features for future releases:

- [ ] Support for request/response examples
- [ ] Postman environment variables generation
- [ ] Test scripts in collections
- [ ] Pre-request scripts support
- [ ] GraphQL endpoint support
- [ ] OpenAPI/Swagger integration
- [ ] Collection versioning
- [ ] Multiple output formats (OpenAPI, Swagger, etc.)
- [ ] Filtering options (include/exclude endpoints)
- [ ] Custom header configuration
- [ ] Request/response body examples from Javadoc

## 📞 Support

Need help? Here's how to get support:

1. **Check the Documentation** - Read this README thoroughly
2. **Review Examples** - Check the [Usage Examples](#usage-examples) section
3. **Check Troubleshooting** - See the [Troubleshooting](#troubleshooting) section
4. **Search Issues** - Look for similar issues on GitHub
5. **Create an Issue** - If you can't find a solution, create a new issue

## 🙏 Acknowledgments

This library uses the following open-source projects:

- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [Jackson](https://github.com/FasterXML/jackson) - JSON processing
- [Google Guava](https://github.com/google/guava) - Core libraries
- [Apache Commons](https://commons.apache.org/) - Utility libraries

---

**Made with ❤️ for the Spring Boot community**

**Star ⭐ this repository if you find it helpful!**


