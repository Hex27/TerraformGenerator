plugins {
    java
}

group = "org.terraform"

repositories {
    maven { url = uri("https://repo.codemc.io/repository/nms/") }
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    mavenCentral()
    flatDir {
        dirs("../libs")
    }
}

dependencies {
    testCompile("junit", "junit", "4.12")
    compileOnly(group = "org.spigotmc", name = "spigot", version = "1.16.4-R0.1-SNAPSHOT")
    compileOnly(fileTree("../libs/"))
    implementation("io.papermc:paperlib:1.0.5")
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
