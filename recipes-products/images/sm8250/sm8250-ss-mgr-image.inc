# SS-MGR Open source Packages
RDEPENDS += "init-mss"
RDEPENDS += "reboot-daemon"

copy_edge_ss_mgr_rpm () {
   cp ${DEPLOY_DIR_RPM}/aarch64/init-mss-git-r7.aarch64.rpm ${IMAGE_ROOTFS}/packages/qcaudio/
}

ROOTFS_POSTPROCESS_COMMAND += "copy_edge_ss_mgr_rpm;"
