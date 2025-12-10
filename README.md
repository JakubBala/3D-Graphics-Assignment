

Compilation Instructions:

Run commands in order:

(Navigate to project root, where this README is)

mkdir bin

powershell -Command "javac -cp 'lib\jogamp-fat.jar;lib\snakeyaml-2.5.jar' -d bin (Get-ChildItem -Recurse src/main/java/*.java, assets/models/*.java | ForEach-Object { $_.FullName })"

java --add-exports java.base/java.lang=ALL-UNNAMED --add-exports java.desktop/sun.java2d=ALL-UNNAMED --add-exports java.desktop/sun.awt=ALL-UNNAMED -cp "bin;lib\jogamp-fat.jar;lib\snakeyaml-2.5.jar" engine.Buzz



Sourced Textures

PolyHaven: https://polyhaven.com/
Licensing: https://polyhaven.com/license
Textures:


Cactus:
https://www2.hm.com/en_gb/productpage.1145562001.html?srsltid=AfmBOorhcBlqbvfUZv26l5wq0ZgodcBtARdunHXRrLWuyvrmPxKTPD5P

Shrubs:
https://opengameart.org/content/plants-textures-pack-02