import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.util.Node
import org.gradle.api.publish.maven.internal.artifact.FileBasedMavenArtifact

plugins {
    java
    `java-library`
    `maven-publish`
    idea
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "gg.generations"
version = "2.11.21"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

sourceSets {
    val assetLoading = create("library") {
        compileClasspath += main.get().compileClasspath
    }

    val patches = create("patches") {
        compileClasspath += main.get().compileClasspath
    }


    main {
        this.compileClasspath += assetLoading.output
        this.compileClasspath += patches.output
        this.runtimeClasspath += assetLoading.output
        this.runtimeClasspath += patches.output

    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://maven.generations.gg/releases")
}

dependencies {
    configurations {
        create("shadowTools")
    }

    compileOnly("org.jetbrains:annotations:24.1.0")

    "shadow"(implementation("org.tukaani", "xz", "1.9"))
    "shadow"(implementation("org.apache.commons", "commons-compress", "1.26.1"))
    "shadowTools"(implementation("org.joml", "joml", "1.10.5"))

    "shadowTools"(implementation(platform("org.lwjgl:lwjgl-bom:3.3.2"))!!)
    "shadowTools"(implementation("org.lwjgl", "lwjgl"))
    "shadowTools"(implementation("org.lwjgl", "lwjgl-glfw"))
    "shadowTools"(implementation("org.lwjgl", "lwjgl-opengl"))
    "shadowTools"(implementation("org.lwjgl", "lwjgl-stb"))
    "shadow"(implementation("org.lwjgl", "lwjgl-assimp", "3.3.2")) //Only now just to keep assimp native from complaining
    "shadow"(implementation("com.github.thecodewarrior", "BinarySMD", "-SNAPSHOT"))
    "shadow"(implementation("org.msgpack", "msgpack-core", "0.8.17"))
    "shadowTools"(implementation(fileTree(mapOf("dir" to "libs", "include" to "*.jar")))!!)
    "shadow"(implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")!!)
    "shadow"(implementation("io.github.mudbill:dds-lwjgl:3.0.0")!!)


    listOf("windows", "macos", "linux", ).forEach { os ->
        listOf("-arm64", "").forEach { cpu ->
            "shadowTools"(runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-$os$cpu"))
            "shadowTools"(runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-$os$cpu"))
            "shadowTools"(runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = "natives-$os$cpu"))
            "shadowTools"(runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-$os$cpu"))
            "shadow"(runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = "natives-$os$cpu"))
            "shadowTools"(runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = "natives-$os$cpu"))
        }
    }

    "shadowTools"(implementation("org.slf4j:slf4j-jdk14:2.0.12")!!)

    // PokeUtils Libs
    "shadowTools"(implementation("com.github.weisj:darklaf-core:3.0.2")!!)
    "shadowTools"(implementation("com.intellij:forms_rt:7.0.3")!!)
    "shadowTools"(implementation("org.lwjgl", "lwjgl-nfd"))
    "shadowTools"(implementation("org.lwjglx", "lwjgl3-awt", "0.1.8"))

    "shadow"(implementation("com.google.flatbuffers:flatbuffers-java:23.5.26")!!)

    //TODO: JT need some funky gradle logic that lets us build a version does and doesn't include gson for the viewer and generations respectively
    "shadowTools"(implementation("com.google.code.gson:gson:2.10.1")!!)


}

tasks {
    var rareCandyTools = register<ShadowJar>("rare_candy_tools") {
        archiveBaseName.set("RareCandyTools")
        from(sourceSets["main"].output.resourcesDir)
        from(sourceSets["main"].output.classesDirs)
        from(sourceSets["library"].output.classesDirs)
        manifest.attributes(mapOf("Main-Class" to "gg.generations.rarecandy.tools.Main"))

        configurations = listOf(
            project.configurations.getByName("shadow"),
            project.configurations.getByName("shadowTools")
        )

        dependsOn("processResources")
    }

    var rareCandy = register<ShadowJar>("rare_candy") {
        archiveBaseName.set("RareCandy")
        from(sourceSets.getByName("library").output.classesDirs)
        from(sourceSets.getByName("patches").output.classesDirs)

        exclude("src/main/**", "org/lwjgl/**", "module-info.class")

        relocate("org.tukaani.xz", "gg.generations.rarecandy.shaded.xz")
        relocate("com.github.benmanes.caffeine.cache", "gg.generations.rarecandy.shaded.caffeine.cache")
        relocate("com.google.flatbuffers", "gg.generations.rarecandy.shaded.flatbuffers")
        relocate("org.msgpack", "gg.generations.rarecandy.shaded.msgpack")
        relocate("dev.thecodewarrior.binarysmd", "gg.generations.rarecandy.shaded.binarysmd")
        relocate("org.checkerframework", "gg.generations.rarecandy.shaded.checkerframework")
        relocate("com.google.errorprone", "gg.generations.rarecandy.shaded.errorprone")
        relocate("org.apache.commons", "gg.generations.rarecandy.shaded.commons")
        relocate("windows.arm64.org.lwjgl.assimp", "windows.arm64.gg.generations.rarecandy.assimp")
        relocate("windows.x64.org.lwjgl.assimp", "windows.x64.gg.generations.rarecandy.assimp")
        relocate("linux.arm64.org.lwjgl.assimp", "linux.arm64.gg.generations.rarecandy.assimp")
        relocate("linux.x64.org.lwjgl.assimp", "linux.x64.gg.generations.rarecandy.assimp")
        relocate("macos.arm64.org.lwjgl.assimp", "macos.arm64.gg.generations.rarecandy.assimp")
        relocate("macos.x64.org.lwjgl.assimp", "macos.x64.gg.generations.rarecandy.assimp")
        relocate("org.lwjgl.assimp", "gg.generations.rarecandy.assimp")

        configurations = listOf(
            project.configurations.getByName("shadow")
        )
    }

    build.get().dependsOn(rareCandy.get(), rareCandyTools.get())
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifacts.clear()
        artifact(tasks["rare_candy_tools"]).let { it.classifier = "tools" }
        artifact(tasks["rare_candy"])

//        artifact(tasks["rare_candy"].outputs.files.files.first())
    }

    repositories {
        mavenLocal()
        maven {
            val releasesRepoUrl = "https://maven.generations.gg/releases"
            val snapshotsRepoUrl = "https://maven.generations.gg/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT") || version.toString().startsWith("0")) snapshotsRepoUrl else releasesRepoUrl)
            name = "Generations-Repo"
            credentials {
                username = project.properties["repoLogin"]?.toString()
                password = project.properties["repoPassword"]?.toString()
            }
        }
    }


}

fun mcDependency(handler: DependencyHandlerScope, group: String, name: String) {
    handler.compileOnly("$group:$name")
    handler.testImplementation("$group:$name")
}
