TBS2
====
TBS2 is (working title) an Advance Wars inspired game. Game play consists of two players battling each other on a 2D top down grid. Players can capture cities and factories, which both provide resources that are used to create units. This game is currently still in development (more info about current state can be found at http://www.cs.utexas.edu/~mrayer/projects.html). Below is further information about the game/etc. If you have any further questions that are not answered in this README, feel free to contact me. 

Basic Game Info:
================
- Use Launcher.java to launch the game
- Game has been most heavily tested on a 1080p display, but it should scale to fit most displays, custom maps may not transfer well between different resolutions at this time
- Launcher.java also contains a for loop which can be used to automate the creation of a new game once a game is complete, simply set the for loops max value to 1 to disable this
- Use right click to end your turn

The AI:
=======

The AI of the game can found in com.rolledback.packages. To battle against the AI simply set one of the teams in Game.java to the basic Team object, and then the other team to one of the various ComputerTeam classes. AIs can also fight against each other. To accomplish this simply have both teams be one of the ComputerTeam classes. AIs also doesn't have to be used at all. Lastly, to control the speed of an AIs animation, change the animationDelay variable in the file of the AI whose speed you wish to change.

Map Maker/Custom Maps:
======================
TBS2 includes a map maker program, and the ability to play your own custom maps. To start the map maker, launch the MapMaker.java file. Various map maker controls inlude:
- Esc: bring up menu to save/open files
- r: generate a random map (including rivers/factories/cities)
- t: genearate a random map (onlly terrain)
- f: fill the map with the currently selected texture
- right click: display the texture picker window (incase you closed it)

The default save location for maps when the program is not a jar is the src folder "maps". Once the game is compiled into a jar, the game will look for maps in a folder in the jar's directory also called maps. This folder can either be created and populated, or you can copy the original maps folder out of the workspace to the jars directory. An installer script is currently in the works. An exploration of the graphics directory reveals the existence of many images that do not appear anywhere in the game. At this time none of those images are supported by the game or the editor, although they may be supported in the future.

If you wish to create your own map editor, you must use the following file format:
- All map files must end with .map
- The first byte of the file must be 0x6D
- The next byte indicates the tile size of the map
- The next 4 bytes are used to store the actual size of the map, 2 for width, 2 for height
- The rest of the bytes are used as follows:
  - 00000000 = plains
  - 00000001 = forest
  - 00000010 = mountain 
  - 00000011 = river_horizontal
  - 00010011 = river_vertical
  - 00100011 = riverEnd_up
  - 00110011 = riverEnd_right
  - 01000011 = riverEnd_down
  - 01010011 = riverEnd_left
  - 01100011 = riverCorner_one
  - 01110011 = riverCorner_two
  - 10000011 = riverCorner_three
  - 10010011 = riverCorner_four
  - 00000100 = bridge_horizontal
  - 00010100 = bridge_vertical 
  - 00000101 = cityGrey
  - 00010101 = cityRed
  - 00100101 = cityBlue
  - 00000110 = factoryGrey
  - 00010110 = factoryRed
  - 00100110 = factoryBlue

License Information
====================
This project is licensed under the Creative Commons Attribution-NonCommercial License. For details on what this means, please see the link below.
- https://creativecommons.org/licenses/by-nc/4.0/
