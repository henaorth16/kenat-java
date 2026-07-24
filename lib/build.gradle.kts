plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "io.github.henaorth16"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// ─── Java toolchain ──────────────────────────────────────────────────────────

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

// ─── Compile / Javadoc options ───────────────────────────────────────────────

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        source = "21"
    }
}

// ─── Publishing ──────────────────────────────────────────────────────────────
// Targets: Maven Central via the Sonatype Central Portal.
//
// Required properties (put in ~/.gradle/gradle.properties, NOT committed):
//   mavenCentralUsername   = <portal token username>
//   mavenCentralPassword   = <portal token password>
//   signing.keyId          = <last 8 hex chars of GPG key>
//   signing.password       = <GPG passphrase>
//   signing.secretKeyRingFile = /home/<you>/.gnupg/secring.gpg
//
// Or use in-memory signing (good for CI):
//   signing.key            = <full armored PGP private key block>
//   signing.password       = <GPG passphrase>
//
// Publish a release:
//   ./gradlew publishToSonatype         # upload → manual "Publish" on portal
// ─────────────────────────────────────────────────────────────────────────────

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId    = "io.github.henaorth16"
            artifactId = "kenat"
            version    = project.version.toString()

            pom {
                name        = "Kenat"
                description = "Ethiopian calendar library — date conversion, holidays, fasting periods, Bahire Hasab, Geez numerals, and more."
                url         = "https://github.com/henaorth16/kenat-java"
                inceptionYear = "2026"

                licenses {
                    license {
                        name         = "MIT License"
                        url          = "https://opensource.org/licenses/MIT"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        id   = "henaorth16"
                        name = "Henok"
                        url  = "https://github.com/henaorth16"
                    }
                }
                scm {
                    url                 = "https://github.com/henaorth16/kenat-java"
                    connection          = "scm:git:git://github.com/henaorth16/kenat-java.git"
                    developerConnection = "scm:git:ssh://git@github.com/henaorth16/kenat-java.git"
                }
            }
        }
    }

    repositories {
        // Sonatype Central Portal staging endpoint (used by the Portal OSSRH Staging API).
        // After upload, log in to https://central.sonatype.com/publishing/deployments and
        // click "Publish" to release to Maven Central.
        maven {
            name = "SonatypeCentralPortal"
            url  = uri("https://central.sonatype.com/api/v1/publisher/upload")
            credentials {
                username = findProperty("mavenCentralUsername") as String? ?: System.getenv("MAVEN_CENTRAL_USERNAME")
                password = findProperty("mavenCentralPassword") as String? ?: System.getenv("MAVEN_CENTRAL_PASSWORD")
            }
        }
        // Convenience: local Maven cache for smoke-testing before uploading
        maven {
            name = "local"
            url  = uri(layout.buildDirectory.dir("local-repo"))
        }
    }
}

// ─── GPG signing (required by Maven Central) ─────────────────────────────────
// Credentials in ~/.gradle/gradle.properties:
//   signing.keyId=5E6B2388
//   signing.password=<passphrase>
//   signing.secretKeyRingFile=/home/hena/.gnupg/secring.gpg
//
// For CI, use in-memory signing instead:
//   signing.key=<full armored PGP PRIVATE KEY BLOCK>
//   signing.password=<passphrase>
// ─────────────────────────────────────────────────────────────────────────────
signing {
    val signingKey: String? = findProperty("signing.key") as String?
    if (signingKey != null) {
        // In-memory key (CI-friendly)
        val signingPassword = findProperty("signing.password") as String? ?: ""
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
    // If signing.key is absent, falls back to signing.keyId +
    // signing.secretKeyRingFile from gradle.properties automatically.
    sign(publishing.publications["mavenJava"])
}
