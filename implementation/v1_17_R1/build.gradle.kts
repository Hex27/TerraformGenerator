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
    testImplementation("junit", "junit", "4.12")
    //compileOnly(group = "org.spigotmc", name = "spigot", version = "1.17-R0.1-SNAPSHOT")
    compileOnly(fileTree("../../libs/"))
}
java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}
