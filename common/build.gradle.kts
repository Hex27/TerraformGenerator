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
    implementation(group = "org.spigotmc", name = "spigot", version = "1.16.4-R0.1-SNAPSHOT")
    implementation(fileTree("../libs/"))
}
