SPIELMODUL: Virenjagd (Maze Adventure)
AUTOR: [Dein Name]
JAVA VERSION: Java 17 (oder deine Version)

INSTALLATION / INTEGRATION:
1. Kopieren Sie das Package "game", "launcher" und "logic" in den Source-Ordner der Spielsammlung.
2. Stellen Sie sicher, dass der Ordner "resources" im Classpath liegt (z.B. unter src/resources).

STARTEN:
- Hauptklasse: launcher.Launcher
- Integration: Das Spiel ist ein JPanel (MazeGame.java). 
  Es kann instanziiert werden mit: "new MazeGame(launcherReferenz, level, spielerName)".
- Der Launcher ("launcher.Launcher") dient als Wrapper zum Testen und nutzt DISPOSE_ON_CLOSE.

FEATURES:
- 3 Schwierigkeitsgrade (Easy, Medium, Hard)
- Sound & Musik integriert
- Cheat-Code: Tippen Sie "GODMODE" während des Spiels.

WICHTIG:
System.exit(0) wurde entfernt, um die Spielsammlung nicht zu beenden.
