# <i>Speed</i>CD #
###### A fast and intuitive alternative to the UNIX "cd" command

## About ##
SpeedCD is a CLI program that allows you to navigate any filesystem at the speed of light!
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
Use the arrow keys to move the cursor.<br>
Press: <kbd>ESC</kbd> to open a `Bash` shell in the current directory<br>

## Installation ##
1. Download a binary from the [releases](https://github.com/null-dev/SpeedCD/releases) section.
2. Create an alias to the jar file by adding `alias scd="java -jar /path/to/the/binary/SpeedCD.jar"` to the end or your `~/.bashrc` file.
3. Restart your terminal/shell.
4. You are ready to go!