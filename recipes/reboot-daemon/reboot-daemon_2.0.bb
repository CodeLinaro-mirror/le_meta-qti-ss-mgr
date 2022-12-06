inherit autotools-brokensep

DESCRIPTION = "Rebooter daemon"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9 \
"

FILESEXTRAPATHS:prepend := "${WORKSPACE}/mdm-ss-mgr:"

SRC_URI = "file://reboot-daemon"
SRC_URI += "file://reboot-daemon.service"

S = "${WORKDIR}/reboot-daemon"

EXTRA_OEMAKE:append = " CROSS=${HOST_PREFIX}"
FILES:${PN} += "${systemd_unitdir}/system/"

do_install() {
    install -m 0755 ${S}/reboot-daemon -D ${D}/sbin/reboot-daemon
    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
      install -d ${D}${systemd_unitdir}/system/
      install -m 0644 ${WORKDIR}/reboot-daemon.service -D ${D}${systemd_unitdir}/system/reboot-daemon.service
      install -d ${D}${systemd_unitdir}/system/multi-user.target.wants/
      install -d ${D}${systemd_unitdir}/system/ffbm.target.wants/
      # enable the service for multi-user.target
      ln -sf ${systemd_unitdir}/system/reboot-daemon.service \
           ${D}${systemd_unitdir}/system/multi-user.target.wants/reboot-daemon.service
      ln -sf ${systemd_unitdir}/system/reboot-daemon.service \
           ${D}${systemd_unitdir}/system/ffbm.target.wants/reboot-daemon.service
   fi
}
