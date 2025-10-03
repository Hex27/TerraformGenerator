plugins {
	java;
    id("com.gradleup.shadow").version("9.0.0-beta2");
    id("io.github.patrick.remapper") version "1.4.2"
}
dependencies {
    implementation(project(":implementation:v1_21_R6"))
}
tasks.shadowJar.configure {
	archiveFileName = "Spigotv1_21_R6.jar" //Don't write to -all
    relocate("org.terraform.v1_21_R6","org.terraform.spigot.v1_21_R6")
	relocate("org.bukkit.craftbukkit","org.bukkit.craftbukkit.v1_21_R6")
}
tasks.remap.configure {
    dependsOn("shadowJar")
    action.set(io.github.patrick.gradle.remapper.RemapTask.Action.MOJANG_TO_OBF)
    version.set("1.21.9")
}