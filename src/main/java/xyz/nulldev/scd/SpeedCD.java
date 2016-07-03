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

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Project: SpeedCD
 * Created: 16/03/16
 * Author: nulldev
 */
public class SpeedCD {

    public static final TextColor HEADER_BCK_COLOR = TextColor.ANSI.GREEN;
    public static final TextColor HEADER_SEARCH_BCK_COLOR = TextColor.ANSI.BLUE;
    public static final TextColor HEADER_FORE_COLOR = TextColor.ANSI.BLACK;
    public static final TextColor SCROLL_INDICATOR_BCK_COLOR = TextColor.ANSI.DEFAULT;
    public static final TextColor SCROLL_INDICATOR_FORE_COLOR = TextColor.ANSI.CYAN;

    public static final TextColor SELECT_BCK_COLOR = TextColor.ANSI.BLUE;
    public static final TextColor SELECT_FORE_COLOR = TextColor.ANSI.BLACK;

    public static final TextColor WARNING_BCK_COLOR = TextColor.ANSI.YELLOW;
    public static final TextColor WARNING_FORE_COLOR = TextColor.ANSI.BLACK;

    public static final TextCharacter SCROLL_LEFT_CHAR =
            new TextCharacter('<').withBackgroundColor(SCROLL_INDICATOR_BCK_COLOR)
                    .withForegroundColor(SCROLL_INDICATOR_FORE_COLOR);

    public static final TextCharacter SCROLL_RIGHT_CHAR =
            new TextCharacter('>').withBackgroundColor(SCROLL_INDICATOR_BCK_COLOR)
                    .withForegroundColor(SCROLL_INDICATOR_FORE_COLOR);

    public static String PATH_CUT_OFF_STRING = "...";
    public static String NO_FILES_WARNING = "This directory is empty!";
    public static String SEARCH_NO_RESULTS_WARNING = "The search returned no results!";
    public static String SEARCH_NO_RESULTS_WARNING_NO_CLEAR = "The search returned no results (or this directory is empty)!";
    public static String SEARCH_PREFIX = "SEARCH: ";

    public static final int MAX_COLUMNS_DIVISOR = 10;
    public static final int MAX_PATH_WIDTH_DIVISOR = 2;

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

    public static HashMap<String, String> parseArgs(String... args) {
        HashMap<String, String> outMap = new HashMap<>();
        for(String arg : args) {
            String s = arg;
            //Strip off dashes
            int dashes = 0;
            for(int i = 0; i < s.length(); i++) {
                if(s.charAt(i) == '-') {
                    dashes++;
                } else {
                    s = s.substring(dashes);
                    break;
                }
            }
            //Remove any quotes
            s = s.replace("\"", "").replace("'", "");
            //Find equal sign
            int equalIndex = s.indexOf('=');
            //Ignore argument if no equal sign
            if(equalIndex == -1) {
                continue;
            }
            String key = s.substring(0, equalIndex).toLowerCase();
            String value = s.substring(equalIndex+1);
            outMap.put(key, value);
        }
        return outMap;
    }

