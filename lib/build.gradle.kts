import org.gradle.plugins.signing.Sign

plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "io.github.henaorth16"
version = "1.1.0"

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
tasks.register("checkSigningKey") {
    doLast {
        val key = findProperty("signing.key") as String?
        println("Key exists: ${key != null}")
        println("Key length: ${key?.length}")
        println("Starts with: ${key?.take(35)}")
        println("Ends with: ${key?.takeLast(35)}")
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
            url  = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
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

signing {
    val signingPassword = findProperty("signing.password") as String?

    val signingKey = findProperty("signing.key") as String?
    val signingKeyFile = findProperty("signing.keyFile") as String?

    if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    } else if (signingKeyFile != null) {
        useInMemoryPgpKeys(File(signingKeyFile).readText(), signingPassword)
    }

    sign(publishing.publications)
}
