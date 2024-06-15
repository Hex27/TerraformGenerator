plugins {
	//When ready, switch back to com.github.johnrengelman.shadow,
	//it currently doesn't support java 21.
    id("io.github.goooler.shadow").version("8.1.7")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":implementation:v1_18_R2"))
    implementation(project(":implementation:v1_19_R3"))
    implementation(project(":implementation:v1_20_R1"))
    implementation(project(":implementation:v1_20_R2"))
    implementation(project(":implementation:v1_20_R3"))
    implementation(project(":implementation:v1_20_R4"))
    implementation(project(":implementation:v1_21_R1"))
}

tasks.shadowJar {
    relocate("io.papermc.lib", "org.terraform.lib")
}