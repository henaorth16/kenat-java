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
        maven {
            name = "SonatypeCentralPortal"
            url  = uri("https://central.sonatype.com/api/v1/publisher/upload")
            credentials {
                username = findProperty("mavenCentralUsername") as String? ?: System.getenv("MAVEN_CENTRAL_USERNAME")
                password = findProperty("mavenCentralPassword") as String? ?: System.getenv("MAVEN_CENTRAL_PASSWORD")
            }
        }
        maven {
            name = "local"
            url  = uri(layout.buildDirectory.dir("local-repo"))
        }
    }
}

// ─── GPG signing ─────────────────────────────────────────────────────────────
// The key file path is read from signing.keyFile in ~/.gradle/gradle.properties.
// This avoids the backslash-continuation problem entirely — the key lives in a
// plain .asc file, not crammed into a .properties value.
//
// ~/.gradle/gradle.properties should contain:
//   signing.keyFile=/home/hena/.gnupg/kenat_signing_key.asc
//   signing.password=yourPassphrase
//   mavenCentralUsername=yourPortalTokenUsername
//   mavenCentralPassword=yourPortalTokenPassword
// ─────────────────────────────────────────────────────────────────────────────
signing {
    val keyFile = findProperty("signing.keyFile") as String?
    val keyPassword = findProperty("signing.password") as String? ?: ""

    if (keyFile != null) {
        val keyContent = file(keyFile).readText()
        useInMemoryPgpKeys(keyContent, keyPassword)
    }

    sign(publishing.publications["mavenJava"])
}

tasks.withType<Sign>().configureEach {
    onlyIf { findProperty("signing.keyFile") != null }
}
