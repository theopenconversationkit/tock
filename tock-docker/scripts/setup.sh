#!/bin/bash

mongodb1=${MONGO1:-mongo}
mongodb2=${MONGO2:-mongo2}
mongodb3=${MONGO3:-mongo3}

port1=${PORT1:-27017}
port2=${PORT2:-27018}
port3=${PORT3:-27019}

echo "Waiting for startup.."
until mongosh --host ${mongodb1}:${port1} --eval 'quit(db.runCommand({ ping: 1 }).ok ? 0 : 2)' &>/dev/null; do
  printf '.'
  sleep 1
done
until mongosh --host ${mongodb2}:${port2} --eval 'quit(db.runCommand({ ping: 1 }).ok ? 0 : 2)' &>/dev/null; do
  printf '.'
  sleep 1
done
until mongosh --host ${mongodb3}:${port3} --eval 'quit(db.runCommand({ ping: 1 }).ok ? 0 : 2)' &>/dev/null; do
  printf '.'
  sleep 1
done

echo "Started.."

echo setup.sh time now: `date +"%T" `
mongosh --host ${mongodb1}:${port1} <<EOF
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
