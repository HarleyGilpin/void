// Redirects every project's build directory when `-PisolatedBuildDir=<path>` is passed.
//
// Why: `run-server.sh` starts the server with `./gradlew :game:run`, which puts the
// module jars (engine-dev.jar, network-dev.jar, ...) and `game/build/classes` directly
// on the live JVM's classpath. That JVM keeps those jars open and caches each zip's
// central directory at startup. Any other Gradle invocation that rewrites one of them
// — notably the */15 renderPhotoBooth cron, whenever a module's sources have changed —
// leaves the running server unable to lazily load classes it hasn't touched yet:
//
//   java.lang.NoClassDefFoundError: world/gregs/voidps/engine/entity/character/mode/Rest$Companion
//   Caused by: java.lang.ClassNotFoundException: ...Rest$Companion
//
// Already-loaded classes keep working, so this surfaces hours later as one feature
// silently breaking (resting, in the case above) rather than as a crash.
//
// Applying this script with `-I` and pointing `isolatedBuildDir` somewhere outside the
// tree keeps such a build's outputs away from the server's classpath entirely.
//
// This applies to the included builds (buildSrc/, build-logic/) as well, so their
// outputs are redirected too — those are shared between invocations and were the other
// thing .gradle-build.lock existed to protect. The lock is kept anyway; it is cheap and
// still serializes access to the shared Gradle user home.
//
// Destinations are namespaced by build so the three root projects (void, buildSrc,
// build-logic) — all of which have the path ":" — do not land in the same directory:
//
//   build-photobooth/void/engine/libs/engine-dev.jar
//   build-photobooth/void/tools/app/...
//   build-photobooth/buildSrc/...

gradle.allprojects {
    val isolated = findProperty("isolatedBuildDir") as String? ?: return@allprojects
    val segments = listOf(rootProject.name) + path.split(':').filter { it.isNotEmpty() }
    layout.buildDirectory.set(File(isolated, segments.joinToString("/")))
}
