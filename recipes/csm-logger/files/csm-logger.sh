#!/bin/bash
#Copyright (c) 2023 Qualcomm Innovation Center, Inc. All rights reserved.
#SPDX-License-Identifier: BSD-3-Clause-Clear

FLAG_FILE="/var/volatile/csm-logger-flag"

if [ ! -f "$FLAG_FILE" ]; then
    echo "csm-logger triggered first time after bootup" > "$FLAG_FILE"
    journalctl --quiet > /data/logs/journal.log
else
    journalctl --quiet --since='5 minutes ago' > /data/logs/journal.log
fi
sync
