####################################################
# Stage 1 - Build Application
####################################################
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

####################################################
# Stage 2 - Runtime Image
####################################################
FROM eclipse-temurin:17-jre

LABEL maintainer="Allan"
LABEL application="ecommerce-shop"

WORKDIR /app

# Create non-root user
RUN addgroup --system spring && \
    adduser --system spring --ingroup spring

COPY --from=builder /build/target/ecomerce-shop.jar app.jar

RUN chown -R spring:spring /app

USER spring

EXPOSE 5454

ENTRYPOINT ["java","-jar","app.jar"]