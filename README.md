# GAME MODULE: Virus Hunt (Maze Adventure)

AUTHOR:  Artur Sanamyan
**JAVA VERSION:** Java 21

### PROJECT INFO:
This game was created as part of a 3rd-semester university project on the topic of **"dunkelIT"**. Each project group was tasked with developing a shared game collection containing a total of 6 different mini-games. "Virus Hunt" is one of the 6 modules contributed to this collection.



### INSTALLATION / INTEGRATION:
1. Copy the packages `game`, `launcher`, and `logic` into the source folder of the game collection.
2. Ensure that the `resources` folder is located in the classpath (e.g., under `src/resources`).

### HOW TO START:
- **Main Class:** `launcher.Launcher`
- **Integration:** The game is a JPanel (`MazeGame.java`). 
  It can be instantiated using: `new MazeGame(launcherReference, level, playerName)`.
- The Launcher (`launcher.Launcher`) serves as a wrapper for isolated testing and deliberately uses `DISPOSE_ON_CLOSE`.

### FEATURES:
- **3 Difficulty Levels** (Easy, Medium, Hard)
- **Integrated Sound & Music**
- **Cheat Code:** Simply type `GODMODE` on your keyboard during gameplay.

### IMPORTANT:
`System.exit(0)` was intentionally removed from the code so that closing this individual game does not accidentally terminate the entire game collection.
