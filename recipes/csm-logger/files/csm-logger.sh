#!/bin/bash
#Copyright (c) 2023 Qualcomm Innovation Center, Inc. All rights reserved.
#SPDX-License-Identifier: BSD-3-Clause-Clear
journalctl --quiet --since='5 minutes ago' > /data/logs/journal.log
