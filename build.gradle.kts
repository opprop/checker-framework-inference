import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.lang.ProcessBuilder.Redirect
import java.util.concurrent.TimeUnit

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

// Note:  For this setup to work you must follow the instructions outlined in the
//       checker manual Section 25.3 "Building from Source"
// http://types.cs.washington.edu/checker-framework/current/checkers-manual.html#build-source

// Begin of extra project properties
val jsr308 by extra(System.getenv("JSR308") ?: file(File("..")).absolutePath)
val checkerFrameworkPath by extra(System.getenv("CHECKERFRAMEWORK") ?: "${jsr308}/checker-framework")
val checkerJar by extra("${checkerFrameworkPath}/checker/dist/checker.jar")
val afu by extra("${jsr308}/annotation-tools/annotation-file-utilities")
val z3Jar by extra("${projectDir}/lib/com.microsoft.z3.jar")
val lingelingTar by extra("${projectDir}/lib/lingeling.tar.gz")
val dljcScript by extra("${jsr308}/do-like-javac/dljc")

// On a Java 8 JVM, use error-prone javac and source/target 8.
// On a Java 9+ JVM, use the host javac, default source/target, and required module flags.
val isJava8 by extra(JavaVersion.current() == JavaVersion.VERSION_1_8)

val errorproneJavacVersion by extra("9+181-r4173-1")
// End of extra project properties

println("""
====================================
    Checker Framework Inference     
====================================

-------------------------------
Important Environment Variables
-------------------------------
CHECKERFRAMEWORK: $checkerFrameworkPath
""".trimIndent())

repositories {
    mavenCentral()
}

configurations {
    create("javacJar")
}

dependencies {
    if (isJava8) {
        "javacJar"(group = "com.google.errorprone", name = "javac", version = errorproneJavacVersion)
    }

    implementation(files(checkerJar))
    implementation(group = "com.google.errorprone", name = "javac", version = errorproneJavacVersion)

    implementation("org.plumelib:options:1.0.5")
    implementation("org.plumelib:plume-util:1.5.8")

    implementation("com.google.guava:guava:31.1-jre")

    // AFU is an "includedBuild" imported in settings.gradle.kts, so the version number doesn"t matter.
    // https://docs.gradle.org/current/userguide/composite_builds.html#settings_defined_composite
    implementation("org.checkerframework:annotation-file-utilities:*") {
        exclude(group = "com.google.errorprone", module = "javac")
    }

    // Serialize constraints
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    // Pretty print serialized constraints
    implementation("com.google.code.gson:gson:2.9.0")

    implementation("org.ow2.sat4j:org.ow2.sat4j.core:2.3.6")
    implementation("org.ow2.sat4j:org.ow2.sat4j.maxsat:2.3.6")
    implementation(files(z3Jar))

    testImplementation(fileTree("dir" to "${checkerFrameworkPath}/framework-test/build/libs", "include" to "framework-test-*.jar"))
    // Mocking library. Used in a couple tests
    testImplementation("org.mockito:mockito-all:2.0.2-beta")
    testImplementation("junit:junit:4.13.2")
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src"))
        }

        resources {
            srcDir("src")
            include("**/*.astub")
        }
    }

    test {
        java {
            setSrcDirs(listOf("tests"))
        }
    }
}

val buildZ3 by tasks.registering(Exec::class) {
    description = "Build Z3 solver"
    onlyIf { !(File(z3Jar).exists()) }
    workingDir("./scripts")
    commandLine("./buildZ3")
}

val buildLingeling by tasks.registering(Exec::class) {
    description = "Build Lingeling solver"
    onlyIf { !(File(lingelingTar).exists()) }
    workingDir("./scripts")
    commandLine("./buildLingeling")
}

val buildDLJC by tasks.registering(Exec::class) {
    description = "Build DLJC Tool"
    onlyIf { !(File(dljcScript).exists()) }
    workingDir("./scripts")
    commandLine("./buildDLJC")
}

