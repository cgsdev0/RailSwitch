plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.3.8"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2" // Generates plugin.yml
}

java {
  // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

group = "net.civmc.railswitch"
version = "2.0.0-SNAPSHOT"
description = "RailSwitch"

repositories {
  mavenCentral()
  maven {
    url = uri("https://repo.mikeprimm.com")
  }
}
dependencies {
  compileOnly("us.dynmap:DynmapCoreAPI:3.3")
  paperDevBundle("1.19.2-R0.1-SNAPSHOT")
  implementation("com.eatthepath:jvptree:0.3.0")
}


tasks {
  // Configure reobfJar to run when invoking the build task
  assemble {
    dependsOn(reobfJar)
  }

  compileJava {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

    // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
    // See https://openjdk.java.net/jeps/247 for more information.

    options.release.set(17)

  }
  javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
  }
  processResources {
    filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
  }
}

// Configure plugin.yml generation
bukkit {
  main = "sh.okx.railswitch.RailSwitchPlugin"
  apiVersion = "1.19"
  authors = listOf("Author")
}
