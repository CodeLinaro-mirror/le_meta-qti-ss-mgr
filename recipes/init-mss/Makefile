include $(TOPDIR)/rules.mk

# Name, version and release number
# The name and version of your package are used to define the variable to point to the build directory of your package: $(PKG_BUILD_DIR)
PKG_NAME:=initmss
PKG_VERSION:=1.0
PKG_RELEASE:=1
PKG_FIXUP:=autoreconf
PKG_LICENSE:=BSD
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
	$(INSTALL_DIR) $(1)/firmware
	$(INSTALL_DIR) $(1)/etc/init.d
ifeq ($(BOARD), sdx35)
	$(INSTALL_BIN) ./init-mss/sdx35-init-mss.init $(1)/etc/init.d/init-mss.init
else ifeq ($(BOARD), sdx75)
	$(INSTALL_BIN) ./init-mss/sdx75-init-mss.init $(1)/etc/init.d/init-mss.init
else ifeq ($(BOARD), sdx85)
	$(INSTALL_BIN) ./init-mss/sdx85-init-mss.init $(1)/etc/init.d/init-mss.init
else ifeq ($(BOARD), sdxecho)
	$(INSTALL_BIN) ./init-mss/sdxecho-init-mss.init $(1)/etc/init.d/init-mss.init

endif
	$(INSTALL_DIR) $(1)/etc/hotplug.d/subsys
	$(INSTALL_DATA) ./init-mss/15-init-mss $(1)/etc/hotplug.d/subsys/
endef

# This command is always the last, it uses the definitions and variables we give above in order to get the job done

$(eval $(call BuildPackage,initmss))
