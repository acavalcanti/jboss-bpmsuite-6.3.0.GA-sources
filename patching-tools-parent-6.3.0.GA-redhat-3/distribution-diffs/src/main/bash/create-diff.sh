#!/bin/bash
# Fail immediately if one of the commands in the script fails
set -e
# Input:
#  - $1: output-dir
#  - $2: version1-dir (the latest GA version)
#  - $3: version2-dir (the latest patch)
#  - $4: distribution name (e.g. eap6.x, brms-engine)
#  - $5: version of the patch (e.g. 6.1.1 or 6.1.2)
#  - $6: directory where to place the generated updates-list.txt (this will be input for future patches)
#
# Output:
#  - <output-dir>
#      -- remove-list.txt
#      -- new-content/ # directory with updates relative paths to the version1-dir/version2-dir root

# Algorithm:
#  1) Create list of all nested files inside both version1-dir and version2-dir
#  2) Compute the base remove-list.txt. Contains files that are in version1, but no in version2 (those need to be removed)
#  4) Create the diff (the files added/updated in version2 in comparison to version1)
#      - go over file-list-dir2
#       - if the same file does not exist in the dir1, add it to the result (+ update remove-list and updates-list)
#       - if the same file exists in dir1, create md5sum and compare
#           - if the checksum is the same no need to include the file, otherwise include it in the result (+ update remove-list and updates-list)

# example params ./create-diff.sh <output-dir> <distro1> <distro2> brms-eap6.x 6.1.1 <updates-lists-dir> <prev-updates-lists-dir> <list-of-paths-to-copy-as-wholes>
OUTPUT_DIR=$1
VERSION1_DIR=$2
VERSION2_DIR=$3
DISTRO_NAME=$4
VERSION=$5 # e.g. 6.1.1 or 6.1.2
UPDATES_LISTS_DIR=$6
WHOLE_DIRS_UPDATE=$7 # comma-separated list

# TODO handle properly input params
# just a trivial check to see if at least distribution dirs were specified
if [ x"${VERSION1_DIR}" == "x" ]; then
    echo "Distribution dir needs to be specified as a first parameter, no param specified!"
    exit 1
fi

if [ x"${VERSION2_DIR}" == "x" ]; then
    echo "Distribution dir needs to be specified as a second parameter, no param specified!"
    exit 1
fi

TMP_DIR=target/create-diff-tmp
rm -rf ${TMP_DIR}
mkdir --parents ${TMP_DIR}

rm -rf ${OUTPUT_DIR}
mkdir --parents ${OUTPUT_DIR}

echo "Output dir: ${OUTPUT_DIR}"
echo "Version1 (older) dir: ${VERSION1_DIR}"
echo "Version2 (newer) dir: ${VERSION2_DIR}"

# make sure there is at least empty file, if no differences found
REMOVE_LIST_CURRENT="${OUTPUT_DIR}/remove-list-${VERSION}.txt"
REMOVE_LIST_ALL="${OUTPUT_DIR}/remove-list.txt"
CHECKSUM_FILE="${OUTPUT_DIR}/checksums.txt"

UPDATES_LIST="${OUTPUT_DIR}/updates-list.txt"
rm -f "${UPDATES_LIST}"
touch "${UPDATES_LIST}"

mkdir "${OUTPUT_DIR}/new-content"

# for each specified path: remove the path in old location, copy the path from new location to new-content dir
# and update remove-list and updates-list
for RELATIVE_PATH in $(echo ${WHOLE_DIRS_UPDATE} | tr "," "\n")
do
    echo "Including the entire directory ${RELATIVE_PATH} (instead of creating diff)"
    mkdir -p "${OUTPUT_DIR}/new-content/${RELATIVE_PATH}"
    mv "${VERSION2_DIR}/${RELATIVE_PATH}/"* "${OUTPUT_DIR}/new-content/${RELATIVE_PATH}"
    rm -rf "${VERSION2_DIR}/${RELATIVE_PATH}"
    rm -rf "${VERSION1_DIR}/${RELATIVE_PATH}"
    echo "${RELATIVE_PATH}" >> "${UPDATES_LIST}"
    echo "${RELATIVE_PATH}" >> "${REMOVE_LIST_CURRENT}"
