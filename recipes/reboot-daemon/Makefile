include $(TOPDIR)/rules.mk

PKG_NAME:=reboot-daemon
PKG_VERSION:=1.0
PKG_RELEASE:=1
PKG_FIXUP:=autoreconf

LOCAL_SRC := $(TOPDIR)/src/mdm-ss-mgr/$(PKG_NAME)

include $(INCLUDE_DIR)/package.mk

define Package/reboot-daemon
  SECTION:=QTI
  CATEGORY:=QTI-PACKAGES
  TITLE:=Reboot Daemon
endef

define Package/reboot-daemon/Description
	Reboot Daemon package
endef

define Build/Prepare
	mkdir -p $(PKG_BUILD_DIR)/
	$(CP) $(LOCAL_SRC)/* $(PKG_BUILD_DIR)/
endef

define Package/reboot-daemon/install
	$(INSTALL_DIR) $(1)/sbin/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/reboot-daemon $(1)/sbin/
	$(INSTALL_DIR) $(1)/etc/init.d/
	$(INSTALL_BIN) ./files/reboot-daemon.init $(1)/etc/init.d/
endef

$(eval $(call BuildPackage,reboot-daemon))

