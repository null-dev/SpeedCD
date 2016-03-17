package xyz.nulldev.scd;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Project: SpeedCD
 * Created: 16/03/16
 * Author: nulldev
 */
public class SpeedCD {

    public static final String SHELL = "bash";

    public static final TextColor HEADER_BCK_COLOR = TextColor.ANSI.GREEN;
    public static final TextColor HEADER_FORE_COLOR = TextColor.ANSI.BLACK;
    public static final TextColor SCROLL_INDICATOR_BCK_COLOR = TextColor.ANSI.DEFAULT;
    public static final TextColor SCROLL_INDICATOR_FORE_COLOR = TextColor.ANSI.CYAN;

    public static final TextColor SELECT_BCK_COLOR = TextColor.ANSI.BLUE;
    public static final TextColor SELECT_FORE_COLOR = TextColor.ANSI.BLACK;

    public static final TextColor NO_FILES_BCK_COLOR = TextColor.ANSI.YELLOW;
    public static final TextColor NO_FILE_FORE_COLOR = TextColor.ANSI.BLACK;

    public static final TextCharacter SCROLL_LEFT_CHAR =
            new TextCharacter('<').withBackgroundColor(SCROLL_INDICATOR_BCK_COLOR)
                    .withForegroundColor(SCROLL_INDICATOR_FORE_COLOR);

    public static final TextCharacter SCROLL_RIGHT_CHAR =
            new TextCharacter('>').withBackgroundColor(SCROLL_INDICATOR_BCK_COLOR)
                    .withForegroundColor(SCROLL_INDICATOR_FORE_COLOR);

    public static String PATH_CUT_OFF_STRING = "...";
    public static String NO_FILES_WARNING = "This directory is empty!";

    static final int MAX_COLUMNS_DIVISOR = 10;
    static final int MAX_PATH_WIDTH_DIVISOR = 2;

