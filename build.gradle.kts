plugins {
    id("java-library")
    id("maven-publish")
}

group = "com.pixelmongenerations"
version = "0.3-INDEV4"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains", "annotations", "23.0.0")

    implementation("org.tukaani", "xz", "1.9")
    implementation("org.apache.commons", "commons-compress", "1.21")
    implementation("org.joml", "joml", "1.10.3")
    implementation("com.intellij", "forms_rt", "7.0.3")

    implementation(platform("org.lwjgl:lwjgl-bom:3.3.0"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-assimp")
    mcDependency(this, "org.lwjgl", "lwjgl-stb")
    mcDependency(this, "org.lwjgl", "lwjgl-glfw")
    mcDependency(this, "org.lwjgl", "lwjgl-opengl")

    addNative(this, "org.lwjgl", "lwjgl")
    addNative(this, "org.lwjgl", "lwjgl-assimp")
    addMcNative(this, "org.lwjgl", "lwjgl-stb")
    addMcNative(this, "org.lwjgl", "lwjgl-glfw")
    addMcNative(this, "org.lwjgl", "lwjgl-opengl")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
        maven {
            //val releasesRepoUrl = "https://maven.pixelmongenerations.com/repository/maven-releases/"
            //val snapshotsRepoUrl = "https://maven.pixelmongenerations.com/repository/maven-snapshots/"
            //url = uri(if (version.toString().endsWith("SNAPSHOT") || version.toString().startsWith("0")) snapshotsRepoUrl else releasesRepoUrl)
            url = uri("https://maven.pixelmongenerations.com/repository/maven-private/")
            println(project.properties["repoLogin"])
            credentials{
                username = project.properties["repoLogin"]?.toString() ?: findProperty("REPO_LOGIN").toString()
                password = project.properties["repoPassword"]?.toString() ?: findProperty("REPO_PASSWORD").toString()
            }
        }

    }
}

fun mcDependency(handler: DependencyHandlerScope, group: String, name: String) {
    handler.compileOnly(group, name)
    handler.testImplementation(group, name)
}

// We Exclude 32-bit systems because they are old.
fun addNative(handler: DependencyHandlerScope, group: String, name: String) {
    handler.implementation(group, name, classifier = "natives-windows")
    handler.implementation(group, name, classifier = "natives-windows-arm64")
    handler.implementation(group, name, classifier = "natives-linux")
    handler.implementation(group, name, classifier = "natives-linux-arm64")
    handler.implementation(group, name, classifier = "natives-macos")
    handler.implementation(group, name, classifier = "natives-macos-arm64")
}

fun addMcNative(handler: DependencyHandlerScope, group: String, name: String) {
    handler.testImplementation(group, name, classifier = "natives-windows")
    handler.testImplementation(group, name, classifier = "natives-windows-arm64")
    handler.testImplementation(group, name, classifier = "natives-linux")
    handler.testImplementation(group, name, classifier = "natives-linux-arm64")
    handler.testImplementation(group, name, classifier = "natives-macos")
    handler.testImplementation(group, name, classifier = "natives-macos-arm64")
}
