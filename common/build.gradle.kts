plugins {
    java
}

group = "org.terraform"

repositories {
    maven{ url = uri("https://repo.codemc.io/repository/nms/") }
    mavenCentral()
    flatDir {
        dirs("../libs")
    }
}

dependencies {
    testCompile("junit", "junit", "4.12")
    compileOnly(group = "org.spigotmc", name = "spigot", version = "1.16.4-R0.1-SNAPSHOT")
    implementation(fileTree("../libs/"))
    compileOnly("org.jetbrains:annotations:20.1.0")
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