tasks.test {
    dependsOn(tasks.shadowJar)
    dependsOn(dist)

    systemProperties(
        "path.afu.scripts" to "${afu}/scripts",
        "use.hacks" to true,
        "JDK_JAR" to "${checkerFrameworkPath}/checker/dist/jdk8.jar"
    )

    if (project.hasProperty("emit.test.debug")) {
        systemProperties("emit.test.debug" to "true")
    }

    if (isJava8) {
        jvmArgs("-Xbootclasspath/p:${configurations["javacJar"].asPath}")
    } else {
        // Without this, the test throw "java.lang.OutOfMemoryError: Java heap space"
        // Corresponding pull request: https://github.com/opprop/checker-framework-inference/pull/263
        setForkEvery(1)
    }

    testLogging {
        // Always run the tests
        outputs.upToDateWhen { false }
        // The following prints out each time a test is passed.
        // events = mutableSetOf(
        //     TestLogEvent.PASSED,
        //     TestLogEvent.SKIPPED,
        //     TestLogEvent.FAILED,
        //     TestLogEvent.STANDARD_OUT,
        //     TestLogEvent.STANDARD_ERROR
        // )

        // Show the found unexpected diagnostics and expected diagnostics not found.
        exceptionFormat = TestExceptionFormat.FULL
    }

    // After each test, print a summary.
    // See https://github.com/gradle/kotlin-dsl/issues/836
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}

        override fun afterSuite(desc: TestDescriptor, result: TestResult) {
            if (desc.className != null) {
                val mils = result.endTime - result.startTime
                val seconds = mils / 1000.0

                logger.info(
                    "Testsuite: ${desc.className}\n" +
                    "Tests run: ${result.testCount}, " +
                    "Failures: ${result.failedTestCount}, " +
                    "Skipped: ${result.skippedTestCount}, " +
                    "Time elapsed: $seconds sec\n"
                )
            }
        }
    })
}

tasks.compileJava {
    dependsOn(buildZ3)
    dependsOn(buildLingeling)
    dependsOn(buildDLJC)
    options.compilerArgs = mutableListOf(
        "-implicit:class",
        "-Awarns",
        "-Xmaxwarns", "10000",
        // Can"t use this because JSON library contains raw types:
        // "-Xlint:unchecked",
        "-Xlint:deprecation",
        "-Werror",
    )
}

// Exclude parts of the build directory that don"t include classes from being packaged in
// the jar file.
// IMPORTANT: If "libs" is packaged in the JAR file you end up with an infinitely
// recursive jar task that will fill up your hard drive (eventually)
tasks.jar {
    description = "Makes a jar with ONLY the classes compiled for checker " +
            "framework inference and NONE of its dependencies"
    archiveFileName.set("checker-framework-inference.jar")
    manifest.attributes("Main-Class" to "checkers.inference.InferenceLauncher")
    exclude("dependency-cache", "libs", "tmp")
}

tasks.shadowJar {
    dependencies {
        exclude(dependency("junit:.*:.*"))
    }
    description = "Creates the \"fat\" checker.jar in dist"
    destinationDirectory.set(file("${projectDir}/dist"))
    archiveFileName.set("checker-framework-inference.jar")
}

val dist by tasks.registering(Copy::class) {
    dependsOn(tasks.shadowJar)

    description = "If your Checker Framework project is fully built, this task " +
            "will build checker-framework-inference.jar, copy all the relevant runtime jars into " +
            "the dist directory."
    from(files(
        "${checkerFrameworkPath}/checker/dist/jdk8.jar",
        "${checkerFrameworkPath}/checker/dist/checker.jar",
        "${checkerFrameworkPath}/checker/dist/checker-qual.jar",
        "${checkerFrameworkPath}/checker/dist/checker-util.jar",
        "${checkerFrameworkPath}/checker/dist/javac.jar",
    ))
    into(file("dist"))
}

val dependenciesJar by tasks.registering(Jar::class) {
    dependsOn(dist)

    description = "Create a jar file with all the dependencies."
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().files.map { if (it.isDirectory) it else zipTree(it) })
    archiveFileName.set("dependencies.jar")
    destinationDirectory.set(file("${projectDir}/dist/"))
}

val testLibJar by tasks.registering(Jar::class) {
    dependsOn(dist)

    from(sourceSets.test.get().output.classesDirs)
    include("checkers/inference/test/**")
    archiveFileName.set("inference-framework-test-lib.jar")
    destinationDirectory.set(file("${projectDir}/dist/"))
}

tasks.clean {
    delete("build/libs/checker-framework-inference.zip")
    delete("jdk8.jar")
    delete("javac.jar")
    delete(fileTree("dist") {
        include("**/*.jar")
    })
    delete("testdata/tmp")
}

val release by tasks.registering(Zip::class) {
    from("src") {
        into("release/src")
    }

    from("dist") {
        into("release/dist")
    }
    from("scripts") {
        into("release/scripts")
        include("*.py")
    }

    archiveBaseName.set("release")
}

val cloneAndBuildDependencies by tasks.registering(Exec::class) {
    description ="Clones (or updates) and builds all dependencies"
    executable = "./.ci-build-without-test.sh"
}

val testCheckerInferenceScript by tasks.registering(Exec::class) {
    dependsOn(dist)

    description = "Basic sanity check of scripts/inference"
    executable = "./scripts/inference"
    setArgs(listOf(
        "--mode", "TYPECHECK",
        "--checker", "ostrusted.OsTrustedChecker",
        "--solver", "checkers.inference.solver.PropagationSolver",
        "testdata/ostrusted/Test.java"
    ))
}

