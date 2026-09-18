inherit autotools-brokensep

DESCRIPTION = "Rebooter daemon"
# BSD-3-Clause-Clear is not present in all Poky trees; probe at parse time
# and fall back to BSD where the file is absent, keeping all targets working.
LICENSE = "${@'BSD-3-Clause-Clear' if os.path.exists(d.getVar('COREBASE') + '/meta/files/common-licenses/BSD-3-Clause-Clear') else 'BSD'}"
LIC_FILES_CHKSUM = "${@'file://${COREBASE}/meta/files/common-licenses/BSD-3-Clause-Clear;md5=7a434440b651f4a472ca93716d01033a' if os.path.exists(d.getVar('COREBASE') + '/meta/files/common-licenses/BSD-3-Clause-Clear') else 'file://${COREBASE}/meta/files/common-licenses/BSD;md5=3775480a712fc46a69647678acb234cb'}"
PR = "r4"

FILESPATH =+ "${WORKSPACE}/mdm-ss-mgr:"

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
