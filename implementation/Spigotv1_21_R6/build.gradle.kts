import net.md_5.specialsource.Jar
import net.md_5.specialsource.JarMapping
import net.md_5.specialsource.JarRemapper
import net.md_5.specialsource.provider.JarProvider
import net.md_5.specialsource.provider.JointProvider
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

plugins {
	java;
    id("com.gradleup.shadow").version("9.0.0-beta2")
}
buildscript {
    dependencies {
        classpath("net.md-5:specialsource-maven-plugin:1.2.2")
    }
}
dependencies {
    implementation(project(":implementation:v1_21_R6"))
}
tasks.shadowJar.configure {
	archiveFileName = "Spigotv1_21_R6.jar" //Don't write to -all
    relocate("org.terraform.v1_21_R6","org.terraform.spigot.v1_21_R6")
	relocate("org.bukkit.craftbukkit","org.bukkit.craftbukkit.v1_21_R6")
}

tasks.register("remapToObs") {
    dependsOn("shadowJar")
    //Download mappings from mojang and perform a remap
    val mappings = layout.buildDirectory.file("server.txt").get().asFile
    if (!mappings.exists()) {
        project.logger.lifecycle("Could not find server.txt, trying to download it from mojang...")
        //var url = URL("")
        //Files.copy(url.openStream(), mappings.getPath(), StandardCopyOption.REPLACE_EXISTING)

        val response : HttpResponse<String> = HttpClient.newHttpClient().send(
            HttpRequest
                .newBuilder(URI.create("https://piston-data.mojang.com/v1/objects/587e016fe8a876cbc1cc98d73f9d0d79bfb32b2c/server.txt"))
                .build(),
            BodyHandlers.ofString())
        val outputStream = FileOutputStream(mappings)
        outputStream.write(response.body().toByteArray())
        outputStream.close()
    }

    if(!mappings.exists()) throw StopActionException("Failed to download mappings from Mojang!")
    var tempFile = Files.createTempFile(null, ".jar").toFile();

    var shadowedJarFile = layout.buildDirectory.dir("libs").get()
        .file("Spigotv1_21_R6.jar").asFile
    var inputJar = Jar.init(shadowedJarFile)
    var jarMapping = JarMapping()
    jarMapping.loadMappings(mappings.getAbsolutePath(), false, false, null, null)

    var provider = JointProvider()
    provider.add(JarProvider(inputJar))
    jarMapping.setFallbackInheritanceProvider(provider)

    var remapper = JarRemapper(jarMapping)
    inputJar.close()
    remapper.remapJar(inputJar, tempFile)
    Files.copy(
        tempFile.toPath(),
        layout.buildDirectory.dir("libs").get()
            .file("reobs.jar").asFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING)
    tempFile.delete()
}