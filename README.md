# BaseProject
A Gradle convention plugin for streamlined project setup and publishing.

## Contact
If you encounter any issues, please report them on the [issue tracker](https://github.com/FlorianMichael/BaseProject/issues).
If you just want to talk or need help with BaseProject, feel free to join my [Discord](https://florianmichael.de/discord).

## Basic Setup
Most of the functions provided by this plugin will default to global set properties in the `gradle.properties` file. However, you can override them for special handling (e.g., `setupJava(version = 17)` instead of using the `project_jvm_version` property).

### Kotlin DSL Example:
Add the plugin to your **`settings.gradle.kts`** file:

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("de.florianmichael.baseproject.BaseProject") version "<version>"
    }
}
```

Update the **`build.gradle.kts`** file:

```kotlin
import de.florianmichael.baseproject.*

plugins {
    id("de.florianmichael.baseproject.BaseProject")
}

// Sets up common configurations: project metadata, Java toolchain, and compiler options
setupProject()
```

Set project properties in the **`gradle.properties`** file:

```properties
project_jvm_version=17

project_group=com.example
project_name=ExampleProject
project_version=1.0.0-SNAPSHOT
project_description=Example Java project.
```

The above can be omitted if you prefer, as the plugin will only set the available properties.

---

### Groovy DSL Example:
Add the plugin to your **`settings.gradle`** file:

```groovy
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id 'de.florianmichael.baseproject.BaseProject' version '<version>'
    }
}
```

Update the **`build.gradle`** file:

```groovy
plugins {
    id 'de.florianmichael.baseproject.BaseProject'
}

// Sets up common configurations: project metadata, Java toolchain, and compiler options
setupProject()
```

Set project properties in the **`gradle.properties`** file:

```properties
project_jvm_version=17

project_group=com.example
project_name=ExampleProject
project_version=1.0.0-SNAPSHOT
project_description=Example Java project.
```

---

## Publishing
### Kotlin DSL Example:
Add the following to your **`build.gradle.kts`** (below the `setupProject` call):

```kotlin
// Maven Central repository definition
configureOssrhRepository()

// Sets publishing metadata
configurePublishing(listOf(DeveloperInfo("<username>", "<full name>", "<contact mail>")))
```

### Groovy DSL Example:
Add the following to your **`build.gradle`** (below the `setupProject` call):

```groovy
// Maven Central repository definition
configureOssrhRepository()

// Sets publishing metadata
configurePublishing([new DeveloperInfo('<username>', '<full name>', '<contact mail>')])
```

---

### Signing and Publishing Credentials
#### **`gradle.properties` in your `.gradle` folder:**
```properties
# Signing properties; required for OSSRH
signing.keyId=<the last 8 digits of your key id>
signing.password=<your key password>
signing.secretKeyRingFile=<path to your keyring file>

# Maven Central credentials
ossrhUsername=<your account name>
ossrhPassword=<your account password> # This is an access token nowadays
```

#### **`gradle.properties` in the project folder:**

```properties
publishing_distribution=github.com/FlorianMichael/ExampleProject
publishing_license=Apache-2.0
```

You can also add your own repository server. The `configurePublishing` function will always sign the publications if the signing properties are present.

---

## Fabric Setup
### Kotlin DSL Example:
Add the following to your **`build.gradle.kts`** (below the `setupProject` call):

```kotlin
// Fabric client setup with Mojang mappings. If an .accesswidener file with the project name is present, it will also be loaded.
setupFabric()

// Yarn mappings (optional):
// setupFabric(yarnMapped())
```

Set the required versions in **`gradle.properties`**:

```properties
minecraft_version=1.21.5
parchment_version=1.21.5:2025.04.19
# yarn_mappings_version=1.21.5+build.1
fabric_loader_version=0.16.14
```

---

### Groovy DSL Example:
Add the following to your **`build.gradle`** (below the `setupProject` call):

```groovy
// Fabric client setup with Mojang mappings. If an .accesswidener file with the project name is present, it will also be loaded.
setupFabric()

// Yarn mappings (optional):
// setupFabric(yarnMapped())
```

Set the required versions in **`gradle.properties`**:

```properties
minecraft_version=1.21.5
parchment_version=1.21.5:2025.04.19
# yarn_mappings_version=1.21.5+build.1
fabric_loader_version=0.16.14
```

---

## Moving On
You can use additional utilities like automatic dependency shading:

### Kotlin DSL Example:
```kotlin
val library = configureShadedDependencies()

dependencies {
    library("group:artifact:version")
}
```

### Groovy DSL Example:
```groovy
def library = configureShadedDependencies()

dependencies {
    library 'group:artifact:version'
}
```

For more utilities and detailed documentation, please refer to the Kotlin files and methods in the plugin, which include detailed KotlinDoc comments.
