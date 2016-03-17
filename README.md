# <i>Speed</i>CD #
###### A fast and intuitive alternative to the UNIX "cd" command

## About ##
SpeedCD is a CLI program that allows you to navigate any filesystem at the speed of light!
SpeedCD is **not** a file manager, it will not open/maniplate files and folders.
Check out a demo here (ignore the flickering, it's because of the recording software):
[![asciicast](https://asciinema.org/a/2wedxpqaf8nsojx9lep5ki9zv.png)](https://asciinema.org/a/2wedxpqaf8nsojx9lep5ki9zv)
The usage of SpeedCD starts at: [0:27](https://asciinema.org/a/2wedxpqaf8nsojx9lep5ki9zv?t=0:27)

## Usage ##
SpeedCD is designed to be as intuitive as possible so instructions are not really necessary (but here is one anyways):

Press: <kbd>+</kbd> to add a column (SpeedCD defaults to 3 columns on startup)<br>
Press: <kbd>-</kbd> to remove a column<br>
Press: <kbd>Backspace</kbd> to go up the directory tree<br>
Press: <kbd>Enter</kbd> to enter a directory<br>
Press: <kbd>CTRL</kbd>+<kbd>r</kbd> to refresh the view (if files have been added/removed from the directory)<br>
Press: <kbd>CTRL</kbd>+<kbd>h</kbd> to hide/show hidden files<br>
Press: <kbd>CTRL</kbd>+<kbd>f</kbd> to hide/show files<br>
Use the arrow keys to move the cursor.<br>
Press any letter/number on the keyboard to jump to the first file that starts with that letter/number.
Press: <kbd>ESC</kbd> to open a `Bash` shell in the current directory<br>

## Installation ##
1. Download and install the Java 7 runtime.
2. Download a binary from the [releases](https://github.com/null-dev/SpeedCD/releases) section.
3. Create an alias to the jar file by adding `alias scd="java -jar /path/to/the/binary/SpeedCD.jar"` to the end or your `~/.bashrc` file.
4. Restart your terminal/shell.
5. You are ready to go!

## Libraries Used ##
The only library used is [lanterna](https://github.com/mabe02/lanterna). It is embedded into the projects because it had to be modified.
