#!/usr/bin/env sh
# Compila y ejecuta el servidor (requiere JDK 17+). Uso: ./run.sh [puerto]
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out src/taskflow/*.java
exec java -cp out taskflow.Main "$@"
