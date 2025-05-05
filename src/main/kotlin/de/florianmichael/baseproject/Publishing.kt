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
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.authentication.http.BasicAuthentication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

/**
 * Sets up Maven publishing using predefined repositories:
 * - Lenni0451's Reposilite
 * - OSSRH (Sonatype)
 *
 * Calls [configureLenni0451Repository], [configureOssrhRepository], and [configurePublishing].
 *
 * Required project property:
 * - `publishing_distribution`: GitHub/GitLab URL used for license and SCM metadata
 */
fun Project.setupPublishing() {
    configureLenni0451Repository()
    configureOssrhRepository()
    configurePublishing()
}

/**
 * Sets up Maven publishing using the ViaVersion repository.
 *
 * Calls [configureViaRepository] to register the `https://repo.viaversion.com/` Maven repo,
 * and [configurePublishing] to configure the Maven publishing plugin with POM metadata and signing.
 *
 * Required project property:
 * - `publishing_distribution`: Repository URL used in generated POM (e.g., GitHub/GitLab URL)
 *
 * Authentication:
 * - Requires basic authentication for the ViaVersion repository (set via Gradle credentials).
 */
fun Project.setupViaPublishing() {
    configureViaRepository()
    configurePublishing()
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
        credentials(PasswordCredentials::class)
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
        credentials(PasswordCredentials::class)
        authentication {
            create<BasicAuthentication>("basic")
        }
    }
}

/**
 * Configures a Maven repository for ViaVersion's repo.
 *
 * URL: `https://repo.viaversion.com/`
 *
 * Requires basic authentication.
 */
fun Project.configureViaRepository() {
    repositories.maven {
        name = "Via"
        url = uri("https://repo.viaversion.com/")
        credentials(PasswordCredentials::class)
        authentication {
            create<BasicAuthentication>("basic")
        }
    }
}

/**
 * Configures Maven publishing and signing using the `maven-publish` and `signing` plugins.
 *
 * Publishes the Java component and includes full POM metadata (name, description, license, developers, SCM).
 *
 * Required project property:
 * - `publishing_distribution`: GitHub/GitLab org/repo (e.g. `github.com/YourName/RepoName`)
 *
 * Optional arguments:
 * - `license`: License name to use in the POM (defaults to Apache-2.0)
 * - `licenseUrl`: URL to license file in the repository
 *
 * Also applies GPG signing (signing is optional and controlled by presence of keys).
 */
fun Project.configurePublishing(
    distribution: String = property("publishing_distribution") as String,
    license: String = "Apache-2.0 license",
    licenseUrl: String = "https://$distribution/blob/main/LICENSE"
) {
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
                            name.set(license)
                            url.set(licenseUrl)
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
