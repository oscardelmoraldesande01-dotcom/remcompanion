# Rem Companion (Forge 1.20.1 - 47.4.0)

## Compilar
1. Descarga el MDK de Forge 1.20.1-47.4.0 (files.minecraftforge.net) y copia de él a esta carpeta:
   `gradlew`, `gradlew.bat` y la carpeta `gradle/`.
2. Necesitas JDK 17.
3. Ejecuta `./gradlew build` (Windows: `gradlew build`).
4. El mod queda en `build/libs/remcompanion-1.20.1-1.0.0.jar` -> a tu carpeta `mods`.
   Para probar sin empaquetar: `./gradlew runClient`.

## Uso en el juego
- Pestaña creativa "Huevos generadores" -> Huevo generador de Rem.
- Al principio desconfía. Dale comida o flores (clic derecho) y se vuelve tu compañera.
- Clic derecho con la mano vacía: hablar (+afecto cada 2 min).
- Regalos (1 por minuto): comida +8, flores +18, tarta/galleta/manzana dorada/etc. +30.
- Agacharte + clic derecho: que se quede quieta / te siga.
- Más afecto = más daño (6->14) y más vida (40->60). Con 300+ te cura si estás por debajo del 50% de vida.
- Ataca a monstruos hostiles cercanos (menos creepers) y a lo que te ataque o ataques.
