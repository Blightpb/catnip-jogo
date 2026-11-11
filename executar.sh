#!/usr/bin/env bash
# Compila e executa o jogo no Linux/macOS (precisa do JDK 11 ou superior).
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -d out src/jogo/*.java
echo "Abra no navegador: http://localhost:8080"
java -cp out jogo.ServidorJogo
