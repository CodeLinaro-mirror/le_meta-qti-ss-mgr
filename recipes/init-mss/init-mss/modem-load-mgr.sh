#!/bin/sh
# Copyright (c) 2023-2025 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: BSD-3-Clause-Clear

UBI_SYS_CLASS="/sys/class/ubi/ubi0"
STATE_OFFLINE="offline"

GetFirmwareVolumeID () {
    local firmware="firmware"

    if [ "x${SLOT_SUFFIX}" == "x" ]; then
       SLOT_SUFFIX="_a"
    fi

    firmware_ab_name=${firmware}${SLOT_SUFFIX}
    volcount=`cat ${UBI_SYS_CLASS}/volumes_count`
    vol_found=""
    for vid in `seq 0 $volcount`; do
        echo $vid  > /dev/kmsg
        name=`cat ${UBI_SYS_CLASS}_$vid/name`
        if [ "$name" == "$firmware" ] || [ "$name" == "$firmware_ab_name" ]; then
            echo "volume id found for $firmware_ab_name, volume id $vid "  > /dev/kmsg
            echo $vid
            vol_found=${vid}
            break
        fi
        echo $name  > /dev/kmsg
    done
}

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
    local firmware_a="firmware_a"
    local firmware_b="firmware_b"
    local current_image_set_status=0
    local rollback_count=0

    if [ ! -e ${abctl_cmd} ]; then
        echo "${abctl_cmd} not found, reboot to edl " > /dev/kmsg
        /bin/sh -c 'reboot edl'
        exit 0
    fi

    mtd_device=`cat /proc/mtd | grep recoveryinfo | awk -F ':' '{print $1}'`
    if [ -z "${mtd_device}" ]; then
        echo " recoveryinfo part not found. " > /dev/kmsg
        mtd_device=`cat /proc/mtd | grep misc | awk -F ':' '{print $1}'`
        if [ -z "${mtd_device}" ]; then
                echo " misc part not found, reboot to edl"> /dev/kmsg
                /bin/sh -c 'reboot edl'
                exit 0
        fi
    fi

    chmod 666 /dev/${mtd_device}
    volid=$(GetFirmwareVolumeID)
    firmware_ab_name=$(cat /sys/class/ubi/ubi0_${volid}/name)
    if [ "$firmware_ab_name" == "$firmware_a" ] || [ "$firmware_ab_name" == "$firmware_b" ] ; then
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

        #Get TBI rollback count
        (${abctl_cmd} --get_rollback_failed_attempts)
        rollback_count=$?
        if [ "$rollback_count" -lt "0" ] || [ "$rollback_count" -gt "6" ]; then
            echo "Error: invalid rollback count" > /dev/kmsg
            /bin/sh -c 'reboot edl'
            exit 0
        fi

        if [ "$SLOT_SUFFIX" = "_a" ] && \
           [ "$current_image_set_status" != "$dont_use_set_b" ] && \
           [ "$rollback_count" -eq "0" ]; then
            echo "Modem load failed for slot A" > /dev/kmsg
            #check if image status is already set to optimize NAND write
            if [ "$current_image_set_status" -ne "$dont_use_set_a" ]; then
                ${abctl_cmd} --set_image_set_status ${dont_use_set_a}
                if [ "$?" -ne "0" ]; then
                    echo "Error: image set status failed" > /dev/kmsg
                    /bin/sh -c 'reboot edl'
                    exit 0
                fi
            fi
        elif [ "$SLOT_SUFFIX" = "_b" ] && \
             [ "$current_image_set_status" != "$dont_use_set_a" ] && \
             [ "$rollback_count" -eq "0" ]; then
              echo "Modem load failed for slot B" > /dev/kmsg
              #check if image status is already set to optimize NAND write
              if [ "$current_image_set_status" -ne "$dont_use_set_b" ]; then
                  ${abctl_cmd} --set_image_set_status ${dont_use_set_b}
                  if [ "$?" -ne "0" ]; then
                      echo "Error: image set status failed" > /dev/kmsg
                      /bin/sh -c 'reboot edl'
                      exit 0
                  fi
              fi
        else
          echo "Modem load failed for slot A and B" > /dev/kmsg
          #check if image status is already set to optimize NAND write
          if [ "$current_image_set_status" -ne "$dont_use_set_ab" ]; then
              ${abctl_cmd} --set_image_set_status ${dont_use_set_ab}
              if [ "$?" -ne "0" ]; then
                  echo "Error: image set status failed" > /dev/kmsg
                  /bin/sh -c 'reboot edl'
                  exit 0
              fi
          fi

            #Go to recovery only after retries are exhausted
            if [ "$rollback_count" -ge "5" ]; then
                mtd_device=`cat /proc/mtd | grep recoveryfs | awk -F ':' '{print $1}'`
                if [ ! -z "${mtd_device}" ]; then
                    echo " recoveryfs partition found, AB scenario, reboot to recovery." > /dev/kmsg
                    #Set owner before reboot
                    ${abctl_cmd} --set_owner ${owner_hlos}
                    if [ "$?" -ne "0" ]; then
                        echo "Error: set owner failed" > /dev/kmsg
                        #Not returning or going to EDL as we already going into recovery
                    fi
                    /bin/sh -c 'reboot recovery'
                    exit 0
                fi
            fi
        fi
        ${abctl_cmd} --set_owner ${owner_hlos}
        if [ "$?" -ne "0" ]; then
            echo "Error: set owner failed" > /dev/kmsg
            /bin/sh -c 'reboot edl'
            exit 0
        fi

        echo "Rebooting for switching slots or EDL mode" > /dev/kmsg
        /bin/sh -c 'reboot system-abnormal'
        exit 0
    else
        mtd_device=`cat /proc/mtd | grep recoveryfs | awk -F ':' '{print $1}'`
        if [ -z "${mtd_device}" ]; then
            echo "non a/b volumes , reboot to edl " > /dev/kmsg
            /bin/sh -c 'reboot edl'
            exit 0
        else
            echo " recoveryfs part found, reboot to recovery" > /dev/kmsg
            /bin/sh -c 'reboot recovery'
            exit 0
        fi
    fi
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
