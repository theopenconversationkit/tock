# Auto updating facebook webhook for dev with ngrok 

* This script allows to create/update a webhook subscription to a facebook app for dev with a secure ssh tunnel listening on port 8080
    * https://developers.facebook.com/docs/graph-api/reference/v3.2/app/subscriptions
    * https://ngrok.com


* Make sure that ngrok is installed on the same directory 
* Modify scripted file permissions with chmod (Linux user) (chmod 755 update.sh)
* Update .env file with facebook properties
    * **FB_APP_ID** : The Messenger application id.  
    * **FB_APP_ACCESS_TOKEN** : The acces token facebook app. (Check https://developers.facebook.com/tools/access_token/ 
    * **FB_APP_FIELDS** : The set of fields that are subsciped to ( For example : "messages,messaging_postbacks,messaging_optins,messaging_account_linking")
    * **FB_APP_VERIFY_TOKEN** : A token (choose what you want) used when registering the webhook in the Messenger admin interface.
* Usage: ./update.sh