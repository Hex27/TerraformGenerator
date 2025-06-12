import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.errorprone.CheckSeverity
import org.gradle.internal.impldep.io.usethesource.capsule.util.collection.AbstractSpecialisedImmutableMap.mapOf

group = "org.terraform"

plugins{
    id("net.ltgt.errorprone").version("4.2.0")
}

dependencies {
    compileOnly(group = "org.spigotmc", name = "spigot", version = "1.18-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("com.github.AvarionMC:yaml:1.1.7")
    errorprone("com.google.errorprone:error_prone_core:2.38.0")
}

// Set this to the lowest compat in implementation
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<JavaCompile>().configureEach {
    options.errorprone.isEnabled = project.hasProperty("ep")
    //Doc warnings are completely useless checks since basically nothing has docs anyway
    options.errorprone.checks.put("MissingSummary", CheckSeverity.OFF)
    options.errorprone.checks.put("EmptyBlockTag", CheckSeverity.OFF)
    options.errorprone.checks.put("InvalidBlockTag", CheckSeverity.OFF)

    options.errorprone.checks.put("InlineMeSuggester", CheckSeverity.OFF) //I can't use this
    options.errorprone.checks.put("ImmutableEnumChecker", CheckSeverity.OFF) //This hole is too deep
    options.errorprone.checks.put("UnnecessaryParentheses", CheckSeverity.OFF) //Leave me alone
    options.errorprone.checks.put("UnusedVariable", CheckSeverity.OFF) //IntelliJ can do this
    options.errorprone.checks.put("NonApiType", CheckSeverity.OFF) //I'll deal with it as it arrives
    options.errorprone.checks.put("UnusedMethod", CheckSeverity.OFF)

    //Huge amount of false positives. It ignores different return types (though
    // what you did with those was indeed quite cursed)
    options.errorprone.checks.put("MissingOverride", CheckSeverity.OFF)

    options.errorprone.excludedPaths = ".*FastNoise.java" //Floods a ton of floating point precision warnings
}