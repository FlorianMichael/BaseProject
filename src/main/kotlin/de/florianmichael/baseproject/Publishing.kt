/*
 * This file is part of BaseProject - https://github.com/FlorianMichael/BaseProject
 * Copyright (C) 2024-2025 FlorianMichael/EnZaXD <florian.michael07@gmail.com> and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.florianmichael.baseproject

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.authentication.http.BasicAuthentication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

/**
 * Sets up Maven publishing using predefined repositories:
 * - Lenni0451's Reposilite
 * - OSSRH (Sonatype)
 *
 * Calls [configureLenni0451Repository], [configureOssrhRepository], and [configureGHPublishing].
 *
 * Required project property:
 * - `publishing_distribution`: GitHub/GitLab URL used for license and SCM metadata
 *
 * @param developerInfo List of developers to include in the POM metadata.
 */
fun Project.setupPublishing(developerInfo: List<DeveloperInfo>) {
    configureLenni0451Repository()
    configureOssrhRepository()
    configureGHPublishing(developerInfo = developerInfo)
}

/**
 * Configures publishing to Lenni0451's Maven Reposilite repository.
 *
 * Chooses `snapshots` or `releases` sub-repo based on project version suffix.
 *
 * Example:
 * - If version ends with `SNAPSHOT`, publishes to `https://maven.lenni0451.net/snapshots`
 * - Otherwise, to `https://maven.lenni0451.net/releases`
 *
 * Requires authentication via basic username/password (credentials block).
 */
fun Project.configureLenni0451Repository() {
    repositories.maven {
        name = "reposilite"
        url = uri(
            "https://maven.lenni0451.net/" +
                if (project.version.toString().endsWith("SNAPSHOT")) "snapshots" else "releases"
        )
        credentials {
            username = findProperty("reposiliteUsername") as String?
            password = findProperty("reposilitePassword") as String?
        }
        authentication {
            create<BasicAuthentication>("basic")
        }
    }
}

/**
 * Configures publishing to Sonatype OSSRH (Maven Central).
 *
 * Automatically selects the snapshot or release URL based on the project version.
 *
 * URLs:
 * - Releases: `https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/`
 * - Snapshots: `https://s01.oss.sonatype.org/content/repositories/snapshots/`
 *
 * Requires authentication (OSSRH credentials via Gradle).
 */
fun Project.configureOssrhRepository() {
    repositories.maven {
        name = "ossrh"
        val releasesUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
        val snapshotsUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
        url = uri(
            if (project.version.toString().endsWith("SNAPSHOT")) snapshotsUrl
            else releasesUrl
        )

        credentials {
            username = findProperty("ossrhUsername") as String?
            password = findProperty("ossrhPassword") as String?
        }
        authentication {
            create<BasicAuthentication>("basic")
        }
    }
}

data class DeveloperInfo(
    val id: String,
    val name: String,
    val email: String
)

/**
 * Convenience wrapper for [configurePublishing] that targets GitHub Maven repositories.
 *
 * Constructs the `distribution` from the given GitHub account and repository name,
 * and generates a license URL assuming it resides at `main/LICENSE`.
 *
 * @param account GitHub username or organization (e.g., "YourName").
 * @param repository GitHub repository name (e.g., "YourRepo").
 * @param license The license name to use (default from `publishing_license` project property or "Apache-2.0").
 * @param developerInfo List of developers to include in the POM metadata.
 */
fun Project.configureGHPublishing(
    account: String = property("publishing_gh_account") as String,
    repository: String = property("publishing_repository") as String,
    license: String = findProperty("publishing_license") as String? ?: "Apache-2.0",
    developerInfo: List<DeveloperInfo>
) {
    val distribution = "github.com/$account/$repository"
    configurePublishing(distribution, license, "https://$distribution/blob/main/LICENSE", developerInfo)
}

/**
 * Configures Maven publishing and signing using the `maven-publish` and `signing` plugins.
 *
 * Publishes the Java component and includes full POM metadata (name, description, license, developers, SCM).
 *
 * Required project property:
 * - `publishing_distribution`: GitHub/GitLab org/repo (e.g. `github.com/YourName/RepoName`)
 *
 * Optional project property:
 * - `publishing_license`: License name to use in the POM (defaults to Apache-2.0)
 * - `publishing_license_url`: URL to license file in the repository (defaults to "https://www.apache.org/licenses/LICENSE-2.0")
 *
 * Also applies GPG signing (signing is optional and controlled by presence of keys).
 * @param distribution The distribution URL for the project (e.g., GitHub/GitLab URL).
 * @param licenseName The name of the license to use in the POM metadata (defaults from `publishing_license` property or "Apache-2.0").
 * @param licenseUrl The URL to the license file in the repository (defaults from `publishing_license_url` property or "https://www.apache.org/licenses/LICENSE-2.0").
 * @param developerInfo List of developers to include in the POM metadata.
 */
fun Project.configurePublishing(
    distribution: String = property("publishing_distribution") as String,
    licenseName: String = findProperty("publishing_license") as String? ?: "Apache-2.0",
    licenseUrl: String = findProperty("publishing_license_url") as String? ?: "https://www.apache.org/licenses/LICENSE-2.0",
    developerInfo: List<DeveloperInfo>
) {
    apply(plugin = "java-library")
    extensions.getByType(JavaPluginExtension::class.java).apply {
        withSourcesJar()
        withJavadocJar()
    }

    apply(plugin = "maven-publish")
    extensions.getByType(PublishingExtension::class.java).apply {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                from(components["java"])

                pom {
                    name.set(artifactId)
                    description.set(project.description)
                    url.set("https://$distribution")
                    licenses {
                        license {
                            name.set(licenseName)
                            url.set(licenseUrl)
                        }
                    }
                    developers {
                        developerInfo.forEach { dev ->
                            developer {
                                id.set(dev.id)
                                name.set(dev.name)
                                email.set(dev.email)
                            }
                        }
                    }
                    scm {
                        connection.set("scm:git:git://$distribution.git")
                        developerConnection.set("scm:git:ssh://$distribution.git")
                        url.set("https://$distribution")
                    }
                }
            }
        }
    }

    apply(plugin = "signing")
    extensions.getByType(SigningExtension::class.java).apply {
        isRequired = false
        sign(extensions.getByType(PublishingExtension::class.java).publications)
    }
}
