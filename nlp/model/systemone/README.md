# System One NLP engine

Integration of the **System One decision-model API contract** as a Tock NLP
engine talking to a REST endpoint.

The client is compatible both with the base Jev API contract,
and Cloudflare's variant for the [Clef model](https://blog.cloudflare.com/clef-decision-models/).

## Intent classification only

The System One API describes a **decision model**, not a span-level tagger:
given a text and a set of allowed options, it returns a probability per option
in a single forward pass. This maps naturally onto Tock's intent
classification (one `choice` question whose `criteria` are the application's
intents), but existing decision models have **no entity extraction capability**.
`SystemOneEntityClassifier` always returns an empty list.

To still get entities for an application that uses System One for intents, set
a **dedicated entity engine** (e.g. `opennlp`) on the application.
Entity build/parse calls are routed to that engine while intent calls keep using System One.

## API response formats

The client handles both Cloudflare's wrapped response (`result`, `success`, and
`errors`) and direct System One responses with top-level `model` and `answers`,
as returned by Jev and Strands Decider. Provider-specific metadata such as
`usage` and `latency_ms` is retained on the direct-response result variant.

## Intent count limits

A SystemOne `choice` question requires **between 2 and 255 criteria**:
- Applications with a **single** intent skip the API call entirely:
  `SystemOneIntentClassifier` short-circuits straight to that intent at
  probability `1.0`.
- Applications with **more than 255** intents are rejected with a clear
  `IllegalStateException` at classification time: consider splitting intents across several
  applications, or pairing this engine with a classic classifier instead.

Intent (and criteria option) ids are sent to the REST endpoint as-is, with no character
escaping: Cloudflare's `choice` schema places no restriction on option id
characters, so Tock's qualified intent names (e.g. `app:greetings`) are used
directly as both criteria keys and answer lookup keys.

## No credentials are persisted in model storage

The System One API has no local training step, so the "model" artifact persisted for an
application only stores a non-secret model-variant marker (`jev`,
`clef-flash`, etc.). The endpoint URL and API token are **never**
serialized into Tock's model storage: `SystemOneEngineProvider` always builds a
fresh `SystemOneConfiguration` from live properties at classification time, the
same way `healthcheck()` does. This avoids leaking the API token into the
model database/backups and keeps credential rotation consistent across all call sites.

## How intent descriptions are used

Classification benefits from a short, human-readable description per
option so the model can decide between them. `SystemOneIntentClassifier` builds
these criteria from the **intent description set in TOCK Studio**
(`IntentDefinition.description`, propagated to the core
`ai.tock.nlp.core.Intent.description` field at classification time).

If an intent has no description, the model will rely only on its namespace and ID.
Setting clear, specific descriptions for every intent in TOCK Studio materially improves models'
classification accuracy - it is the main "feature engineering" lever
available with this engine.

## Configuring the intent question

The intent question's `instructions` text can be overridden in TOCK Studio.
Open the application's advanced options, select **NLU Engine configuration**,
and set the following in **Intent Classifier properties**:

```properties
intent_question_instructions=Choose the intent that best matches the user's message.
```

If the property is missing or blank, the default is
`Select the intent that best matches the user message.` Save the configuration
and trigger an application model build for the change to take effect.

## Configuration

| Property                      | Default      | Description                                                                                                                                                                                           |
|-------------------------------|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `tock_systemone_endpoint_url` | (empty)      | Full URL of the decision-model endpoint. For Cloudflare, include account and model path segments, e.g. `https://api.cloudflare.com/client/v4/accounts/<account-id>/ai/run/@cf/cloudflare/clef-flash`. |
| `tock_systemone_api_token`    | (empty)      | API token (Bearer)                                                                                                                                                                                    |
| `tock_systemone_model`        | `clef-flash` | Model identifier sent in the request body; for Cloudflare, keep it consistent with the model in the endpoint URL.                                                                                     |
| `tock_systemone_timeout_ms`   | `10000`      | Request timeout in milliseconds                                                                                                                                                                       |

## Testing

- `SystemOneClientTest`, `SystemOneIntentClassifierTest`, `SystemOneEntityClassifierTest`,
  `SystemOneNlpModelBuilderTest`: unit tests using a `MockWebServer`, no
  network/credentials required.
- `SystemOneClientIntegrationTest`: `@Disabled` by default - it calls a real (possibly billed) REST endpoint.
  Set the full endpoint URL and token (`tock_systemone_endpoint_url` / `tock_systemone_api_token`) and remove
  `@Disabled` to run it locally.
