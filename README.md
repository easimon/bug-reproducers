# Bug: Docker image eclipse-temurin:17+ for arm/v7 unusable in cross-platform execution

## TL;DR

Root cause seems to be an issue in QEMU, which is used by Docker for cross platform
support, so not directly an issue in Eclipse Temurin.

## Diagnosis

Everything that requires `javax.crypto` in some way, fails with a SecurityException like

```
java.lang.SecurityException: Couldn't iterate through the jurisdiction policy files: unlimited
```

This happens e.g. for every HTTPs request -- for Maven builds, this means, Maven fails to download
dependencies.

The error occurs only in cross-platform runs for 32bit arm images on 64bit host platforms, starting
with `eclipse-temurin:17` and newer. `eclipse-temurin:8` and `eclipse-temurin:11` work as expected.

This repository contains a small Java program that exposes the underlying IOException
by copying only the relevant code fragments from javax.crypto.JceSecurity, and printing the original
Stacktrace (which is swallowed in JceSecurity):

```
java.nio.file.FileSystemException: /opt/java/openjdk/conf/security/policy/unlimited: Value too large for defined data type
```

Googling the exception message `Value too large for defined data type` leads to various issues describing similar
issues also in Rust, Python and other projects, indicating that the root cause is an issue in QEMU, not a bug in
eclipse-temurin.

## Possible workarounds

Following workarounds use a multistage build:

### Don't use cross platform docker builds

Makes generation of multiarch Docker images in CI quite painful, except for people that have
a CI landscape with bare metal runners for every target platform.

### Stick to JDK 11 for building, use JRE 17+ for execution

Since JDK 11 does not trigger the error, you can build the JAR on Java 11, and then copy it over
to e.g. 17-jre. You lose new language features this way, but at least have new runtime features/fixes.
Example:

```Dockerfile
ARG BUILD_IMAGE=eclipse-temurin:11
ARG RUNTIME_IMAGE=eclipse-temurin:17-jre

FROM $BUILD_IMAGE as builder

WORKDIR /build

COPY .mvn /build/.mvn/
COPY mvnw pom.xml /build/
COPY src /build/src
RUN ./mvnw -B package

# Build runtime image
FROM $RUNTIME_IMAGE

COPY --from=builder /build/target/*.jar app.jar
USER 65535:65535
CMD exec java -jar app.jar
```

### Cross-platform-multistage builds

Run the build in a fixed working platform, and copy the resulting JAR over to multiple platform-specific
final images. The resulting arm32/v7 image will work on arm32/v7 machines, you just can't test it in CI.

Example:

```Dockerfile
ARG BUILD_ARCH=linux/amd64
ARG BUILD_IMAGE=eclipse-temurin:17
ARG RUNTIME_IMAGE=eclipse-temurin:17-jre

# Pin the build to a specific platform -- the native one for the build host would be best
FROM --platform=$BUILD_ARCH $BUILD_IMAGE as builder

WORKDIR /build

COPY .mvn /build/.mvn/
COPY mvnw pom.xml /build/
COPY src /build/src
RUN ./mvnw -B package

# Build platform-specific runtime image, will use the `--platform` argument of docker build command
FROM $RUNTIME_IMAGE

COPY --from=builder /build/target/*.jar app.jar
USER 65535:65535
CMD exec java -jar app.jar
```

## Links

- https://github.com/docker/buildx/issues/395
- https://gitlab.com/qemu-project/qemu/-/issues/263