    public static void main(String[] args) {
        //Parse arguments
        HashMap<String, String> argsMap = parseArgs(args);
        String shell = "bash";
        if(argsMap.containsKey("shell")) {
            shell = argsMap.get("shell");
        }
        //Path file from args
        String pathFile = null;
        if(argsMap.containsKey("pathFile")) {
            pathFile = argsMap.get("pathFile");
        } else if(argsMap.containsKey("pathfile")) {
            pathFile = argsMap.get("pathfile");
        }
        //Clear search on directory switch?
        boolean clearSearchOnDirectorySwitch = true;
        if(argsMap.containsKey("clearsearchondirectoryswitch")) {
            clearSearchOnDirectorySwitch = Boolean.parseBoolean("clearsearchondirectoryswitch");
        }
        //Case sensitive search?
        boolean caseSensitiveSearch = true;
        if(argsMap.containsKey("casesensitivesearch")) {
            caseSensitiveSearch = Boolean.parseBoolean("casesensitivesearch");
        }
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

            //Begin program code
            int columns = 3;
            boolean fileTablesDirty = true;
            boolean filesDirty = true;
            int fileTableIndex = 0;
            FileTable[] fileTables = new FileTable[0];
            String fileText;
            int fileTextLength;
            String absolutePath = null;
            File selectionToRestore = null;
            boolean completeRefresh = false;
            boolean showHiddenFiles = true;
            boolean showFiles = true;
            String searchText = null;

            File startingDirectory = new File("").getAbsoluteFile();
            File wd = startingDirectory;
            List<File> fileList = null;

            //Main terminal loop
            boolean exit = false;
            while (!exit) {
                //Full refresh terminal if it has been resized
                TerminalSize size = screen.doResizeIfNecessary();
                if (size == null) {
                    size = screen.getTerminalSize();
                } else {
                    completeRefresh = true;
                    fileTablesDirty = true;
                    if (fileTables.length > 0) {
                        selectionToRestore = fileTables[fileTableIndex].getSelectedFile();
                    }
                }
                //Dirty file tables if the files are dirty
                if (filesDirty) fileTablesDirty = true;
                try {
                    if (filesDirty) {
                        //Generate file list
                        File[] fileArray = wd.listFiles();
                        if (fileArray == null) {
                            fileList = new ArrayList<>();
                        } else {
                            fileList = new ArrayList<>(fileArray.length);
                            Collections.addAll(fileList, fileArray);
                        }
                        absolutePath = wd.getAbsolutePath();
                        //Remove hidden files
                        if (!showHiddenFiles
                                || !showFiles) {
                            Iterator<File> fileIterator = fileList.iterator();
                            while(fileIterator.hasNext()) {
                                File file = fileIterator.next();
                                boolean passedFilter = true;
                                if (!showHiddenFiles && isHidden(file))
                                    passedFilter = false;
                                if (passedFilter && !showFiles && file.isFile())
                                    passedFilter = false;
                                if (!passedFilter) {
                                    fileIterator.remove();
                                }
                            }
                        }
                        //Filter out only files matched by search
                        if (searchText != null) {
                            String lowercaseSearchText;
                            if (!caseSensitiveSearch) {
                                lowercaseSearchText = searchText.toLowerCase();
                            } else {
                                lowercaseSearchText = null;
                            }
                            if (fileTables.length > 0) {
                                Iterator<File> fileIterator = fileList.iterator();
                                while(fileIterator.hasNext()) {
                                    File file = fileIterator.next();
                                    boolean passedFilter = false;
                                    if (caseSensitiveSearch) {
                                        if (file.getName().startsWith(searchText)) {
                                            passedFilter = true;
                                        }
                                    } else {
                                        if (file.getName().toLowerCase().startsWith(lowercaseSearchText)) {
                                            passedFilter = true;
                                        }
                                    }
                                    if(!passedFilter) {
                                        fileIterator.remove();
                                    }
                                }
                            }
                        }
                        //Sort files
                        Collections.sort(fileList);
                        filesDirty = false;
                    }
                    int termHeight = size.getRows();
                    int termHeightWithHeader = termHeight - 1;
                    if (!fileList.isEmpty()) {
                        if (fileTablesDirty) {
                            fileTables = FileTable.generateFileTables(fileList, columns, termHeightWithHeader);
                            fileTablesDirty = false;
                        }
                        //Move cursor if out of bounds
                        if (fileTableIndex >= fileTables.length) {
                            fileTableIndex = fileTables.length - 1;
                            fileTables[fileTableIndex].setCursorIndex(fileTables[fileTableIndex].getMaxFiles() - 1);
                        }
                        //Build right header text
                        StringBuilder builder = new StringBuilder();
                        StringBuilder modifiers = new StringBuilder();
                        if (showHiddenFiles) {
                            modifiers.append("H");
                        }
                        if (showFiles) {
                            modifiers.append("F");
                        }
                        if (modifiers.length() > 0) {
                            builder.append(modifiers);
                            builder.append(" | ");
                        }
                        builder.append(fileList.size()).append(" files | ").append(columns).append(" cols | Page: ")
                                .append(fileTableIndex + 1).append("/").append(fileTables.length);
                        fileText = builder.toString();
                        //Restore selection
                        if (selectionToRestore != null) {
                            for (int fileTableId = 0; fileTableId < fileTables.length; fileTableId++) {
                                if (!fileTables[fileTableId].selectFile(selectionToRestore)) {
                                    fileTableIndex = fileTableId;
                                    break;
                                }
                            }
                            selectionToRestore = null;
                        }
                    } else {
                        fileText = "Empty directory!";
                    }
                    fileTextLength = fileText.length();
                    screen.clear();
                    //Draw file table and scroll indicators
                    int termWidth = size.getColumns();
                    int startX = 0;
                    //Left
                    if (fileTableIndex > 0 && fileTables.length > 1) {
                        startX = 1;
                        //Scroll indicators
                        for (int y = 1; y < termHeight; y++) {
                            screen.setCharacter(0, y, SCROLL_LEFT_CHAR);
                        }
                    }
                    //Right
                    int tableTermWidth = termWidth;
                    if (fileTableIndex < fileTables.length - 1) {
                        tableTermWidth--;
                        //Scroll indicators
                        for (int y = 1; y < termHeight; y++) {
                            screen.setCharacter(termWidth - 1, y, SCROLL_RIGHT_CHAR);
                        }
                    }
                    if (!fileList.isEmpty()) {
                        fileTables[fileTableIndex].drawTable(screen, startX, 1, tableTermWidth);
                    } else {
                        //Draw no files warning!
                        String warning;
                        if (searchText != null) {
                            if (clearSearchOnDirectorySwitch) {
                                warning = SEARCH_NO_RESULTS_WARNING;
                            } else {
                                warning = SEARCH_NO_RESULTS_WARNING_NO_CLEAR;
                            }
                        } else {
                            warning = NO_FILES_WARNING;
                        }
                        int remainingWidth = termWidth - warning.length();
                        int xCenter = (remainingWidth - (remainingWidth % 2)) / 2;
                        int yCenter = (termHeightWithHeader - (termHeightWithHeader % 2)) / 2;
                        for (int x = 0; x < warning.length(); x++) {
                            screen.setCharacter(x + xCenter, yCenter, new TextCharacter(warning.charAt(x))
                                    .withBackgroundColor(WARNING_BCK_COLOR).withForegroundColor(WARNING_FORE_COLOR));
                            screen.setCursorPosition(new TerminalPosition(0, 1));
                        }
                    }
                    //Draw header
                    int maxPathWidth = (termWidth - (termWidth % MAX_PATH_WIDTH_DIVISOR)) / MAX_PATH_WIDTH_DIVISOR;
                    String path;
                    TextColor leftHeaderBackgroundColor;
                    if (searchText != null) {
                        path = SEARCH_PREFIX + searchText;
                        leftHeaderBackgroundColor = HEADER_SEARCH_BCK_COLOR;
                    } else {
                        path = absolutePath;
                        leftHeaderBackgroundColor = HEADER_BCK_COLOR;
                    }
                    if (path.length() > maxPathWidth) {
                        path = PATH_CUT_OFF_STRING +
                                path.substring(path.length() - maxPathWidth + PATH_CUT_OFF_STRING.length());
                    }
                    for (int x = 0; x < termWidth; x++) {
                        TextCharacter character;
                        if (x < path.length()) {
                            character = new TextCharacter(path.charAt(x));
                            character = character.withBackgroundColor(leftHeaderBackgroundColor);
                        } else {
                            if (x > termWidth - fileTextLength - 1) {
                                character = new TextCharacter(fileText.charAt(fileTextLength - (termWidth - x)));
                            } else {
                                character = new TextCharacter(' ');
                            }
                            character = character.withBackgroundColor(HEADER_BCK_COLOR);
                        }
                        character = character.withForegroundColor(HEADER_FORE_COLOR);
                        screen.setCharacter(x, 0, character);
                    }
                    //Perform screen refresh
                    Screen.RefreshType refreshType;
                    if (completeRefresh) {
                        refreshType = Screen.RefreshType.COMPLETE;
                        completeRefresh = false;
                    } else {
                        refreshType = Screen.RefreshType.DELTA;
                    }
                    screen.refresh(refreshType);
                } catch (IOException ignored) {
                }
                //Keystroke processor
                try {
                    KeyStroke stroke = terminal.readInput();
                    if (stroke != null) {
                        //Process character keystrokes
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
                            } else if (stroke.getCharacter().equals('h') && stroke.isCtrlDown()) {
                                //Show hidden files
                                showHiddenFiles = !showHiddenFiles;
                                filesDirty = true;
                            } else if (stroke.getCharacter().equals('f') && stroke.isCtrlDown()) {
                                //Show only files
                                showFiles = !showFiles;
                                filesDirty = true;
                            } else if (!KeyType.Backspace.equals(stroke.getKeyType())
                                    && !KeyType.Enter.equals(stroke.getKeyType())
                                    && (!fileList.isEmpty() || searchText != null)) {
                                //Append to search text
                                if (searchText == null) {
                                    searchText = "";
                                }
                                searchText += stroke.getCharacter();
                                filesDirty = true;
                            }
                        }
                        //Process special keystrokes
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
                                        if (clearSearchOnDirectorySwitch) {
                                            searchText = null;
                                        }
                                        filesDirty = true;
                                    }
                                }
                            }
                            //Backspace and escape keys
                            if (stroke.getKeyType() == KeyType.Backspace) {
                                if (searchText != null) {
                                    //Backspace on search instead
                                    searchText = searchText.substring(0, searchText.length() - 1);
                                    if (searchText.isEmpty()) {
                                        searchText = null;
                                    }
                                    filesDirty = true;
                                } else {
                                    if (wd.getParentFile() != null) {
                                        selectionToRestore = wd;
                                        wd = wd.getParentFile();
                                        if (clearSearchOnDirectorySwitch) {
                                            searchText = null;
                                        }
                                        filesDirty = true;
                                    } else {
                                        //No parent file!
                                        terminal.bell();
                                    }
                                }
                            } else if (stroke.getKeyType() == KeyType.Escape) {
                                if (searchText != null) {
                                    searchText = null;
                                    filesDirty = true;
                                } else {
                                    exit = true;
                                }
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
            } catch (IOException e) {
                System.out.println("ERROR: Error cleaning up terminal!");
                e.printStackTrace();
                System.exit(-1);
                return;
            }
            if(pathFile != null) {
                //Write path to pathfile
                File pathFileRef = new File(pathFile);
                if(!pathFileRef.exists()) {
                    if(!pathFileRef.createNewFile()) {
                        System.out.println("ERROR: Failed to create path file!");
                        System.exit(-1);
                        return;
                    }
                }
                try (PrintStream stream = new PrintStream(new FileOutputStream(pathFileRef))) {
                    stream.print(wd.getAbsolutePath());
                }
            } else {
                try {
                    //Even with stopScreen() the terminal's state is still not completely restored!
                    //We need to restore the STTY!

                    //Use hacky reflection since restoreSTTY is private
                    Method restoreSTTYMethod = terminal.getClass().getDeclaredMethod("restoreSTTY");
                    if(restoreSTTYMethod != null) {
                        restoreSTTYMethod.setAccessible(true);
                        restoreSTTYMethod.invoke(terminal);
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: Error cleaning up terminal!");
                    e.printStackTrace();
                    System.exit(-1);
                    return;
                }
                //Invoke the shell if we are not in the same starting directory
                if (!wd.equals(startingDirectory)) {
                    ProcessBuilder processBuilder = new ProcessBuilder(shell);
                    processBuilder.inheritIO();
                    processBuilder.directory(wd);
                    processBuilder.start().waitFor();
                }
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
