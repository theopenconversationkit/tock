#!/bin/bash

#
# Copyright (C) 2017/2025 SNCF Connect & Tech
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

mongodb1=${MONGO1:-mongo}
mongodb2=${MONGO2:-mongo2}
mongodb3=${MONGO3:-mongo3}

port1=${PORT1:-27017}
port2=${PORT2:-27018}
port3=${PORT3:-27019}

echo "Waiting for startup.."
until mongo --host ${mongodb1}:${port1} --eval 'quit(db.runCommand({ ping: 1 }).ok ? 0 : 2)' &>/dev/null; do
  printf '.'
  sleep 1
done
until mongo --host ${mongodb2}:${port2} --eval 'quit(db.runCommand({ ping: 1 }).ok ? 0 : 2)' &>/dev/null; do
  printf '.'
  sleep 1
done
until mongo --host ${mongodb3}:${port3} --eval 'quit(db.runCommand({ ping: 1 }).ok ? 0 : 2)' &>/dev/null; do
  printf '.'
  sleep 1
done

echo "Started.."

echo setup.sh time now: `date +"%T" `
mongo --host ${mongodb1}:${port1} <<EOF
   var cfg = {
        "_id": "${RS}",
        "members": [
            {
                "_id": 0,
                "host": "${mongodb1}:${port1}"
            },
            {
                "_id": 1,
                "host": "${mongodb2}:${port2}"
            },
            {
                "_id": 2,
                "host": "${mongodb3}:${port3}"
            }
        ]
    };
    rs.initiate(cfg, { force: true });
    rs.reconfig(cfg, { force: true });
EOF
