import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.paperweight.tasks.TinyRemapper
import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.gradle.jvm.tasks.Jar

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.playmonumenta.paperweight-aw.userdev") version "2.0.0-build.5+2.0.0-beta.18" // from https://maven.playmonumenta.com/releases/
//    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

// Custom configuration used for shading/includes
val include: Configuration by configurations.creating
val shade: Configuration by configurations.creating

shade.extendsFrom(include)
configurations.getByName("implementation").extendsFrom(include)
configurations.getByName("runtimeClasspath").extendsFrom(configurations.getByName("mojangMappedServerRuntime"))
configurations.getByName("runtimeClasspath").extendsFrom(configurations.getByName("mojangMappedServer"))
group = "org.example"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://maven.playmonumenta.com/releases")
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.6-R0.1-SNAPSHOT")

    implementation("io.github.llamalad7:mixinextras-common:0.5.0")
    implementation("com.floweytf.fabricpaperloader:fabric-paper-loader:2.0.0+fabric.0.17.2")

    remapper("net.fabricmc:tiny-remapper:0.11.1") {
        artifact {
            classifier = "fat"
        }
    }
}

// Tasks configuration
val targetJavaVersion = 21

tasks {
    jar {
        archiveClassifier.set("dev")
    }

    shadowJar {
        configurations = listOf(shade)
        archiveClassifier.set("dev")
    }

    reobfJar {
//        remapperArgs = TinyRemapper.createArgsList() + "--mixin"
    }
}

// Optionally configure runServer. Leaving it out avoids type issues in Kotlin DSL.
// If needed later, we can configure with the proper task type from run-paper plugin.

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to inputs.properties["version"]))
    }
}
