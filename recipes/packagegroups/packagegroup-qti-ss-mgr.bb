SUMMARY = "Group to bring packages for sub-system management"

LICENSE = "BSD-3-Clause"

inherit packagegroup

PROVIDES = "${PACKAGES}"

PACKAGES = ' \
    packagegroup-qti-ss-mgr \
'

# Decide if early init of subsystems is needed.
EARLY_SS_INIT ?= "init-mss"
EARLY_SS_INIT_waipio ?= ""


# Don't install reboot-daemon for user builds.
RBDAEMON = "reboot-daemon"
RBDAEMON_qti-distro-user = ""

# Daemons needed for subsystem management
RDEPENDS_${PN} = "\
    ${EARLY_SS_INIT} \
    ${RBDAEMON} \
"
