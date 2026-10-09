---
title: RAG prompt
description: "Write and tune the answering prompt of a Tock RAG bot, step by step."
---

# The RAG prompt

> **Audience:** developers, integrators and prompt designers.
> The structure of the prompt and its JSON output are detailed in the [RAG prompt reference](rag-prompt-reference.md).

## In short: writing the prompt of your bot

1. Start from the template closest to your use case: end-user customer bot, internal business advisor or
   developer / operations bot (see [Use-case typology](#use-case-typology)).
2. Keep **Section 1** (system rules) as is, and fill in **Section 2** (business rules) following the
   [configuration guide](#configuring-a-new-bot-section-2-guide).
3. Manage the covered topics, excluded topics and business lexicon in the [_Rag prompt context_](rag-prompt-context.md)
   menu rather than in the prompt text: they are injected at runtime.
4. Paste the prompt in the _Question answering_ section of the [_Rag settings_](rag.md#question-answering), after trying
   it in the [playground](playground.md).
5. Check the effect on a [dataset](answers-quality.md#datasets) of representative questions
   (see [Improving the answers](improve.md)).

The `status`, `topic` and `suggested_topics` fields of the answer feed the RAG indicators of the dashboard and of
_Metrics_, and `redirection_intent` switches the conversation to a story
(see [How the bot answers](how-it-works.md#redirecting-from-the-rag-to-a-story)).

## Overview

[Tock](https://doc.tock.ai) is an open-source platform for building conversational AI bots, used across a wide range of industries and organizations. The RAG prompt framework described in this document defines a **structured, reusable prompt architecture** for LLM-based chatbots operating within Tock's RAG (Retrieval-Augmented Generation) pipeline.

The key feature of this framework is that the LLM is instructed to return a **strictly structured JSON object** instead of a free-text response. This output contract enables downstream systems to:

- Route responses programmatically based on a `status` field
- Trigger actions (e.g. human escalation) via a `redirection_intent` field
- Audit which retrieved documents were used via a `context_usage` array
- Monitor confidence and topic classification without additional NLP processing

All prompts share the same structural skeleton. Only the **Business Rules (Section 2)** vary between deployments.

## Configuring a New Bot — Section 2 Guide

To deploy a new bot using this framework, only **Section 2** needs to be authored. The other sections are reused as-is (with optional relaxation of Section 1 constraints as needed).

### Step 1 — Define Bot Identity (2.1)

```markdown
- **Name:** <Bot name>
- **Role:** <What does the bot do and for whom>
- **Domain:** <The knowledge domain it operates in>
- **Target Audience:** <Who will interact with it>
- **Response language:** {{locale}}
```

**Tips:**

- Be specific about the audience — it influences tone and technicality defaults.
- The domain statement is used by the LLM for domain validation (Section 1.1).

### Step 2 — Define Scope (2.2)

List the topics the bot **will** and **will not** cover.

```markdown
**Covered Topics:**

- Small talk
- <Topic A>
- <Topic B>

**Excluded Topics:**

- Personal/Private Matters
- Legal advice
- <Any domain-specific exclusion>
```

**Tips:**

- Always include `Small talk` in covered topics to allow greetings and chitchat.
- Be explicit in excluded topics — ambiguity leads to inconsistent `out_of_scope` behavior.
- Topic names here become the allowed values for the `topic` field in the JSON output.

### Step 3 — Set Response Expectations (2.3)

```markdown
- **Required Depth Level:** <Concise / Detailed / Balanced>
- **Level of Technicality:** <Low / Moderate / High>
- **Assumptions Allowed:** <What can the bot assume about the user's knowledge>
```

### Step 4 — Define Style & Tone (2.4)

```markdown
- **Tone:** <Formal / Neutral / Friendly / Empathetic / ...>
- **Formatting:** <Markdown / Plain text / Bullet points / Code blocks / ...>
- **Vocabulary Constraints:** <Jargon allowed? Which terminology to use?>
```

**Tips:**

- For end-customer bots: use plain, accessible language.
- For internal technical bots: Markdown with code blocks is recommended.
- Specify whether the bot should use `tu` or `vous` for French, or equivalent formality markers for other languages.

### Step 5 — Add Domain-Specific Constraints (2.5)

```markdown
- **Regulatory Constraints:** <e.g. no financial/legal advice>
- **Compliance Rules:** <e.g. data privacy requirements>
- **Forbidden Statements:** <e.g. no speculation on unreleased products>
- **Mandatory Mentions:** <e.g. always cite product name with month + year>
- **Smart suggestions:** <e.g. suggest related topics when unable to answer>
- **Absolute URLs only:** <never generate relative links>
```

### Step 6 — Add Specific Instructions if Needed (2.6)

This optional subsection is for any logic that does not fit the standard fields. Common examples:

| Use Case                   | Example Instruction                                                           |
| -------------------------- | ----------------------------------------------------------------------------- |
| Product disambiguation     | Require the user to specify month + year when multiple product variants exist |
| Human escalation logic     | Define when to offer escalation and how to trigger it                         |
| Fallback contacts          | Provide a fallback email if the context cannot answer                         |
| Multi-environment handling | Request clarification when multiple environments are possible                 |

### Step 7 — Review Section 1 Constraints

Decide whether the default Section 1 constraints are appropriate for this bot:

| Constraint           | Default | When to relax                                        |
| -------------------- | ------- | ---------------------------------------------------- |
| RAG-only answers     | Strict  | Bot handles dev/IT topics not covered by documents   |
| Anti-hallucination   | Strict  | Generally keep strict for business-sensitive domains |
| Injection protection | Strict  | Never relax                                          |
| Domain validation    | Strict  | Never relax                                          |

When relaxing Section 1.2 (RAG Policy), add an explicit note such as:

> _For topics outside the core business domain (e.g. general development questions, code generation), the LLM may draw on its native knowledge when no relevant context is retrieved._

## Use-Case Typology

Because Tock is an open-source platform used across a wide range of industries and organizations, the RAG prompt framework must accommodate very different deployment contexts. Three archetypal use cases have been identified, each with distinct configuration priorities.

### Type A — End-User Customer Bot

**Example Prompt:** [Type A Prompt template](https://github.com/theopenconversationkit/tock/blob/master/docs/docs/en/gen-ai/prompt-examples/prompt-example-type-a-end-user-customer-bot.md)

**Profile:** A bot exposed directly to the general public or to a company's end customers. Users have no specific domain expertise and expect simple, reassuring, accessible answers.

**Typical deployment contexts:** Retail banking, e-commerce, insurance, public services, telecoms customer support.

**Key characteristics:**

| Dimension                  | Guidance                                                                                  |
| -------------------------- | ----------------------------------------------------------------------------------------- |
| **Tone**                   | Warm, empathetic, polite, supportive                                                      |
| **Technicality**           | Low — avoid jargon entirely                                                               |
| **RAG strictness**         | High — answers must be strictly grounded in documentation                                 |
| **Human escalation**       | Strongly recommended — offer live handoff when confidence is low or the query is personal |
| **Formatting**             | Plain text, short sentences, no Markdown syntax                                           |
| **Scope**                  | Narrow and well-defined — out-of-scope refusal must be clear but non-frustrating          |
| **Regulatory constraints** | High — no legal or financial advice, no assumptions about the user's personal situation   |

**Specific instructions to consider (Section 2.6):**

- Define explicit escalation triggers (e.g. when context is insufficient, or when the user's situation is too individual to be handled generically).
- Distinguish between a live human agent (reachable via the chat) and the user's personal advisor (who cannot be contacted via this channel).
- Use `redirection_intent` to trigger seamless frontend handoff without breaking the conversation.

### Type B — Internal Business Advisor Bot

**Example Prompt:** [Type B Prompt template](https://github.com/theopenconversationkit/tock/blob/master/docs/docs/en/gen-ai/prompt-examples/prompt-example-type-b-internal-business-advisor-bot.md)

**Profile:** A bot assisting employees or domain experts within an organization — advisors, sales teams, analysts, or operational staff. Users have domain knowledge but need quick, reliable access to structured product or process information.

**Typical deployment contexts:** Sales support, product knowledge bases, compliance guidance, internal procedures, field advisor assistance.

**Key characteristics:**

| Dimension                  | Guidance                                                                                  |
| -------------------------- | ----------------------------------------------------------------------------------------- |
| **Tone**                   | Formal, professional, direct                                                              |
| **Technicality**           | Moderate — domain terminology is acceptable and expected                                  |
| **RAG strictness**         | High — answers must come from official documentation; no improvisation on business topics |
| **Human escalation**       | Optional — a fallback contact (email, internal channel) is often sufficient               |
| **Formatting**             | Structured: bold key terms, bullet points, clear sections                                 |
| **Scope**                  | Focused on specific product lines, processes, or knowledge areas                          |
| **Regulatory constraints** | Moderate to high depending on domain (financial products, compliance, etc.)               |

**Specific instructions to consider (Section 2.6):**

- Add product or entity disambiguation logic when multiple similar items exist (e.g. products differentiated by date, version, or region).
- Define mandatory mention rules (e.g. always cite the full product name including version or date).
- Provide a fallback contact point when the documentation does not cover the question.

### Type C — Developer & Technical Operations Bot

**Example Prompt:** [Type C Prompt template](https://github.com/theopenconversationkit/tock/blob/master/docs/docs/en/gen-ai/prompt-examples/prompt-example-type-c-developer-ops-bot.md)

**Profile:** A bot assisting engineers, DevOps teams, or technical operators. Users are highly technical and expect precise, actionable answers — including code, commands, architecture patterns, and debugging guidance.

**Typical deployment contexts:** Infrastructure documentation, application stack support, internal developer portals, OPS runbooks, CI/CD guidance.

**Key characteristics:**

| Dimension                  | Guidance                                                                                                                                                                     |
| -------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Tone**                   | Neutral, concise, peer-to-peer                                                                                                                                               |
| **Technicality**           | High — technical jargon is expected and appropriate                                                                                                                          |
| **RAG strictness**         | **Relaxed** — the LLM may use its native knowledge for generic technical topics (code patterns, standard tools, common architectures) when retrieved context is insufficient |
| **Human escalation**       | Rarely needed — redirect to internal channels (team leads, OPS referents) when out of scope                                                                                  |
| **Formatting**             | Mandatory Markdown: fenced code blocks, inline `code`, bold for key terms                                                                                                    |
| **Scope**                  | Broad technical scope with explicit exclusions (e.g. no HR, no legal)                                                                                                        |
| **Regulatory constraints** | Low for generic topics; may be higher for security-sensitive procedures                                                                                                      |

**Specific instructions to consider (Section 2.6):**

- Explicitly state that the LLM may draw on native knowledge for topics not covered by documentation (coding patterns, tool usage, generic IT concepts).
- Request clarification when a question could apply to multiple environments, stacks, or configurations.
- Apply smart suggestion logic: when unable to answer, suggest related concepts or tools present in the context.
- Only provide absolute URLs — never generate relative links.

### Typology Comparison Summary

| Dimension                   | Type A — End-User          | Type B — Business Advisor      | Type C — Developer / OPS        |
| --------------------------- | -------------------------- | ------------------------------ | ------------------------------- |
| **Primary audience**        | General public / customers | Domain experts / employees     | Engineers / technical operators |
| **Tone**                    | Warm, empathetic           | Formal, professional           | Neutral, concise                |
| **Technicality**            | Low                        | Moderate                       | High                            |
| **RAG strictness**          | High                       | High                           | Relaxed for generic topics      |
| **Human escalation**        | ✅ Recommended             | ⚠️ Optional (fallback contact) | ❌ Rarely needed                |
| **Formatting**              | Plain text                 | Structured prose               | Markdown + code blocks          |
| **Scope width**             | Narrow                     | Focused                        | Broad technical                 |
| **Key Section 2.6 concern** | Escalation logic           | Disambiguation logic           | Native knowledge relaxation     |

> These three types are reference profiles, not rigid categories. A real deployment may blend characteristics from multiple types depending on the organization's needs and the target audience's profile.
