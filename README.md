# AndroidUtils

A lightweight Android utility library maintained by [ouyuanx](https://github.com/ouyuanx).

The repository contains two modules:

- `utils`: the Android library published as an AAR.
- `app`: a sample application used for integration and manual testing.

## Coordinates

```text
io.github.ouyuanx:android-utils:0.1.0
```

After the first release is available on Maven Central, add the dependency with:

```kotlin
dependencies {
    implementation("io.github.ouyuanx:android-utils:0.1.0")
}
```

## Development

Build and test the project:

```shell
./gradlew build
```

Publish the release variant to the local Maven repository:

```shell
./gradlew :utils:publishToMavenLocal
```

Generate a Maven repository under `utils/build/repo` for inspection:

```shell
./gradlew :utils:publishReleasePublicationToBuildDirectoryRepository
```

Public APIs should be added under:

```text
utils/src/main/java/io/github/ouyuanx/androidutils
```

## Release

Releases use semantic versioning. Update `VERSION_NAME` in `gradle.properties`, update
`CHANGELOG.md`, run the verification tasks, and create a matching Git tag such as `v0.1.0`.

Maven Central credentials and the in-memory PGP signing key must be stored as encrypted
GitHub Actions secrets. Never commit them to this repository.

## License

Copyright 2026 ouyuanx

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE).
