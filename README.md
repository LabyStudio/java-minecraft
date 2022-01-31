# Minecraft recode in Java

This is a sandbox that provides all basic features of Minecraft.<br>
The main purpose of this project is to understand the render and physics engine of Minecraft.<br>
It is a fork of the [first version](https://github.com/thecodeofnotch/rd-131655) of Minecraft.

### Feature Overview
- Block rendering
- Block collision
- Player movement
    - Walking
    - Sprinting
    - Sneaking
    - Flying
    - Swimming
- Dynamic lightning
- Smooth lightning
- Anvil world loading/saving
- Perlin world generation
- Frustum Culling
- Fog
- Underwater fog
- HUD rendering
    - Cross-hair
    - Font rendering
- Dynamic FOV

![Ingame](.artwork/ingame.png)

### Setup
- Clone the project
- Set your working directory to ``./run``
- Run main class ``de.labystudio.game.Minecraft``
- Wait a few seconds for the world generation

### Controls
```
W: Forward
S: Backwards
A: Left
D: Right

Left click: Destroy block
Right click: Place block
Middle click: Pick block

Space: Jump
Double Space: Toggle flying
Q: Sneaking
Shift: Sprinting
ESC: Toggle game focus

R: Return to spawn
```

### Smooth lightning example
![Smooth Lightning](.artwork/smooth_lightning.png)

### Known issues
- Mouse over block calculation is acting weird and doesn't work sometimes
- No light updates during world generation

### Planned
- Generate new chunk if not generated yet (Infinite map)
- Multiplayer
- Entity rendering
- Loading screen