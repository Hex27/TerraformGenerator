plugins {
	java;
    id("com.gradleup.shadow").version("9.0.0-beta2")
}

dependencies {
    shadow(project(":common"))
	shadow(group = "org.spigotmc", name = "spigot", version = "1.21.9-R0.1-SNAPSHOT")
	shadow("org.jetbrains:annotations:20.1.0")
    shadow("com.github.AvarionMC:yaml:1.1.7")
}

//Copies source files from v1_21_R6 into this subproject
tasks.register<Copy>("duplicateSource") {
	delete("src")
	from(project(":implementation:v1_21_R6").sourceSets["main"].java + "org/terraform/v1_21_R6")
	filter { 
		line: String -> line.replace("package org.terraform.","package org.terraform.paper.")
    }
	into("src/main/java/org/terraform/paper/v1_21_R6")
}

tasks.compileJava.configure {
    dependsOn("duplicateSource")
}

tasks.assemble.configure {
	dependsOn("shadowJar")
}
tasks.shadowJar.configure {
    dependencies {
        exclude(dependency("com.example:my-special-artifact:0.0.1"))
    }
	archiveFileName = "Paperv1_21_R6.jar"
	relocate("org.bukkit.craftbukkit.v1_21_R6","org.bukkit.craftbukkit")
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}