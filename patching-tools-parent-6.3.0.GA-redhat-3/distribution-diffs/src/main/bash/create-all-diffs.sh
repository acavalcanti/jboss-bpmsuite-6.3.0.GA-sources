#!/usr/bin/env bash
# Fail immediately if one of the commands in the script fails
set -e

# Creates all required distribution diffs using the specified create-diff.sh script

# $1: distro name
# $2: relative path to latest GA build dir (older build)
# $3: relative path to latest patch build dir (newer build)
# $4: comma separeted list of dirs that will be included as a whole (not creating diffs for them)
create_brms_diff() {
    echo ">>> Creating diff for BRMS $1"
    create_diff "brms" "${1}" "${2}" "${3}" "${4}"
}

# $1: distro name
# $2: relative path to latest GA build dir (older build)
# $3: relative path to latest patch build dir (newer build)
# $4: comma separeted list of dirs that will be included as a whole (not creating diffs for them)
create_bpmsuite_diff() {
    echo ">>> Creating diff for BPM Suite $1"
    create_diff "bpmsuite" "${1}" "${2}" "${3}" "${4}"
}

# $1: distro name
# $2: relative path to latest GA build dir (older build)
# $3: relative path to latest patch build dir (newer build)
# $4: comma separeted list of dirs that will be included as a whole (not creating diffs for them)
create_brms_bpmsuite_diff() {
    echo ">>> Creating diff for BRMS + BPM Suite $1"
    create_diff "brms-bpmsuite" "${1}" "${2}" "${3}" "${4}"
}

# $1: basedir
# $1: war name
unzip_war() {
    WAR_PATH="${1}/${2}"
    mv "${WAR_PATH}" "${WAR_PATH}_file"
    unzip -q "${WAR_PATH}_file" -d "${WAR_PATH}"
    rm -rf "${WAR_PATH}_file"
}

# $1: product name
# $2: distro name
# $3: relative path to latest GA build dir (older build)
# $4: relative path to latest patch build dir (newer build)
# $5: comma separated list of relative paths to dirs. These will be included as whole dirs, instead of creating diffs from them
create_diff() {
    rm -rf ${TMP_DIR}
    mkdir -p ${TMP_DIR}
    # copy the distributions as we will make changes there that can not be undone (rm, mv). Better to mess with the copy. This
    # also means that we don't need to unzip the distributions every time during maven build
    VERSION1_DIR="${TMP_DIR}/distribution-version1"
    mkdir ${VERSION1_DIR}
    cp -r "${BASEDIR}/$3/"* ${VERSION1_DIR}
    VERSION2_DIR="${TMP_DIR}/distribution-version2"
    mkdir ${VERSION2_DIR}
    cp -r "${BASEDIR}/$4/"* ${VERSION2_DIR}
    # unzip WARs for WAS8 distibution
    if [ x"$2" == x"was8" ]; then
        unzip_war "${VERSION1_DIR}" "business-central.war"
        unzip_war "${VERSION1_DIR}" "kie-server.war"
        unzip_war "${VERSION2_DIR}" "business-central.war"
        unzip_war "${VERSION2_DIR}" "kie-server.war"
        # dashbuilder is only in BPM Suite
        if [ x"$1" == x"bpmsuite" ]; then
            unzip_war "${VERSION1_DIR}" "dashbuilder.war"
            unzip_war "${VERSION2_DIR}" "dashbuilder.war"
        fi
    fi
    sh ${CREATE_DIFF_SCRIPT} "${BASEDIR}/diffs/${1}-${2}" "${VERSION1_DIR}" "${VERSION2_DIR}" "${1}-${2}" "${PATCH_VERSION}" "${CURRENT_UPDATES_LISTS_DIR}" "$5"
}

CREATE_DIFF_SCRIPT=$1
BASEDIR=$2
PATCH_VERSION=$3
GA_BUILD_VERSION=$4
PATCH_BUILD_VERSION=$5

TMP_DIR="${BASEDIR}/create-all-diffs-tmp"

CURRENT_UPDATES_LISTS_DIR=${BASEDIR}/updates-lists

rm -rf ${CURRENT_UPDATES_LISTS_DIR}
mkdir ${CURRENT_UPDATES_LISTS_DIR}


