plugins {
    java
}

group = "org.terraform"

repositories {
    mavenCentral()
    maven{ url = uri("https://repo.codemc.io/repository/nms/") }
}

dependencies {
    implementation(project(":common"))
    testCompile("junit", "junit", "4.12")
    implementation(group = "org.spigotmc", name = "spigot", version = "1.15.2-R0.1-SNAPSHOT")
    implementation(fileTree("../../libs/"))
}
