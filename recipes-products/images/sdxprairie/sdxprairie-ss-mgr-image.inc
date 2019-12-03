# SS-MGR Open source Packages
IMAGE_INSTALL += "init-mss"
IMAGE_INSTALL += "${@bb.utils.contains_any('VARIANT', 'user', '', 'reboot-daemon', d)}"
