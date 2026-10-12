plugins {
    java
}

group = "tk.slicecollections.maxteer.lobby"
version = "1.8"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val configuredMCoreJar = providers.gradleProperty("mcoreJar").orNull
    ?: providers.environmentVariable("MCORE_JAR").orNull
val candidateMCoreJars = listOf(
    rootProject.file("mCore/build/libs/mCore.jar"),
    rootProject.projectDir.parentFile.resolve("mCore/build/libs/mCore.jar"),
    rootProject.file("libs/mCore.jar")
)
val mCoreJar = configuredMCoreJar?.let { file(it) }
    ?: candidateMCoreJars.firstOrNull { it.isFile }
    ?: candidateMCoreJars.first()

if (!mCoreJar.isFile) {
    throw GradleException(
        "mCore.jar was not found. Build mCore first or set MCORE_JAR (or -PmcoreJar) to its path: ${mCoreJar.absolutePath}"
    )
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://repo.phoenix616.dev/") }
    maven { url = uri("https://repo.helpch.at/releases/") }
}

dependencies {
    compileOnly(files(mCoreJar))
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.0")
    compileOnly("me.clip:placeholderapi:2.10.5")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.jar {
    archiveFileName.set("mLobby.jar")
}
