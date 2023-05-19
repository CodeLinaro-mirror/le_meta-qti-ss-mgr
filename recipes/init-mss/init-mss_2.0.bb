inherit autotools-brokensep update-rc.d systemd

DESCRIPTION = "Modem init"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9 \
"

FILESEXTRAPATHS:prepend := "${WORKSPACE}/mdm-ss-mgr:"
FILESEXTRAPATHS:prepend := "${THISDIR}/init_mss:"

SRC_URI = "file://init_mss"
SRC_URI += "file://init_sys_mss.service"
SRC_URI += "file://init_mss.rules"
SRC_URI += "file://init_mss.conf"
SRC_URI += "file://init_rproc_mss.service"

S = "${WORKDIR}/init_mss"

# Hold /dev/subsys_modem forever on all SOCs which don't have Modem wakeup support.
EXTRA_OECONF:append:msm = " --enable-indefinite-sleep"
EXTRA_OECONF:append:sdxpoorwills = " --enable-indefinite-sleep"
EXTRA_OECONF:append:sdxprairie = " --enable-indefinite-sleep"
EXTRA_OECONF:append:sdxnightjar = " --enable-indefinite-sleep"
EXTRA_OECONF:append:qti-distro-base = " --enable-indefinite-sleep"

EXTRA_OECONF:append:qcs40x = " --enable-indefinite-sleep=yes"
EXTRA_OECONF:append:qcs40x = " --enable-wcnss=yes"

EXTRA_OECONF:append = " --enable-modem"

EXTRA_OECONF:remove:kalama = " --enable-modem"
EXTRA_OECONF:remove:kalama = " --enable-wcnss"

# QCS40x has wcnss but not modem
EXTRA_OECONF:remove:qcs40x = "--enable-modem"

FILES:${PN} += "${systemd_unitdir}/system/"
FILES:${PN} += "${sysconfdir}/udev/rules.d/"

INITSCRIPT_NAME = "init_sys_mss"
INITSCRIPT_PARAMS = "start 38 2 3 4 5 ."
INITSCRIPT_PARAMS:sdxpoorwills = "start 31 S ."
INITSCRIPT_PARAMS:sdxprairie = "start 31 S ."

do_install() {
    install -m 0755 ${S}/init_mss -D ${D}/sbin/init_mss
    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -d ${D}${systemd_unitdir}/system/
        install -d ${D}${sysconfdir}/udev/rules.d/
        install -m 0644 ${WORKDIR}/init_sys_mss.service -D ${D}${systemd_unitdir}/system/init_sys_mss.service
        install -m 0644 ${WORKDIR}/init_mss.rules -D ${D}${sysconfdir}/udev/rules.d/init_mss.rules
        install -m 0644 ${WORKDIR}/init_mss.conf -D ${D}${sysconfdir}/tmpfiles.d/init_mss.conf
        if ${@bb.utils.contains_any('DISTRO_NAME', 'mdm auto', 'true', 'false', d)}; then
           #ADD NAND CHECK IF REQUIRED.
           # Clear the values of After, Requires and WantedBy.
           sed -i '/After/s/firmware.mount//' ${D}${systemd_unitdir}/system/init_sys_mss.service
           sed -i '/Requires/s/firmware.mount//' ${D}${systemd_unitdir}/system/init_sys_mss.service
           sed -i '/WantedBy/s/sysinit.target//' ${D}${systemd_unitdir}/system/init_sys_mss.service

           # Add new values to After, Requires and WantedBy.
           sed -i '/\<After\>/s/$/firmware-mount.service QCMAP_ConnectionManagerd.service/' ${D}${systemd_unitdir}/system/init_sys_mss.service
           sed -i '/Requires/s/$/firmware-mount.service/' ${D}${systemd_unitdir}/system/init_sys_mss.service
           sed -i '/WantedBy/s/$/sockets.target/' ${D}${systemd_unitdir}/system/init_sys_mss.service

           # Add new lines after "Requires=firmware-mount.service" to set DefaultDependencies to no.
           sed -i '/Requires=firmware-mount.service/a DefaultDependencies=no' ${D}${systemd_unitdir}/system/init_sys_mss.service
           sed -i '/Requires=firmware-mount.service/a Before=sockets.target' ${D}${systemd_unitdir}/system/init_sys_mss.service


            # Add sleep for mdm targets to ensure full CPU is available to load modem.
            if ${@bb.utils.contains_any('DISTRO_NAME', 'auto', 'true', 'false', d)}; then
                sed -i '/RemainAfterExit=yes/a ExecStartPost=+sleep 7' ${D}${systemd_unitdir}/system/init_sys_mss.service
            else
                sed -i '/RemainAfterExit=yes/a ExecStartPost=+sleep 12' ${D}${systemd_unitdir}/system/init_sys_mss.service
            fi
            install -d ${D}${systemd_unitdir}/system/sockets.target.wants/
            ln -sf ${systemd_unitdir}/system/init_sys_mss.service ${D}/${systemd_unitdir}/system/sockets.target.wants/init_sys_mss.service
        else
            install -d ${D}${systemd_unitdir}/system/sysinit.target.wants
            ln -sf ${systemd_unitdir}/system/init_sys_mss.service ${D}/${systemd_unitdir}/system/sysinit.target.wants/init_sys_mss.service
        fi
    else
        install -m 0755 ${S}/start_mss -D ${D}${sysconfdir}/init.d/init_sys_mss
    fi
}

do_install:append:kalama() {
	if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
		install -m 0644 ${WORKDIR}/init_rproc_mss.service -D ${D}${systemd_unitdir}/system/init_sys_mss.service
	fi
}

SYSTEMD_SERVICE:${PN}:kalama = "init_sys_mss.service"
