#!/usr/bin/env bash
set -e

echo "=========================================="
echo " Painel de Threads - Sem Maven"
echo "=========================================="

rm -rf target
mkdir -p target/classes

javac -encoding UTF-8 -d target/classes src/main/java/mercado/*.java
cp -R src/main/resources/* target/classes/

echo "Servidor iniciado em: http://localhost:8081"
echo "Para parar, aperte CTRL + C neste terminal."
java -cp target/classes mercado.ServidorWeb
