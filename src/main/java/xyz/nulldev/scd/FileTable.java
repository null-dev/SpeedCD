package xyz.nulldev.scd;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;

import java.io.File;


/**
 * Project: SpeedCD
 * Created: 16/03/16
 * Author: nulldev
 */
public class FileTable {

    public static TextColor FILE_BCK_COLOR = TextColor.ANSI.DEFAULT;
    public static TextColor FILE_FORE_COLOR = TextColor.ANSI.RED;

    public static TextColor DIR_BCK_COLOR = TextColor.ANSI.DEFAULT;
    public static TextColor DIR_FORE_COLOR = TextColor.ANSI.GREEN;

    public static String CUT_OFF_STRING = "...";

    int cursorIndex;
    File[][] fileTable;
    int columns;
    int rows;
    int lastColumnWithFiles;
    int maxFiles;

    public static FileTable[] generateFileTables(File[] files, int columns, int rows) {
        int extraFiles = files.length % rows;
        int requiredColumns = (files.length - extraFiles) / rows;
        if (extraFiles > 0) requiredColumns++;
        int extraColumns = requiredColumns % columns;
        int requiredTables = (requiredColumns - extraColumns) / columns;
        if (extraColumns > 0) requiredTables++;
        FileTable[] tables = new FileTable[requiredTables];
        File[][] currentFileArray = null;
        int table = 0;
        int column = 0;
        int row = 0;
        for (File file : files) {
            if (row >= rows) {
                row = 0;
                column++;
                if (column >= columns) {
                    column = 0;
                    tables[table] = new FileTable(0, currentFileArray, columns, rows);
                    currentFileArray = null;
                    table++;
                }
            }
            if (currentFileArray == null) {
                currentFileArray = new File[columns][rows];
            }
            currentFileArray[column][row] = file;
            row++;
        }
        tables[table] = new FileTable(0, currentFileArray, columns, rows);
        return tables;
    }

    public FileTable(int cursorIndex, File[][] fileTable, int columns, int rows) {
        this.cursorIndex = cursorIndex;
        this.fileTable = fileTable;
        this.columns = columns;
        this.rows = rows;
        //Count max files
        this.maxFiles = 0;
        this.lastColumnWithFiles = 1;
        for (File files[] : fileTable) {
            for (File file : files) {
                if (file != null) {
                    if (maxFiles > rows * lastColumnWithFiles) {
                        lastColumnWithFiles++;
                    }
                    maxFiles++;
                }
            }
        }
    }

    public boolean selectFile(char c) {
        int fileIndex = 0;
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                if (fileTable[x][y] == null) continue;
                String fname = fileTable[x][y].getName();
                if (fname.length() > 1 && fname.charAt(0) == c) {
                    cursorIndex = fileIndex;
                    return false;
                }
                fileIndex++;
            }
        }
        return true;
    }

    public boolean cursorDown() {
        if (cursorIndex < maxFiles - 1) {
            cursorIndex++;
        } else {
            return true;
        }
        return false;
    }

    public boolean cursorUp() {
        if (cursorIndex > 0) {
            cursorIndex--;
        } else {
            return true;
        }
        return false;
    }

    public boolean cursorRight() {
        int leftOver = cursorIndex % rows;
        int cursorLane = (cursorIndex - leftOver) / rows;
        if (cursorLane < lastColumnWithFiles - 1) {
            cursorIndex += rows;
            if (cursorIndex > maxFiles) {
                cursorIndex = maxFiles - 1;
            }
            return false;
        } else {
            return true;
        }
    }

    public boolean cursorLeft() {
        int leftOver = cursorIndex % rows;
        int cursorLane = (cursorIndex - leftOver) / rows;
        if (cursorLane > 0) {
            cursorIndex -= rows;
            return false;
        } else {
            return true;
        }
    }

    public int getCursorIndex() {
        return cursorIndex;
    }

    public void setCursorIndex(int cursorIndex) {
        this.cursorIndex = cursorIndex;
    }

    public int getLastColumnWithFiles() {
        return lastColumnWithFiles;
    }

    public void setLastColumnWithFiles(int lastColumnWithFiles) {
        this.lastColumnWithFiles = lastColumnWithFiles;
    }

    public int getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }

    public File getSelectedFile() {
        int fileIndex = 0;
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                if (fileTable[x][y] == null) continue;
                if (fileIndex == cursorIndex) {
                    return fileTable[x][y];
                }
                fileIndex++;
            }
        }
        return null;
    }

    public void drawTable(Screen screen, int oX, int oY, int width) {
        //Leave space between each column
        width -= columns - 1;
        int remainingWidth = width % columns;
        int perColumnsWidth = (width - remainingWidth) / columns;
        int fileIndex = 0;
        for (int x = 0; x < columns; x++) {
            int startX = x * perColumnsWidth;
            for (int y = 0; y < rows; y++) {
                File file = fileTable[x][y];
                if (file == null) {
                    continue;
                }
                String name = file.getName();
                if (name.length() > perColumnsWidth) {
                    name = name.substring(0, perColumnsWidth - (1 + CUT_OFF_STRING.length())) + CUT_OFF_STRING;
                }
                for (int rx = 0; rx < name.length(); rx++) {
                    TextCharacter character = new TextCharacter(name.charAt(rx));
                    if (fileIndex == cursorIndex) {
                        character = character.withBackgroundColor(SpeedCD.SELECT_BCK_COLOR)
                                .withForegroundColor(SpeedCD.SELECT_FORE_COLOR);
                        screen.setCursorPosition(new TerminalPosition(oX + startX, oY + y));
                    } else {
                        if (file.isDirectory()) {
                            character = character.withBackgroundColor(FileTable.DIR_BCK_COLOR)
                                    .withForegroundColor(FileTable.DIR_FORE_COLOR);
                        } else if (file.isFile()) {
                            character = character.withBackgroundColor(FileTable.FILE_BCK_COLOR)
                                    .withForegroundColor(FileTable.FILE_FORE_COLOR);
                        }
                    }
                    screen.setCharacter(oX + rx + startX, oY + y, character);
                }
                fileIndex++;
            }
        }
    }
}
