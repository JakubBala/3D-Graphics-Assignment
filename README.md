

Compilation Instructions:

Run commands in order:

(Navigate to project root)

mkdir bin

powershell -Command "javac -cp 'lib\jogamp-fat.jar;lib\snakeyaml-2.5.jar' -d bin (Get-ChildItem -Recurse src/main/java/*.java, assets/models/*.java | ForEach-Object { $_.FullName })"

java --add-exports java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.java2d=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED -cp "bin;lib\jogamp-fat.jar;lib\snakeyaml-2.5.jar" engine.Buzz