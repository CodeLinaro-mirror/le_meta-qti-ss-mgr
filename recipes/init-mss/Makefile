include $(TOPDIR)/rules.mk

# Name, version and release number
# The name and version of your package are used to define the variable to point to the build directory of your package: $(PKG_BUILD_DIR)
PKG_NAME:=initmss
PKG_VERSION:=1.0
PKG_RELEASE:=1
PKG_FIXUP:=autoreconf

# Source settings (i.e. where to find the source codes)
# This is a custom variable, used below
SOURCE_DIR:=$(TOPDIR)/src/mdm-ss-mgr/init_mss

include $(INCLUDE_DIR)/package.mk

CONFIGURE_ARGS +=  --enable-modem --enable-indefinite-sleep

# Package definition; instructs on how and where our package will appear in the overall configuration menu ('make menuconfig')
define Package/initmss
	SECTION:=QTI
	CATEGORY:=QTI-PACKAGES
	TITLE:=initmss
endef

# Package description; a more verbose description on what our package does
define Package/initmss/description
	initmss
endef

# Package preparation instructions; create the build directory and copy the source code.
# The last command is necessary to ensure our preparation instructions remain compatible with the patching system.
define Build/Prepare
	mkdir -p $(PKG_BUILD_DIR)
	cp -rp $(SOURCE_DIR)/* $(PKG_BUILD_DIR)
	$(Build/Patch)
endef

define Package/initmss/install
	$(INSTALL_DIR) $(1)/usr/bin
	$(INSTALL_DIR) $(1)/firmware
	$(CP) $(PKG_BUILD_DIR)/init_mss $(1)/usr/bin/
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_BIN) ./init-mss/init-mss.init $(1)/etc/init.d/
endef

# This command is always the last, it uses the definitions and variables we give above in order to get the job done

$(eval $(call BuildPackage,initmss))
