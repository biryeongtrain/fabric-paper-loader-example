plugins {
	id("com.playmonumenta.papermixins.mod-conventions")
}

group = "org.example"
version = "1.0.0-SNAPSHOT"

dependencies {
	implementation(libs.nbtapi.plugin)
	shade(libs.nbtapi)
}

// Tasks configuration
val targetJavaVersion = 21

tasks {
	processResources {
		inputs.properties("version" to version.toString())

		filesMatching("fabric.mod.json") {
			expand(
				mapOf(
					"version" to version.toString()
				)
			)
		}
	}

	reobfJar {
		accessWideners.add("exampleMod.accesswidener")
	}

	shadowJar {
		relocate("de.tr7zw.changeme.nbtapi", "de.tr7zw.nbtapi")
	}
}
