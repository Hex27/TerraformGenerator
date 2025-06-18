plugins {
    id("com.gradleup.shadow").version("9.0.0-beta2")
}

buildscript {
    dependencies {
        classpath("org.yaml:snakeyaml:2.0")
    }
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
    implementation(project(":implementation:v1_21_R2"))
    implementation(project(":implementation:v1_21_R3"))
    implementation(project(":implementation:v1_21_R4"))
    implementation(project(":implementation:v1_21_R5"))
    implementation("com.github.AvarionMC:yaml:1.1.7")
}

tasks.shadowJar {
    doFirst {
        val yamlFile = file("${rootProject.projectDir}/common/src/main/resources/plugin.yml")
        val yaml = org.yaml.snakeyaml.Yaml()
        val config = yaml.load<Map<String, Any>>(yamlFile.inputStream())

        // Set the archive name and version based on the plugin.yml file
        archiveBaseName.set(config["name"].toString())
        archiveVersion.set(config["version"].toString())
        archiveClassifier.set("") // Don't add the '-all' postfix.
    }

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    relocate("io.papermc.lib", "org.terraform.lib")
}

tasks.register<Copy>("deploy") {
    dependsOn(tasks.named("shadowJar"))

    from(layout.buildDirectory.dir("libs"))
    include("*.jar")
    into(rootProject.projectDir)

    doNotTrackState("Disable state tracking due to file access issues")
}