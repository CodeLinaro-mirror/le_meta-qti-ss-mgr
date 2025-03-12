#!/bin/sh
# Copyright (c) 2023-2025 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: BSD-3-Clause-Clear

STATE_OFFLINE="offline"

IsGPIOEnabled () {
    gpio_enable_status=`cat /proc/cmdline | awk -F'recoveryinfo_gpio=' '{print $2}' | awk '{print $1}' | tr -d '"'`
    return ${gpio_enable_status}
}

SlotSwitchReboot () {
    local abctl_cmd="/usr/bin/nad-abctl"
    # Set image_set_status fields in recoveryinfo struct
    #  'A &B' Usable     :  SET_AB_USABLE(0)
    #  'A' corrupted     :  DONT_USE_SET_A(1)
    #  'B' corrupted     :  DONT_USE_SET_B(2)
    #  'A &B' corrupted  :  DONT_USE_SET_AB(3)

    # Set owner fields in recoveryinfo struct
    #  OWNER_XBL         :  1
    #  OWNER_HLOS        :  2
    local owner_hlos=2
    local dont_use_set_a=1
    local dont_use_set_b=2
    local dont_use_set_ab=3
    local current_image_set_status=0

    if [ ! -e ${abctl_cmd} ]; then
        echo "${abctl_cmd} not found, reboot to edl " > /dev/kmsg
        /bin/sh -c 'reboot edl'
        exit 0
    fi

    mtd_device=`cat /proc/mtd | grep recoveryinfo | awk -F ':' '{print $1}'`
    if [ -z "${mtd_device}" ]; then
        echo " recoveryinfo part not found, reboot to edl " > /dev/kmsg
        /bin/sh -c 'reboot edl'
        exit 0
    fi

    chmod 666 /dev/${mtd_device}
    if [ "x${SLOT_SUFFIX}" == "x" ]; then
        echo "SLOT_SUFFIX not present or invalid, reboot to edl" > /dev/kmsg
        /bin/sh -c 'reboot edl'
        exit 0
    fi

    #Get current image set status
    (${abctl_cmd} --get_image_set_status)
    current_image_set_status=$?

    if [ "$current_image_set_status" -eq "-1" ]; then
        echo "Error: incorrect image set status" > /dev/kmsg
        /bin/sh -c 'reboot edl'
        exit 0
    fi

    if [ "$SLOT_SUFFIX" = "_a" ] && [ "$current_image_set_status" != "$dont_use_set_b" ]; then
        echo "Modem load failed for slot A" > /dev/kmsg
        ${abctl_cmd} --set_image_set_status ${dont_use_set_a}
        if [ "$?" -eq "-1" ]; then
            echo "Error: image set status failed" > /dev/kmsg
            /bin/sh -c 'reboot edl'
            exit 0
        fi
    elif [ "$SLOT_SUFFIX" = "_b" ] && [ "$current_image_set_status" != "$dont_use_set_a" ]; then
          echo "Modem load failed for slot B" > /dev/kmsg
          ${abctl_cmd} --set_image_set_status ${dont_use_set_b}
          if [ "$?" -eq "-1" ]; then
              echo "Error: image set status failed" > /dev/kmsg
              /bin/sh -c 'reboot edl'
              exit 0
          fi
    else
          echo "Modem load failed for slot A and B" > /dev/kmsg
          ${abctl_cmd} --set_image_set_status ${dont_use_set_ab}
          if [ "$?" -eq "-1" ]; then
              echo "Error: image set status failed" > /dev/kmsg
              /bin/sh -c 'reboot edl'
              exit 0
          fi
    fi

    ${abctl_cmd} --set_owner ${owner_hlos}
    if [ "$?" -eq "-1" ]; then
        echo "Error: set owner failed" > /dev/kmsg
        /bin/sh -c 'reboot edl'
        exit 0
    fi

    echo "Reboot for switching slots or EDL mode" > /dev/kmsg
    echo "warm" > /sys/kernel/reboot/mode
    /bin/sh -c 'reboot'
}


/bin/sh -c 'echo start > /sys/class/remoteproc/remoteproc0/state'

modstate=`cat /sys/class/remoteproc/remoteproc0/state`

if [ "$modstate" == "$STATE_OFFLINE" ]; then
    IsGPIOEnabled
    if [ "$?" -eq "1" ]; then
        #GPIO Enabled moving device to EDL.
        echo "GPIO Enabled boot to EDL" > /dev/kmsg
        /bin/sh -c 'reboot edl'
    else
        echo "Start slot switch for Modem failure" > /dev/kmsg
        SlotSwitchReboot
    fi
fi

exit 0
