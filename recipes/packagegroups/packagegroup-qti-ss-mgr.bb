SUMMARY = "Group to bring packages for sub-system management"

LICENSE = "BSD-3-Clause"

inherit packagegroup

PROVIDES = "${PACKAGES}"

PACKAGES = ' \
    packagegroup-qti-ss-mgr \
'

# Don't install reboot-daemon for user builds.
RBDAEMON = "reboot-daemon"
RBDAEMON_qti-distro-user = ""

# Install init-mss-rproc for selected machines
INIT_MSS_RPROC ?= 'False'
INIT_MSS_RPROC:sa525m = 'True'
INIT_MSS_RPROC:mdm9607 = 'True'
# Daemons needed for subsystem management
RDEPENDS:${PN} = "\
    ${@bb.utils.contains("MACHINE_FEATURES", "qti-remoteproc", "", oe.utils.conditional('INIT_MSS_RPROC', 'True', 'init-mss-rproc', 'init-mss', d), d)} \
    ${@bb.utils.contains("MACHINE_FEATURES", "qti-csm", "csm-logger rsync", "", d)} \
    ${RBDAEMON} \
"
