inherit autotools-brokensep systemd

DESCRIPTION = "csm logger"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9 \
"

SRC_URI = "file://csm-logger.service"
SRC_URI += "file://csm-logger.sh"
SRC_URI += "file://csm-logger.timer"

S = "${WORKDIR}"

RDEPENDS:${PN} = "bash"

do_compile[noexec] = "1"
do_configure[noexec] = "1"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -d ${D}${sysconfdir}
    install -m 755 -d ${D}/data/logs
    install -m 0644 ${S}/csm-logger.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${S}/csm-logger.timer ${D}${systemd_system_unitdir}/
    install -m 0755 ${S}/csm-logger.sh ${D}${sysconfdir}/
}

SYSTEMD_SERVICE:${PN} += "csm-logger.timer"
FILES:${PN} = "${systemd_system_unitdir}/*"
FILES:${PN} += "${sysconfdir}/*"
FILES:${PN} += "/data/logs"
