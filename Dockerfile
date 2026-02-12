# ---- build stage ----
    FROM gradle:8.7-jdk17 AS builder
    WORKDIR /web
    
    # Copy only build files first (better caching)
    COPY build.gradle settings.gradle gradlew /web/
    COPY gradle /web/gradle
    RUN chmod +x /web/gradlew
    
    # Download deps (cache layer)
    RUN ./gradlew dependencies --no-daemon || true
    
    # Copy source and build
    COPY src /web/src
    RUN ./gradlew clean bootJar -x test --no-daemon
    
    # ---- run stage ----
    FROM eclipse-temurin:17-jre
    WORKDIR /web
    
    COPY --from=builder /web/build/libs/*.jar web.jar
    
    # web Runner expects the web to listen on $PORT (usually 8080)
    ENV PORT=8080
    EXPOSE 8080
    
    # Optional: set prod profile (or set it in web Runner env vars instead)
    ENV SPRING_PROFILES_ACTIVE=prod
    
    ENTRYPOINT ["java","-jar","/web/web.jar"]
    