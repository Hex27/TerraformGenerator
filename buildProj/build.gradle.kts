plugins {
    id("com.github.johnrengelman.shadow").version("7.1.0")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":implementation:v1_18_R2"))
    implementation(project(":implementation:v1_19_R3"))
    implementation(project(":implementation:v1_20_R1"))
    implementation(project(":implementation:v1_20_R2"))
    implementation(project(":implementation:v1_20_R3"))
}

tasks.shadowJar {
    relocate("io.papermc.lib", "org.terraform.lib")
}