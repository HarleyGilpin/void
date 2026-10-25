## Build Commands

```bash
./gradlew build -x test        # initial setup (cache files needed for tests)
./gradlew test                 # all tests
./gradlew test --tests "content.skill.firemaking.FiremakingTest"
./gradlew :game:test           # single module
./gradlew test -x :game:test   # everything except the slow content tests
./gradlew spotlessApply        # format (required before committing)
./gradlew spotlessCheck        # what CI gates on
./gradlew shadowJar            # distributable JAR
./gradlew run                  # start server (working dir = root)
./gradlew assembleBundleDist   # JAR + data + startup scripts -> game/build/distributions/
```

Add `-PincludeDb` to any of the above to compile in the `database` module (PostgreSQL persistence); without it the project is not part of the build.

Toolchain: JVM 21, Kotlin 2.4, Gradle 9.6 (configuration cache on).

Tests require cache files in `data/cache/`. CI runs spotless plus all engine and content tests on all PRs.
