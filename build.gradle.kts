plugins {
    java
    id("com.github.johnrengelman.shadow").version("6.1.0")
}

dependencies {

}

repositories {
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots")}
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
