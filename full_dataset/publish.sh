#!/bin/bash

# Base directory containing the files
BASE_DIR=""

declare -A task_map
task_map["1"]="BEO"
task_map["2"]="CLH"
task_map["3"]="CRH"
task_map["4"]="DLF"
task_map["5"]="PLF"
task_map["6"]="DRF"
task_map["7"]="PRF"
task_map["8"]="Rest"

get_task_from_identifier() {
    local identifier=$1
    local task="${task_map[$identifier]}"
    if [[ -z "$task" ]]; then
        task="Unknown"  # If no match, mark as Unknown
        echo "No matching task was found for $identifier. Exiting..."
        exit 1
    fi
    echo "$task"
}

# Ask the user if they want to filter by subject
read -p "Do you want to select a specific subject? (yes/no): " RESPONSE

if [[ "$RESPONSE" =~ ^(yes|y)$ ]]; then
    # Prompt for specific subject details
    read -p "Enter the subject pattern (e.g., S1R1I1): " FILE

    # Extract the first two characters to determine the directory
    SUBJECT=${FILE:0:2}
    DIR="${BASE_DIR}${SUBJECT}"

    # Check if the directory exists
    if [[ ! -d "$DIR" ]]; then
        echo "Directory $DIR does not exist."
        exit 1
    fi

    # Change to the specific directory
    cd "$DIR" || exit

    # List files matching the subject pattern
    echo "Files matching the pattern '${FILE}.csv' in directory ${DIR}:"
    ls "${FILE}.csv" 2>/dev/null

    # Check if no files were found
    if [[ $? -ne 0 ]]; then
        echo "No files matching the pattern '${FILE}_*.csv' were found in $DIR."
        exit 1
    fi

    number_of_lines=$(wc -l < "${FILE}.csv")

    # Extract the identifier (M1, I1, etc.) from the filename
    identifier=$(echo "$FILE" | grep -o -E '[IM][0-9]{1,2}' | head -n 1)
    task_identifier="${identifier:1}"
    new_fields=$(get_task_from_identifier "$task_identifier")

    # Check if the filename contains 'I' or 'M' and append it after the task identifier field
    if [[ "$FILE" =~ "I" ]]; then
        new_fields="$new_fields,I"
    elif [[ "$FILE" =~ "M" ]]; then
        new_fields="$new_fields,M"
    fi

    # Extract the repetition number after "R" from the filename
    r_number=$(echo "$FILE" | grep -o -E 'R([0-9]{1,2})' | sed 's/R//')
    new_fields="$new_fields,$r_number"

    # Extract the subject number from the file name
    s_number=$(echo "$FILE" | grep -o -E 'S([0-9]{1,2})' | sed 's/S//')
    new_fields="$new_fields,$s_number"

    for ((line=2; line<=$number_of_lines; line++))
    do
        current_line=$(sed -n "${line}p" "${FILE}.csv" | tr -d '\r' | sed 's/[[:space:]]*$//')
        current_line="${current_line},$(date +%s%3N)"
        current_line=$(echo "$current_line" | sed "s/\([^,]*\),/\1,$new_fields,/")
        echo $current_line
        echo $current_line | kafka-console-producer --topic brain_data --bootstrap-server 192.168.1.76:9092
        sleep 0.1
    done

else
    # Process all directories and files
    echo "Processing all files in all directories under $BASE_DIR:"

    # Iterate over each subdirectory in the base directory
    for DIR in "$BASE_DIR"*/; do
        echo "Entering directory: $DIR"

        # Iterate over each CSV file in the current directory
        for FILE in "$DIR"*.csv; do
            if [[ ! -e "$FILE" ]]; then
                echo "No CSV files found in $DIR"
                continue
            fi

            echo "Processing file: $FILE"

            number_of_lines=$(wc -l < "$FILE")

            # Extract the identifier (M1, I1, etc.) from the filename
            identifier=$(basename "$FILE" | grep -o -E '[IM][0-9]{1,2}' | head -n 1)
            task_identifier="${identifier:1}"
            new_fields=$(get_task_from_identifier "$task_identifier")

            # Check if the filename contains 'I' or 'M' and append it after the task identifier field
            if [[ "$FILE" =~ "I" ]]; then
                new_fields="$new_fields,I"
            elif [[ "$FILE" =~ "M" ]]; then
                new_fields="$new_fields,M"
            fi

            # Extract the repetition number after "R" from the filename
            r_number=$(basename "$FILE" | grep -o -E 'R([0-9]{1,2})' | sed 's/R//')
            new_fields="$new_fields,$r_number"

            # Extract the subject number from the file name
            s_number=$(basename "$FILE" | grep -o -E 'S([0-9]{1,2})' | sed 's/S//')
            new_fields="$new_fields,$s_number"

            for ((line=2; line<=$number_of_lines; line++))
            do
                current_line=$(sed -n "${line}p" "$FILE" | tr -d '\r' | sed 's/[[:space:]]*$//')
                current_line="${current_line},$(date +%s%3N)"
                current_line=$(echo "$current_line" | sed "s/\([^,]*\),/\1,$new_fields,/")
                echo $current_line
                echo $current_line | kafka-console-producer --topic brain_data --bootstrap-server 192.168.1.76:9092
                sleep 0.1
            done
        done
    done
fi
