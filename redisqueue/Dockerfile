# Java 17 JDK 베이스 이미지
FROM openjdk:17-jdk

# 작업 디렉토리 설정 (옵션)
WORKDIR /

# JAR 복사
COPY build/libs/*SNAPSHOT.jar app.jar

# 실행 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]