val testCheckerInferenceDevScript by tasks.registering(Exec::class) {
    dependsOn(dist, dependenciesJar)

    description = "Basic sanity check of scripts/inference-dev"
    executable = "./scripts/inference-dev"
    setArgs(listOf(
        "--mode", "INFER",
        "--checker", "interning.InterningChecker",
        "--solver", "checkers.inference.solver.MaxSat2TypeSolver",
        "--hacks=true",
        "testdata/interning/MapAssignment.java"
    ))
}

val testDataflowExternalSolvers by tasks.registering(Exec::class) {
    dependsOn(dist, dependenciesJar)

    description = "Test Dataflow type system on its external solvers Lingeling and LogicBlox"
    executable = "./testing/dataflowexample/ci-test.sh"
}

afterEvaluate {
    // Create a task for each test class whose name is the same as the class name.
    sourceSets.test.get().java.filter {
        it.path.contains("tests/checkers") &&
        it.path.endsWith("Test.java") &&
        !it.path.contains("CFInferenceTest")
    }.forEach { file ->
        val junitClassName = file.name.replace(".java", "")
        tasks.create(name = junitClassName, type = Test::class) {
            group = "Verification"
            description = "Run $junitClassName tests."
            include("**/${name}.class")
        }
    }

    // Configure Tests
    tasks.withType(Test::class) {
        dependsOn(tasks.shadowJar)

        systemProperties(
            "path.afu.scripts" to "${afu}/scripts",
            "path.inference.script" to "${projectDir}/scripts/inference",
            "use.hacks" to true,
            "JDK_JAR" to "${checkerFrameworkPath}/checker/dist/jdk8.jar"
        )

        if (project.hasProperty("emit.test.debug")) {
            systemProperties("emit.test.debug" to "true")
        }

        if (isJava8) {
            jvmArgs("-Xbootclasspath/p:${configurations["javacJar"].asPath}")
        }

        testLogging {
            // Always run the tests
            outputs.upToDateWhen { false }
            // The following prints out each time a test is passed.
             events = mutableSetOf(
            //     TestLogEvent.PASSED,
            //     TestLogEvent.SKIPPED,
                 TestLogEvent.FAILED,
            //     TestLogEvent.STANDARD_OUT,
            //     TestLogEvent.STANDARD_ERROR
             )

            // Show the found unexpected diagnostics and expected diagnostics not found.
            exceptionFormat = TestExceptionFormat.FULL
        }

        // After each test, print a summary.
        // See https://github.com/gradle/kotlin-dsl/issues/836
        addTestListener(object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) {}
            override fun beforeTest(testDescriptor: TestDescriptor) {}
            override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}

            override fun afterSuite(desc: TestDescriptor, result: TestResult) {
                if (desc.className != null) {
                    val mils = result.endTime - result.startTime
                    val seconds = mils / 1000.0

                    logger.info(
                        "Testsuite: ${desc.className}\n" +
                        "Tests run: ${result.testCount}, " +
                        "Failures: ${result.failedTestCount}, " +
                        "Skipped: ${result.skippedTestCount}, " +
                        "Time elapsed: $seconds sec\n"
                    )
                }
            }
        })
    }
}

// TODO: convert the commented out code to Kotlin
/// Commented out because plugins section is commented out
// /* Configuration for formatting */
// googleJavaFormat {
//   // toolVersion "1.3"
//   options style: "AOSP"
// }
// tasks.googleJavaFormat {
//   group "Formatting"
//   description = "Reformat Java source code with Google-Java-format"
//   exclude "testing"
//   exclude "testinputs"
// }
// tasks.verifyGoogleJavaFormat {
//   group "Formatting"
//   description = "Check Java source code is in Google-Java-format"
//   exclude "testing"
//   exclude "testinputs"
// }

val etags by tasks.registering {
    doLast {
        val sources = sourceSets.main.get().java.files.map { src -> src.path }.sorted()
        val sourcesStr = sources.joinToString(" ")

        val proc = ProcessBuilder("etags", sourcesStr)
            .directory(rootProject.projectDir)
            .redirectOutput(Redirect.INHERIT)
            .redirectError(Redirect.INHERIT)
            .start()
        proc.inputStream.bufferedReader().useLines { line -> println(line) }
        proc.errorStream.bufferedReader().useLines { line -> println("ERROR: $line") }
        proc.waitFor(10, TimeUnit.MINUTES)
    }
}

val tags by tasks.registering {
    dependsOn(etags)
}

val countHacks by tasks.registering(Exec::class) {
    commandLine("bash", "-c", "grep -r 'InferenceMain.isHackMode(' src/ | wc -l")
}