# BRMS diffs
create_brms_diff "eap6.x"\
 "brms-eap6.x-previous-release/jboss-eap-6.4"\
 "brms-eap6.x-latest-release/jboss-eap-6.4"\
 "standalone/deployments/business-central.war/org.kie.workbench.drools.KIEDroolsWebapp,standalone/deployments/business-central.war/WEB-INF/classes/org/jboss/errai/marshalling/server/impl"

create_brms_diff "generic"\
 "brms-generic-previous-release/jboss-brms-$GA_BUILD_VERSION-deployable-generic"\
 "brms-generic-latest-release/jboss-brms-6.3-deployable-generic"\
 "business-central.war/org.kie.workbench.drools.KIEDroolsWebapp,business-central.war/WEB-INF/classes/org/jboss/errai/marshalling/server/impl"

create_brms_diff "was8"\
 "brms-was8-previous-release/jboss-brms-$GA_BUILD_VERSION-deployable-was8"\
 "brms-was8-latest-release/jboss-brms-6.3-deployable-was8"\
 "business-central.war/org.kie.workbench.drools.KIEDroolsWebapp,business-central.war/WEB-INF/classes/org/jboss/errai/marshalling/server/impl"

create_brms_diff "wls12c"\
 "brms-wls12c-previous-release/jboss-brms-$GA_BUILD_VERSION-deployable-wls12c"\
 "brms-wls12c-latest-release/jboss-brms-6.3-deployable-wls12c"\
 "business-central.war/org.kie.workbench.drools.KIEDroolsWebapp,business-central.war/WEB-INF/classes/org/jboss/errai/marshalling/server/impl"

create_brms_diff "engine"\
 "brms-engine-previous-release/jboss-brms-$GA_BUILD_VERSION-engine"\
 "brms-engine-latest-release/jboss-brms-6.3-engine"

# BRMS + BPM Suite shared diffs
create_brms_bpmsuite_diff "planner-engine"\
 "brms-bpmsuite-planner-engine-previous-release/jboss-brms-bpmsuite-$GA_BUILD_VERSION-planner-engine"\
 "brms-bpmsuite-planner-engine-latest-release/jboss-brms-bpmsuite-6.3-planner-engine"

create_brms_bpmsuite_diff "supplementary-tools"\
 "brms-bpmsuite-supplementary-tools-previous-release/jboss-brms-bpmsuite-$GA_BUILD_VERSION-supplementary-tools"\
 "brms-bpmsuite-supplementary-tools-latest-release/jboss-brms-bpmsuite-6.3-supplementary-tools"

# BPM Suite diffs
create_bpmsuite_diff "eap6.x"\
 "bpmsuite-eap6.x-previous-release/jboss-eap-6.4"\
 "bpmsuite-eap6.x-latest-release/jboss-eap-6.4"\
  "standalone/deployments/business-central.war/org.kie.workbench.KIEWebapp,standalone/deployments/business-central.war/WEB-INF/classes/org/jboss/errai/marshalling/server/impl"

create_bpmsuite_diff "generic"\
 "bpmsuite-generic-previous-release/jboss-bpmsuite-$GA_BUILD_VERSION-deployable-generic"\
 "bpmsuite-generic-latest-release/jboss-bpmsuite-6.3-deployable-generic"\
 "business-central.war/org.kie.workbench.KIEWebapp,business-central.war/WEB-INF/classes/org/jboss/errai/marshalling/server/impl"

create_bpmsuite_diff "was8"\
 "bpmsuite-was8-previous-release/jboss-bpmsuite-$GA_BUILD_VERSION-deployable-was8"\
 "bpmsuite-was8-latest-release/jboss-bpmsuite-6.3-deployable-was8"\
 "business-central.war/org.kie.workbench.KIEWebapp,business-central.war/WEB-INF/classes/org/jboss/errai/marshalling/server/impl"

create_bpmsuite_diff "wls12c"\
 "bpmsuite-wls12c-previous-release/jboss-bpmsuite-$GA_BUILD_VERSION-deployable-wls12c"\
 "bpmsuite-wls12c-latest-release/jboss-bpmsuite-6.3-deployable-wls12c"\
 "business-central.war/org.kie.workbench.KIEWebapp,business-central.war/WEB-INF/classes/org/jboss/errai/marshalling/server/impl"

create_bpmsuite_diff "engine"\
 "bpmsuite-engine-previous-release/jboss-bpmsuite-$GA_BUILD_VERSION-engine"\
 "bpmsuite-engine-latest-release/jboss-bpmsuite-6.3-engine"
