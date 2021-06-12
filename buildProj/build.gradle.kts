plugins {
    id("com.github.johnrengelman.shadow").version("6.1.0")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":implementation:v1_14_R1"))
    implementation(project(":implementation:v1_15_R1"))
    implementation(project(":implementation:v1_16_R1"))
    implementation(project(":implementation:v1_16_R2"))
    implementation(project(":implementation:v1_16_R3"))
    implementation(project(":implementation:v1_17_R1"))
}

tasks.shadowJar {
    relocate("io.papermc.lib", "org.terraform.lib")
}