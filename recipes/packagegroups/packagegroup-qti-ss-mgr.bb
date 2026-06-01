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
RDEPENDS:${PN} = "\
    init-mss \
    ${RBDAEMON} \
"
