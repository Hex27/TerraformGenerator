plugins {
    java
    id("com.github.johnrengelman.shadow").version("6.1.0")
}

group = "org.terraform"

repositories {
    maven{ url = uri("https://repo.codemc.io/repository/nms/") }
    mavenCentral()
}

dependencies {
    testCompile("junit", "junit", "4.12")
    implementation(project(":common"))
    implementation(project(":implementation:v1_14_R1"))
    implementation(project(":implementation:v1_15_R1"))
    implementation(project(":implementation:v1_16_R1"))
    implementation(project(":implementation:v1_16_R2"))
    implementation(project(":implementation:v1_16_R3"))
}
