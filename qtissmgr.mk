include $(INCLUDE_DIR)/package.mk

QTISSMGR:=initmss

ifneq ($(USER_VARIANT),1)
	QTISSMGR += reboot-daemon
endif