    public static boolean isHidden(File file) {
        String fileName = file.getName();
        if(fileName.length() > 0) {
            char firstChar = fileName.charAt(0);
            char lastChar = fileName.charAt(fileName.length() - 1);
            if(firstChar == '.' || lastChar == '~') {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        //Create terminal
        Terminal terminal;
        Screen screen = null;
        try {
            try {
                terminal = new DefaultTerminalFactory().createTerminal();
                screen = new TerminalScreen(terminal);
            } catch (IOException e) {
                System.out.println("ERROR: Error creating terminal/screen!");
                e.printStackTrace();
                System.exit(-1);
                return;
            }
            try {
                screen.startScreen();
                screen.clear();
                if (0 != 0) throw new IOException();
            } catch (IOException e) {
                System.out.println("ERROR: Error setting up terminal!");
                e.printStackTrace();
                System.exit(-1);
                return;
            }

            int columns = 3;
            boolean fileTablesDirty = true;
            boolean filesDirty = true;
            int fileTableIndex = 0;
            FileTable[] fileTables = new FileTable[0];
            String fileText;
            int fileTextLength;
            String absolutePath = null;
            boolean completeRefresh = false;
            boolean showHiddenFiles = true;
            boolean showFiles = true;

            File startingDirectory = new File("").getAbsoluteFile();
            File wd = startingDirectory;
            File[] fileList = null;

            boolean exit = false;
            while (!exit) {
                TerminalSize size = screen.doResizeIfNecessary();
                if (size == null) {
                    size = screen.getTerminalSize();
                } else {
                    completeRefresh = true;
                    fileTablesDirty = true;
                }
                if (filesDirty) fileTablesDirty = true;
                try {
                    if (filesDirty) {
                        fileList = wd.listFiles();
                        if (fileList == null) {
                            fileList = new File[0];
                        }
                        absolutePath = wd.getAbsolutePath();
                        //Remove hidden files
                        if(!showHiddenFiles
                                || !showFiles) {
                            ArrayList<File> fileArrayList = new ArrayList<>();
                            for(File file : fileList) {
                                boolean passedFilter = true;
                                if(!showHiddenFiles && isHidden(file))
                                    passedFilter = false;
                                if(passedFilter && !showFiles && file.isFile())
                                    passedFilter = false;
                                if(passedFilter)
                                    fileArrayList.add(file);
                            }
                            fileList = fileArrayList.toArray(new File[fileArrayList.size()]);
                        }
                        Arrays.sort(fileList);
                        filesDirty = false;
                    }
                    int termHeight = size.getRows();
                    int termHeightWithHeader = termHeight - 1;
                    if (fileList.length > 0) {
                        if (fileTablesDirty) {
                            fileTables = FileTable.generateFileTables(fileList, columns, termHeightWithHeader);
                            fileTablesDirty = false;
                        }
                        //Move cursor if out of bounds
                        if (fileTableIndex >= fileTables.length) {
                            fileTableIndex = fileTables.length - 1;
                            fileTables[fileTableIndex].setCursorIndex(fileTables[fileTableIndex].getMaxFiles() - 1);
                        }
                        StringBuilder builder = new StringBuilder();
                        StringBuilder modifiers = new StringBuilder();
                        if(showHiddenFiles) {
                            modifiers.append("H");
                        }
                        if(showFiles) {
                            modifiers.append("F");
                        }
                        if(modifiers.length() > 0) {
                            builder.append(modifiers);
                            builder.append(" | ");
                        }
                        builder.append(fileList.length).append(" files | ").append(columns).append(" cols | Page: ")
                                .append(fileTableIndex + 1).append("/").append(fileTables.length);
                        fileText = builder.toString();
                    } else {
                        fileText = "Empty directory!";
                    }
                    fileTextLength = fileText.length();
                    screen.clear();
                    //Draw file table and scroll indicators
                    int termWidth = size.getColumns();
                    int startX = 0;
                    if (fileTableIndex > 0 && fileTables.length > 1) {
                        startX = 1;
                        //Scroll indicators
                        for (int y = 1; y < termHeight; y++) {
                            screen.setCharacter(0, y, SCROLL_LEFT_CHAR);
                        }
                    }
                    int tableTermWidth = termWidth;
                    if (fileTableIndex < fileTables.length - 1) {
                        tableTermWidth--;
                        //Scroll indicators
                        for (int y = 1; y < termHeight; y++) {
                            screen.setCharacter(termWidth - 1, y, SCROLL_RIGHT_CHAR);
                        }
                    }
                    if (fileList.length > 0) {
                        fileTables[fileTableIndex].drawTable(screen, startX, 1, tableTermWidth);
                    } else {
                        //Draw no files warning!
                        int remainingWidth = termWidth - NO_FILES_WARNING.length();
                        int xCenter = (remainingWidth - (remainingWidth % 2)) / 2;
                        int yCenter = (termHeightWithHeader - (termHeightWithHeader % 2)) / 2;
                        for (int x = 0; x < NO_FILES_WARNING.length(); x++) {
                            screen.setCharacter(x + xCenter, yCenter, new TextCharacter(NO_FILES_WARNING.charAt(x))
                                    .withBackgroundColor(NO_FILES_BCK_COLOR).withForegroundColor(NO_FILE_FORE_COLOR));
                            screen.setCursorPosition(new TerminalPosition(0, 1));
                        }
                    }
                    //Draw header
                    int maxPathWidth = (termWidth - (termWidth % MAX_PATH_WIDTH_DIVISOR)) / MAX_PATH_WIDTH_DIVISOR;
                    String path = absolutePath;
                    if (path.length() > maxPathWidth) {
                        path = PATH_CUT_OFF_STRING +
                                path.substring(path.length() - maxPathWidth + PATH_CUT_OFF_STRING.length());
                    }
                    for (int x = 0; x < termWidth; x++) {
                        TextCharacter character;
                        if (x < path.length()) {
                            character = new TextCharacter(path.charAt(x));
                        } else if (x > termWidth - fileTextLength - 1) {
                            character = new TextCharacter(fileText.charAt(fileTextLength - (termWidth - x)));
                        } else {
                            character = new TextCharacter(' ');
                        }
                        character =
                                character.withBackgroundColor(HEADER_BCK_COLOR).withForegroundColor(HEADER_FORE_COLOR);
                        screen.setCharacter(x, 0, character);
                    }
                    Screen.RefreshType refreshType;
                    if(completeRefresh) {
                        refreshType = Screen.RefreshType.COMPLETE;
                        completeRefresh = false;
                    } else {
                        refreshType = Screen.RefreshType.DELTA;
                    }
                    screen.refresh(refreshType);
                } catch (IOException ignored) {
                }
                try {
                    KeyStroke stroke = terminal.readInput();
                    //Increment column counts
                    if (stroke != null) {
                        if (stroke.getCharacter() != null) {
                            if (stroke.getCharacter().equals('-')) {
                                if (columns > 1) {
                                    columns--;
                                    fileTablesDirty = true;
                                }
                            } else if (stroke.getCharacter().equals('+') || stroke.getCharacter().equals('=')) {
                                int screenColumns = size.getColumns();
                                int maxColumns =
                                        (screenColumns - (screenColumns % MAX_COLUMNS_DIVISOR)) / MAX_COLUMNS_DIVISOR;
                                if (columns < maxColumns) {
                                    columns++;
                                    fileTablesDirty = true;
                                }
                            } else if (stroke.getCharacter().equals('r') && stroke.isCtrlDown()) {
                                //Refresh files
                                filesDirty = true;
                            } else if(stroke.getCharacter().equals('h') && stroke.isCtrlDown()) {
                                //Show hidden files
                                showHiddenFiles = !showHiddenFiles;
                                filesDirty = true;
                            } else if(stroke.getCharacter().equals('f') && stroke.isCtrlDown()) {
                                showFiles = !showFiles;
                                filesDirty = true;
                            } else {
                                //Select file starting with letter
                                if (fileTables.length > 0) {
                                    char c = stroke.getCharacter();
                                    boolean found = false;
                                    //First search for original case version
                                    for (int table = 0; table < fileTables.length; table++) {
                                        if (!fileTables[table].selectFile(c)) {
                                            fileTableIndex = table;
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        for (int table = 0; table < fileTables.length; table++) {
                                            //Try upper and lower case
                                            if (fileTables[table].selectFile(Character.toUpperCase(c))) {
                                                if (!fileTables[table].selectFile(Character.toLowerCase(c))) {
                                                    fileTableIndex = table;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (stroke.getKeyType() != null) {
                            if (fileTables.length > 0) {
                                if (stroke.getKeyType() == KeyType.ArrowLeft) {
                                    boolean nextPage = fileTables[fileTableIndex].cursorLeft();
                                    if (nextPage) {
                                        if (fileTableIndex > 0) fileTableIndex--;
                                    }
                                } else if (stroke.getKeyType() == KeyType.ArrowRight) {
                                    boolean nextPage = fileTables[fileTableIndex].cursorRight();
                                    if (nextPage) {
                                        if (fileTableIndex < fileTables.length - 1) fileTableIndex++;
                                    }
                                } else if (stroke.getKeyType() == KeyType.ArrowUp) {
                                    boolean nextPage = fileTables[fileTableIndex].cursorUp();
                                    if (nextPage) {
                                        if (fileTableIndex > 0) fileTableIndex--;
                                    }
                                } else if (stroke.getKeyType() == KeyType.ArrowDown) {
                                    boolean nextPage = fileTables[fileTableIndex].cursorDown();
                                    if (nextPage) {
                                        if (fileTableIndex < fileTables.length - 1) fileTableIndex++;
                                    }
                                } else if (stroke.getKeyType() == KeyType.Enter) {
                                    File selectedFile = fileTables[fileTableIndex].getSelectedFile();
                                    if (selectedFile != null && selectedFile.isDirectory()) {
                                        wd = selectedFile;
                                        filesDirty = true;
                                    }
                                }
                            }
                            if (stroke.getKeyType() == KeyType.Backspace) {
                                if (wd.getParentFile() != null) {
                                    wd = wd.getParentFile();
                                    filesDirty = true;
                                }
                            } else if (stroke.getKeyType() == KeyType.Escape) {
                                exit = true;
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("ERROR: Error getting input!");
                    e.printStackTrace();
                    System.exit(-1);
                    return;
                }
            }

            try {
                screen.stopScreen();
                //Even with stopScreen() the terminal's state is still not completely restored!
                //We need to restore the STTY!

                //Use hacky reflection since restoreSTTY is private
                Method restoreSTTYMethod = terminal.getClass().getDeclaredMethod("restoreSTTY");
                restoreSTTYMethod.setAccessible(true);
                restoreSTTYMethod.invoke(terminal);
            } catch (IOException e) {
                System.out.println("ERROR: Error cleaning up terminal!");
                e.printStackTrace();
                System.exit(-1);
                return;
            }
            //Invoke the shell if we are not in the same starting directory
            if(!wd.equals(startingDirectory)) {
                ProcessBuilder processBuilder = new ProcessBuilder(SHELL);
                processBuilder.inheritIO();
                processBuilder.directory(wd);
                processBuilder.start().waitFor();
            }
            //Print path to shell
            //            System.out.println(wd.getAbsolutePath());
        } catch (Throwable t) {
            if (screen != null) {
                try {
                    screen.stopScreen();
                } catch (IOException ignored) {
                }
            }
            System.out.println("ERROR: Uncaught exception!");
            t.printStackTrace();
        }
    }
}
