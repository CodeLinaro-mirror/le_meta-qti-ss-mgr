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

# Daemons needed for subsystem management
RDEPENDS_${PN} = "\
    ${@bb.utils.contains("MACHINE_FEATURES", "qti-remoteproc", "", "init_mss", d)} \
    ${RBDAEMON} \
"
