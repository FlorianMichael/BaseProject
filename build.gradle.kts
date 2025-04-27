plugins {
    `java-library`
    `maven-publish`
    signing
    //idea
}

base {
    group = property("maven_group") as String
    archivesName.set(property("maven_name") as String)
    version = property("maven_version") as String
    description = property("maven_description") as String
}

val library: Configuration by configurations.creating {
    configurations.api.get().extendsFrom(this)
}

// Uncomment to cache changing modules less aggressively
//configurations.all {
//    resolutionStrategy.cacheDynamicVersionsFor(5, TimeUnit.MINUTES)
//    resolutionStrategy.cacheChangingModulesFor(5, TimeUnit.MINUTES)
//}

repositories {
    mavenCentral()
}

dependencies {
    // For unit testing
    //testImplementation(platform("org.junit:junit-bom:5.10.3"))
    //testImplementation("org.junit.jupiter:junit-jupiter")

    // Example dependency
    //implementation("org.apache.commons:commons-lang3:3.14.0")
    //library("org.apache.commons:commons-lang3:3.14.0")
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    // Add all dependencies which are included using "library" to the jar file and exclude the META-INF folder
    dependsOn(library)
    from({
        library.map { zipTree(it) }
    }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
            "Main-Class" to "de.florianmichael.baseproject.Example"
        )
    }

    // Rename the project's license file to LICENSE_<project_name> to avoid conflicts
    from("LICENSE") {
        rename { "LICENSE_${project.name}" }
    }
}

// For unit testing
//tasks.test {
//    useJUnitPlatform()
//    testLogging {
//        events("passed", "skipped", "failed")
//    }
//    maxParallelForks = Runtime.getRuntime().availableProcessors()
//}

// Uncomment to exclude specific folders from IntelliJ indexing
//idea {
//    module {
//        excludeDirs.add(file("run"))
//    }
//}

publishing {
    repositories {
        maven {
            name = "reposilite"
            url = uri("https://maven.lenni0451.net/" + if (version.toString().endsWith("SNAPSHOT")) "snapshots" else "releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
        maven {
            name = "ossrh"
            val releasesUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl)
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set(artifactId)
                description.set(project.description)
                url.set("https://github.com/FlorianMichael/BaseProject")
                licenses {
                    license {
                        name.set("Apache-2.0 license")
                        url.set("https://github.com/FlorianMichael/BaseProject/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("FlorianMichael")
                        name.set("EnZaXD")
                        email.set("florian.michael07@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/FlorianMichael/BaseProject.git")
                    developerConnection.set("scm:git:ssh://github.com/FlorianMichael/BaseProject.git")
                    url.set("https://github.com/FlorianMichael/BaseProject")
                }
            }
        }
    }
}

signing {
    isRequired = false
    sign(configurations.archives.get())
    sign(publishing.publications["maven"])
}

tasks.withType<PublishToMavenRepository>().configureEach {
    dependsOn(tasks.withType<Sign>())
}
