ARG BUILD_IMAGE=eclipse-temurin:17
ARG RUNTIME_IMAGE=eclipse-temurin:17-jre
# arm32
#ARG BUILD_IMAGE=eclipse-temurin:17@sha256:0a3bf35d831d8887257a89acd50243642b63a083970876bc3f11090b8efdb000
#ARG RUNTIME_IMAGE=eclipse-temurin:17-jre@sha256:626ea369e3e754a89ca3724e165b3221f684d971b7439f9d839e2c67228c5e7c

# amd64
#ARG BUILD_IMAGE=eclipse-temurin:17@sha256:24634c901f1f6c915ad2e962ee2471fc08d5a7a329383a7991148cdb78b46fef
#ARG RUNTIME_IMAGE=eclipse-temurin:17-jre@sha256:be9cb927cc01d590bee658d24fb99dc5e4f7f324f2a5483c7a899323788a8374

FROM $BUILD_IMAGE as builder

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
