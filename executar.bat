@echo off
REM Compila e executa o jogo no Windows (precisa do JDK 11 ou superior).
if not exist out mkdir out
javac -d out src\jogo\*.java
if errorlevel 1 (
  echo Erro ao compilar.
  pause
  exit /b 1
)
echo Abra no navegador: http://localhost:8080
java -cp out jogo.ServidorJogo
pause
