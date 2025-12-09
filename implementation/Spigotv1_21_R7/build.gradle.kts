plugins {
	java;
    id("com.gradleup.shadow").version("9.0.0-beta2");
    id("io.github.patrick.remapper") version "1.4.2"
}
dependencies {
    implementation(project(":implementation:v1_21_R7"))
}
tasks.shadowJar.configure {
    dependsOn("jar")
	archiveFileName = "Spigotv1_21_R7.jar" //Don't write to -all
    relocate("org.terraform.v1_21_R7","org.terraform.spigot.v1_21_R7")
	relocate("org.bukkit.craftbukkit","org.bukkit.craftbukkit.v1_21_R7")
}
tasks.remap.configure {
    dependsOn("shadowJar")
    version.set("1.21.11")
}