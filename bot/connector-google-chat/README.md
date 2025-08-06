##  Prerequisites

### Create and publish a Google Chat bot

Follow the official instructions:  
👉 **[Google Chat Bot Publishing Guide](https://developers.google.com/hangouts/chat/how-tos/bots-publish)**

> ⚠️ **Important**: Each connector requires its own Google Cloud Project, as Chat API settings (bot avatar, display name, endpoint) are project-specific.

### IAM Google Cloud Permissions

Grant the following permissions:
- `chat.bots.get`
- `chat.bots.update`

> 💡 **Recommendation**: Create a specific role with these permissions and assign it to a service account.

### Retrieve information from Google Cloud Console

|  Required Element |  Description |
|---------------------|-----------------|
| **Bot project number** | The numeric ID of your project (e.g., `37564789203`) |
| **JSON credentials** | Service account credentials file |

### Configure Google Chat API

1.  Go to **[Google Chat API](https://console.cloud.google.com/marketplace/product/google/chat.googleapis.com)**
2. Click **Manage**, then go to the **[Configuration tab](https://console.cloud.google.com/apis/api/chat.googleapis.com/hangouts-chat)**
3. Configure the following settings:

```
HTTP endpoint URL       → https://your-ngrok-url.ngrok-free.app/io/app/assistant/google_chat
Authentication Audience → Project Number
```

---

##  Tock Configuration

### Configuration Steps

1.  In Tock admin UI: **Settings > Configuration > New Configuration**
2.  Create a Google Chat configuration with the following values:

|  Field |  Example |
|-----------|-------------|
| **Connector type** | `google_chat` |
| **Application base URL** | `https://area-simple-teal.ngrok-free.app` |
| **Bot project number** | `37564789203` |
| **Service account credential json content** | `{"type": "service_account", ...}` |
| **Use condensed footnotes** | `1` = condensed, `0` = detailed |

---

##  Footnote Display Examples

###  Condensed mode (`useCondensedFootnotes = 1`)

<img src="docs/condensed_mode.png" alt="Condensed mode"/>

---

###  Detailed mode (`useCondensedFootnotes = 0`)

<img src="docs/detailed_mode.png" alt="Detailed mode"/>

---

##  Markdown Support

The connector includes a converter that transforms standard Markdown into a simplified format compatible with Google Chat.

###  Supported Conversions

|  Markdown Input |  Google Chat Output |  Status |
|-------------------|------------------------|-----------|
| `**bold**` | `*bold*` | ✅ |
| `_italic_` | `_italic_` | ✅ |
| `~~strikethrough~~` | `~~strikethrough~~` | ✅ |
| `` `inline code` `` | `` `inline code` `` | ✅ |
| ```` ```code block``` ```` | ```` ```code block``` ```` | ✅ |
| `# Heading` | `*Heading*` | ✅ |
| `* list item` | `* list item` | ✅ |
| `[Label](https://link.com)` | `<https://link.com\|Label>` | ✅ |
| `[](https://link.com)` | `<https://link.com\|https://link.com>` | ✅ |

> ⚠️ **Limitation**: Google Chat does not support full Markdown. Formatting is automatically simplified.

---

##  Bot Behavior

###  Conversation Management

- **In a room**: The bot replies in the **same thread** as the user message
- **Direct message or system event**: The bot starts a **new thread**
- **Fallback mechanism**: Uses `REPLY_MESSAGE_FALLBACK_TO_NEW_THREAD` to ensure delivery

---

##  Local Development (Integrated Mode)

### Configuration for Local Testing

1. ** URL Configuration**: Set your bot's URL in the Google Cloud Console
   - The URL must match the path configured in Tock

2. ** Secure Exposure**: Use a tunnel to expose your local endpoint securely

#### Example with ngrok

```bash
# Installation (if needed)
npm install -g ngrok

# Expose local port
ngrok http 8080
```

**Expected output:**
```
Session Status    online
Version          2.3.40
Region           United States (us)
Web Interface    http://127.0.0.1:4040
Forwarding       https://area-simple-teal.ngrok-free.app -> http://localhost:8080
```

3. ** Update Configuration**: Use the generated ngrok URL in your Google Cloud Console configuration
