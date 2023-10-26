inherit autotools-brokensep systemd

DESCRIPTION = "csm logger"

LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=3775480a712fc46a69647678acb234cb"

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
