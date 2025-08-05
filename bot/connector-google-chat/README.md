## Prerequisites

1. **Create and publish a Google Chat bot**  
   Follow the instructions here:  
   https://developers.google.com/hangouts/chat/how-tos/bots-publish

2. **IAM Google Cloud Permissions**  
    - `chat.bots.get`
    - `chat.bots.update`

3. **Retrieve the following elements from the Google Cloud Console**:
    - **Bot project number** , this is the Google Cloud Project number (numerical one, not the project ID). 
      Example: `37564789203`
    - **JSON credentials**  
      Downloaded when you create a service account with the `Project Owner` role.

4. **Configurate Google Chat API for your project**:
    - `HTTP endpoint URL`: Your ngrok URL + TOCK Relative REST path. (Example : https://area-simple-teal.ngrok-free.app/io/app/assistant/google_chat)
    - `Authentification Audience`: Select 'Project Number'.

⚠️ For now, dialogs will reset every 24 hours as stated in the variable dialogMaxValidityInSeconds of `UserTimelineMongoDAO.kt`
   
## Tock Configuration

1. Go to **Settings > Configuration > New Configuration** in the Tock admin interface.

2. Create a new Google Chat configuration specifying the following options:
    - `Connector type`: google_chat.
    - `Application base URL`: Your ngrok URL (Example : https://area-simple-teal.ngrok-free.app).
    - `Bot project number`: Bot project number (Example : 37564789203).
    - `Service account credential json content`: raw JSON content pasted from the credential file.
    - `Use condensed footnotes`: If activated, sources will be shown as [1] [2] [3]... True = 1, False = 0.

## Bot API

To be implemented and tested.

## Integrated Mode (Local Development)

To connect your bot to a Google Chat bot application in local development:

1. Set your bot's URL in the Google Cloud Console.  
   This URL must match the path configured in the Tock configuration.

2. Use a secure tunnel to expose your local bot endpoint.  
   Example using [ngrok](https://ngrok.com/):

   ```sh
   ngrok http 8080