done

find "${VERSION1_DIR}" -type f -printf '%P\n' | sort > "${TMP_DIR}/version1-file-list.txt"
find "${VERSION2_DIR}" -type f -printf '%P\n' | sort > "${TMP_DIR}/version2-file-list.txt"

# files only in version1 (older) needs to be added to the remove list
grep -Fxv -f "${TMP_DIR}/version2-file-list.txt" "${TMP_DIR}/version1-file-list.txt" >> "${REMOVE_LIST_CURRENT}"

# create the actual patch dir contents (things new/updated in version2)
IFS=$'\n'       # make newlines the only separator
for FILE in `cat "${TMP_DIR}/version2-file-list.txt"`; do
    # if the file is not in version1, include it directly and go to next file
    if [ ! -f "$VERSION1_DIR/$FILE" ]; then
        # we need to preserve just the right level of parent dirs, so the "cd" is bit of hack
        OLD_PWD=`pwd`
        cd "${VERSION2_DIR}"
        cp --parents "${FILE}" "${OUTPUT_DIR}/new-content"
        cd "${OLD_PWD}"
        # this file should _not_ go into remove-list as it is just in new distro (nothing to remove from the old one)
        echo "${FILE}" >> "${UPDATES_LIST}"
        continue
    fi
    # now the file is both in version1 and version2. Detect if it changed and if so, include it in the patch dir
    MD5SUM1=`md5sum "${VERSION1_DIR}/${FILE}" | awk '{print $1}'`
    MD5SUM2=`md5sum "${VERSION2_DIR}/${FILE}" | awk '{print $1}'`
    #echo "${FILE}, ${MD5SUM1}" >> ${CHECKSUM_FILE}
    #echo $MD5SUM1
    #echo $MD5SUM2
    if [ ! ${MD5SUM1} == ${MD5SUM2} ]; then
        OLD_PWD=`pwd`
        cd "${VERSION2_DIR}"
        cp --parents "${FILE}" "${OUTPUT_DIR}/new-content"
        cd "${OLD_PWD}"
        # add this file also into remove-list.txt so that we know we should back it up before applying the patch
        # or should we just copy the whole directory before applying the patch?
        echo "${FILE}" >> "${UPDATES_LIST}"
        echo "${FILE}" >> "${REMOVE_LIST_CURRENT}"
        continue
    fi
    #echo "Excluding $FILE from the patch as the file did not change"
done

# compute and store checksums
for FILE in `cat "${TMP_DIR}/version1-file-list.txt"`; do
    MD5SUM=`md5sum "${VERSION1_DIR}/${FILE}" | awk '{print $1}'`
    echo "${FILE}=${MD5SUM}" >> ${CHECKSUM_FILE}
done

# sort the remove-list so that it is easily comparable with the updates-list
cat "${REMOVE_LIST_CURRENT}" | sort > "${TMP_DIR}/sorted-remove-list.txt" && cp -r "${TMP_DIR}/sorted-remove-list.txt" "${REMOVE_LIST_CURRENT}"
cat "${UPDATES_LIST}" | sort > "${TMP_DIR}/sorted-updates-list.txt" && cp -r "${TMP_DIR}/sorted-updates-list.txt" "${UPDATES_LIST}"
cp "${UPDATES_LIST}" "${UPDATES_LISTS_DIR}/${DISTRO_NAME}-${VERSION}-updates-list.txt"

# replace the "-redhat-<number>" suffix with wildcard (*) to make sure we remove all possible versions introduced by one-offs
# there are no previous update-lists to include (this is the first patch being generated)
cat "${REMOVE_LIST_CURRENT}" | sed 's/-redhat-.*.jar/*.jar/g' | sort | uniq > ${REMOVE_LIST_ALL}
