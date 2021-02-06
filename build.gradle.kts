import java.io.ByteArrayOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

plugins {
    java
}

dependencies {

}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val testDir = "target/server"
val testVersion = "1.16.5"

val setupServer = tasks.create("setupServer") {
    dependsOn("buildProj:shadowJar")
    doFirst {
        // clean
        file("${testDir}/").deleteRecursively()
        file("WorldGenTestServer/").deleteRecursively()
        file("${testDir}/plugins").mkdirs()

        // Downloading latest paper jar.
        val paperUrl = URL("https://papermc.io/api/v1/paper/$testVersion/latest/download")
        val paperReadableByteChannel = Channels.newChannel(paperUrl.openStream())
        val paperFile = file("${testDir}/paper.jar")
        val paperFileOutputStream = paperFile.outputStream()
        val paperFileChannel = paperFileOutputStream.channel
        paperFileChannel.transferFrom(paperReadableByteChannel, 0, Long.MAX_VALUE)

        // Cloning test setup.
        gitClone("https://github.com/PolyhedralDev/WorldGenTestServer")

        // Copying plugins
        file("WorldGenTestServer/plugins").copyRecursively(file("$testDir/plugins"), true)
        // Copy Drycell lib
        file("libs/Drycell.jar").copyTo(file("$testDir/plugins/Drycell.jar"), true)
        // Copying config
        val serverText = URL("https://raw.githubusercontent.com/PolyhedralDev/WorldGenTestServer/master/server.properties").readText()
        file("${testDir}/server.properties").writeText(serverText)
        val bukkitText = URL("https://raw.githubusercontent.com/PolyhedralDev/WorldGenTestServer/master/bukkit.yml").readText()
        file("${testDir}/bukkit.yml").writeText(bukkitText.replace("\${world}", "world").replace("\${gen}", "TerraformGenerator"))

        File("${testDir}/eula.txt").writeText("eula=true")

        // clean up
        file("WorldGenTestServer").deleteRecursively()
    }
}

val testWithPaper = tasks.create("testWithPaper") {
    dependsOn("buildProj:shadowJar")
    doFirst {
        copy {
            from("${project("buildProj").buildDir}/libs/buildProj-all.jar")
            into("${testDir}/plugins/")
        }
        exec {
            commandLine = mutableListOf("java", "-Xmx3G", "-Xms3G", "-XX:+UseG1GC", "-XX:+ParallelRefProcEnabled", "-XX:MaxGCPauseMillis=200",
                    "-XX:+UnlockExperimentalVMOptions", "-XX:+DisableExplicitGC", "-XX:+AlwaysPreTouch",
                    "-XX:G1NewSizePercent=30", "-XX:G1MaxNewSizePercent=40", "-XX:G1HeapRegionSize=8M",
                    "-XX:G1ReservePercent=20", "-XX:G1HeapWastePercent=5", "-XX:G1MixedGCCountTarget=4",
                    "-XX:InitiatingHeapOccupancyPercent=15", "-XX:G1MixedGCLiveThresholdPercent=90",
                    "-XX:G1RSetUpdatingPauseTimePercent=5", "-XX:SurvivorRatio=32", "-XX:+PerfDisableSharedMem",
                    "-XX:MaxTenuringThreshold=1", "-Dusing.aikars.flags=https://mcflags.emc.gs",
                    "-Daikars.new.flags=true", "-DIReallyKnowWhatIAmDoingISwear", "-jar", "paper.jar", "nogui") // Remove nogui for gui
            workingDir = file("${testDir}/")
            standardOutput = System.out
            standardInput = System.`in`
        }
    }
}

}

def outputTasks() {
    ["shadowJar", ":Bukkit:shadowJar", ":Bungee:shadowJar"].stream().map({ tasks.findByPath(it) })
}

fun gitClone(name: String) {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = mutableListOf("git", "clone", name)
        standardOutput = stdout
    }
}
