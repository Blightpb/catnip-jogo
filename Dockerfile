# Compila e executa o jogo (Java 21).
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY src ./src
RUN mkdir out && javac -d out src/jogo/*.java

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/out ./out
COPY web ./web
ENV DATA_DIR=/app/dados
EXPOSE 8080
CMD ["java", "-Xmx256m", "-cp", "out", "jogo.ServidorJogo"]