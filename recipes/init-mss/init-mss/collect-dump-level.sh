#!/bin/sh
#******************************************************************************
# Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause-Clear
#******************************************************************************/

# sys node to read the dump_level
NODE="/sys/kernel/qcom_rproc/dump_level"

# Persistent path to save dump_level info
OUTFILE="/etc/dump_level"

# Check if node exists
if [ -f "$NODE" ]; then
    # Read the node value
    VALUE=$(cat "$NODE")
    # Write the value to the output file
    echo "$VALUE" > "$OUTFILE"
    echo "Dump level value ($VALUE) saved to $OUTFILE" >> /dev/kmsg
else
    echo "Node $NODE does not exist" >> /dev/kmsg
fi

