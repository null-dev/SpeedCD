# Place this in your .bashrc or shell startup file
# ================[SpeedCD Shell Integration Code]================
ABSOLUTE_PATH_TO_BINARY="---> REPLACE ME <---"
scd() {
    tmpfile=$(mktemp /tmp/speedcd.XXXXXX)
    java -jar "$ABSOLUTE_PATH_TO_BINARY" "--pathFile=$tmpfile"
    scdPath=$(cat "$tmpfile")
    cd "$scdPath"
    rm "$tmpfile"
}