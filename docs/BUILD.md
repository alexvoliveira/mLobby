# mLobby build

mLobby is a plugin that requires mCore at runtime (`plugin.yml` declares `depend: [mCore]`).
The project compiles against the mCore JAR; it does not package or relocate mCore itself.

## Local build

Build mCore first so its shaded inventory API is available in the public menu wrapper, then run:

```bash
./gradlew --no-daemon clean build -PmcoreJar=/absolute/path/to/mCore.jar
```

Alternatively set `MCORE_JAR` to the absolute path of `mCore.jar`. By default the build checks
`mCore/build/libs/mCore.jar`, `../mCore/build/libs/mCore.jar`, and `libs/mCore.jar`.

## GitHub Actions

The build workflow checks out the compatible mCore revision and compiles both projects. Because
mCore is private, add a repository secret named `MCORE_REPO_TOKEN` to mLobby with read access to
`alexvoliveira/mCore` (contents: read). The workflow pins mCore to a commit so a moving branch
cannot unexpectedly break the mLobby build; update that pin deliberately when mCore changes.
