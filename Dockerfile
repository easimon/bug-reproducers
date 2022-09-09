ARG BUILD_ARCH=linux/amd64
ARG BUILD_IMAGE=eclipse-temurin:17
ARG RUNTIME_IMAGE=eclipse-temurin:17-jre

FROM --platform=$BUILD_ARCH $BUILD_IMAGE as builder

WORKDIR /build

COPY .mvn /build/.mvn/
COPY mvnw pom.xml /build/
COPY src /build/src
RUN java -version
RUN ./mvnw -B package

# Build runtime image
FROM $RUNTIME_IMAGE

COPY --from=builder /build/target/*.jar app.jar
USER 65535:65535
CMD exec java -jar app.jar
