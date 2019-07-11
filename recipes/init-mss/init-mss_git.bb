inherit autotools-brokensep update-rc.d systemd

DESCRIPTION = "Modem init"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/BSD;md5=3775480a712fc46a69647678acb234cb"
PR = "r7"

FILESPATH =+ "${WORKSPACE}/mdm-ss-mgr:"
FILESEXTRAPATHS_prepend := "${THISDIR}/init_mss:"

SRC_URI = "file://init_mss"
SRC_URI += "file://init_sys_mss.service"

S = "${WORKDIR}/init_mss"

# Hold /dev/subsys_modem forever on all SOCs which don't have Modem wakeup support.
EXTRA_OECONF_append_msm = " --enable-indefinite-sleep"
EXTRA_OECONF_append_sdxpoorwills = " --enable-indefinite-sleep"
EXTRA_OECONF_append_sdxprairie = " --enable-indefinite-sleep"

EXTRA_OECONF_append = " --enable-modem"

# QCS40x has wcnss but not modem
EXTRA_OECONF_remove_qcs40x = "--enable-modem"
EXTRA_OECONF_append_qcs40x = " --enable-wcnss"

FILES_${PN} += "${systemd_unitdir}/system/"

INITSCRIPT_NAME = "init_sys_mss"
INITSCRIPT_PARAMS = "start 38 2 3 4 5 ."
INITSCRIPT_PARAMS_sdxpoorwills = "start 31 S ."
INITSCRIPT_PARAMS_sdxprairie = "start 31 S ."

do_install() {
    install -m 0755 ${S}/init_mss -D ${D}/sbin/init_mss
    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -d ${D}${systemd_unitdir}/system/
        install -m 0644 ${WORKDIR}/init_sys_mss.service -D ${D}${systemd_unitdir}/system/init_sys_mss.service

        # for non AB targets with NAND flash, set dependency on firmare-ubi.service.
        if ${@bb.utils.contains('DISTRO_FEATURES','ab-boot-support','false','true',d)}; then
            if ${@bb.utils.contains('DISTRO_FEATURES','nand-boot','true','false',d)}; then

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
                if ${@bb.utils.contains('DISTRO_NAME', 'mdm', 'true', 'false', d)}; then
                    sed -i '/RemainAfterExit=yes/a ExecStartPost=+sleep 8' ${D}${systemd_unitdir}/system/init_sys_mss.service
                fi

                install -d ${D}${systemd_unitdir}/system/sockets.target.wants/
                ln -sf ${systemd_unitdir}/system/init_sys_mss.service ${D}/${systemd_unitdir}/system/sockets.target.wants/init_sys_mss.service
           else
                install -d ${D}${systemd_unitdir}/system/sysinit.target.wants/
                ln -sf ${systemd_unitdir}/system/init_sys_mss.service ${D}/${systemd_unitdir}/system/sysinit.target.wants/init_sys_mss.service
           fi
        else
            install -d ${D}${systemd_unitdir}/system/sysinit.target.wants
            ln -sf ${systemd_unitdir}/system/init_sys_mss.service ${D}/${systemd_unitdir}/system/sysinit.target.wants/init_sys_mss.service
        fi
    else
        install -m 0755 ${S}/start_mss -D ${D}${sysconfdir}/init.d/init_sys_mss
    fi
}
