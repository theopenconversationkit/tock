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

echo "1 - Loading configuration"
source .env
echo "2 - Killing existing ngrok process"
unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     killall ngrok;;
    Darwin*)    killall ngrok;;
    CYGWIN*)    tskill ngrok;;
    MINGW*)     tskill ngrok;;
esac
echo "3 - Launching ngrok tunnel"
./ngrok http 8080 --bind-tls "true" &
echo "4 - Waiting for ngrok"
sleep 30s
host=$(curl --silent --show-error http://127.0.0.1:4040/api/tunnels | sed -nE 's/.*public_url":"https:..([^"]*).*/\1/p')
echo "5 - ngrok host initialized : $host"
url="https://graph.facebook.com/v3.2/$FB_APP_ID/subscriptions?access_token=$FB_APP_ACCESS_TOKEN&object=page&callback_url=https://$host/messenger&fields=$FB_APP_FIELDS&verify_token=$FB_APP_VERIFY_TOKEN"
echo "6- Updating webhook facebook url : $url"
curl -X POST $url
