# <i>Speed</i>CD #
###### A fast and intuitive alternative to the UNIX "cd" command

## About ##
SpeedCD is a CLI program that allows you to navigate any filesystem at the speed of light!
SpeedCD is **not** a file manager, it will not open/manipulate files.
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
Type anything to search for files in the current directory.<br>
Press: <kbd>ESC</kbd> to open a shell in the current directory (if you have a search running, this will cancel the search).<br>

## Configuration ##
Configuration settings are available by passing in command-line arguments:
* `--shell=`: Specify the shell to be opened when <kbd>ESC</kbd> is pressed. Example: `--shell=zsh`
* `--pathFile=`: Specify a file to write the resulting path to when <kbd>ESC</kbd> is pressed. Overrides `--shell=` Example: `--pathFile=/tmp/speedcd.123456.tmp`
* `--clearSearchOnDirectorySwitch`: `true` or `false`, specifies whether or not to clear the current search (if any) when you move into a new directory. Defaults to `true`.
* `--caseSensitiveSearch`: `true` or `false`, specifies whether or not searching is case sensitive.

## Installation ##
1. Download and install the Java 7 runtime.
2. Download a binary from the [releases](https://github.com/null-dev/SpeedCD/releases) section.
3. Create an alias to the jar file by adding `APTB="---> REPLACE ME <---";scd() { t=$(mktemp /tmp/speedcd.XXXXXX);java -jar "$APTB" "--pathFile=$t";s=$(cat "$t");cd "$s";rm "$t";}` to the end of your `~/.bashrc` file (replace `---> REPLACE ME <---` with the path to the SpeedCD binary).
4. Restart your terminal/shell.
5. You are ready to go!

## Libraries Used ##
The only library used is [lanterna](https://github.com/mabe02/lanterna).
