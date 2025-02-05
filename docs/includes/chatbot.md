<!-- Chat inclusion start -->
<div>
  <script src="https://unpkg.com/vue@3.4/dist/vue.global.prod.js"></script>
  <link
    rel="stylesheet"
    href="https://unpkg.com/tock-vue-kit@0.3.6/dist/style.css"
  />
  <script
    crossorigin
    src="https://unpkg.com/tock-vue-kit@0.3.6/dist/tock-vue-kit.iife.js"
  ></script>
  <style>
    :root {
      --tvk_base_font-size: 1em;
      --tvk_base_radius: 0;
      --tvk_colors_brand-hue: 207;
      --tvk_colors_brand-lightness: 54%;
      --tvk_colors_brand-saturation: 90%;
      --tvk_colors_light_background: white;
      --tvk_colors_light_surface2: hsl(var(--tvk_colors_brand-hue) 10% 95%);
      --tvk_footnotes_margin: 2em 0em 0em 0em;
      --tvk_footnotes_sources-title_display: block;
      --tvk_footnotes_flex-wrap: wrap;
      --tvk_footnotes_margin: 1em 0em 0em 0em;
      --tvk_question_box-shadow: inset 0px 0px 5px #00000030;
      --tvk_message_body_line-height: 1.3;
      --tvk_message_header_avatar_radius: unset;
      --tvk_message_header_avatar_bot_background: transparent;
      --tvk_message_header_font-weight: 300;
      --tvk_wrapper_padding: 0.5em;
      --tvk_wrapper_max-width: 45vw;
      --tvk_wrapper_min-height: 25em;
    }
    #chat-wrapped {
      display: none;
      position: fixed;
      bottom: 0.5em;
      right: 0.5em;
      min-width: 20em;
      flex-direction: column;
      background-color: white;
      box-shadow: 0 0 5px rgba(0, 0, 0, 0.2);
      transition: all 0.3s ease;
      animation: fadeIn 0.8s ease-in-out;
    }
    .chat-header {
      background-color: var(--md-primary-fg-color);
      color: white;
      padding: 0.25rem 0.5rem;
      font-weight: 100;
      font-size: 1rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .chat-header-title {
      display: flex;
      align-items: center;
    }
    .chat-header-title-logo {
      height: 1.5em !important;
      margin-right: 0.25em;
    }
    #chat-header-close {
      margin-top: -3px;
    }
    #chat-container {
      flex-grow: 1;
    }
    #chat-icon {
      position: fixed;
      bottom: 20px;
      right: 20px;
      width: 60px;
      height: 60px;
      background-color: var(--md-primary-fg-color);
      border-radius: 50%;
      color: white;
      font-size: 2rem;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
    }
    #chat-icon:hover {
      background-color: var(--md-accent-fg-color);
    }
    .pointer {
      cursor: pointer;
    }
    @keyframes fadeIn {
      from {
        opacity: 0;
        transform: translateY(10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
  </style>

  <!-- Icône de chat flottant -->
  <div id="chat-icon" onclick="toggleChat()" title="Chat with Tock">
    <img src="{{PATH_TO_DOCS_ROOT}}img/Logo_Tock_White.svg" />
  </div>

  <!-- Fenêtre du chatbot -->
  <div id="chat-wrapped">
    <div class="chat-header">
      <div class="chat-header-title">
        <img src="{{PATH_TO_DOCS_ROOT}}img/Logo_Tock_White.svg" class="chat-header-title-logo" />
        <span id="chat-header-title-string">Chat with Tock</span>
      </div>
      <span
        id="chat-header-close"
        onclick="closeChat()"
        class="pointer"
        title="Close chat"
        >&times;</span
      >
    </div>
    <div id="chat-container"></div>
  </div>

  <script>
    function toggleChat() {
      const chatWindow = document.getElementById("chat-wrapped");

      if (
        chatWindow.style.display === "" ||
        chatWindow.style.display === "none"
      ) {
        chatWindow.style.display = "flex";
      } else {
        chatWindow.style.display = "none";
      }
    }

    function closeChat() {
      const chatWindow = document.getElementById("chat-wrapped");
      chatWindow.style.display = "none";
    }

    let i18n = {
      title: "Chat with Tock",
      initialization: {
        welcomeMessage: "Hello, would you like to talk about Tock?",
      },
      wording: {
        messages: {
          message: {
            header: {
              labelUser: "You",
            },
            footnotes: {
              sources: "Sources:",
              showMoreLink: "> Show more",
            },
          },
        },
        questionBar: {
          clearHistoryAriaLabel: "Clear discussion and history button",
          input: {
            placeholder: "Ask me anything about Tock...",
          },
          submitAriaLabel: "Submit button",
        },
        connectionErrorMessage:
          "An unexpected error occured. Please try again later.",
      },
    };

    if (document.documentElement.getAttribute("lang") === "fr") {
      document.getElementById("chat-icon").title = "Discutez avec Tock";
      document.getElementById("chat-header-close").title = "Fermer le chat";
      document.getElementById("chat-header-title-string").innerText =
        "Discutez avec Tock";

      i18n = {
        initialization: {
          welcomeMessage: "Bonjour, vous souhaitez parler de Tock ?",
        },
        wording: {
          messages: {
            message: {
              header: {
                labelUser: "Vous",
              },
              footnotes: {
                sources: "Sources :",
                showMoreLink: "> Plus",
              },
            },
          },
          questionBar: {
            clearHistoryAriaLabel:
              "Bouton d'effacement de la discussion et de l'historique",
            input: {
              placeholder: "Demandez-moi n'importe quoi sur Tock...",
            },
            submitAriaLabel: "Bouton d'envoi",
          },
          connectionErrorMessage:
            "Une erreur inattendue s'est produite. Veuillez réessayer plus tard.",
        },
      };
    }

    const tockUrl = "https://demo-bot.tock.ai/io/tock/tockbot/web";

    TockVueKit.renderChat(document.getElementById("chat-container"), tockUrl, {
      localStorage: {
        enabled: true,
      },
      initialization: {
        welcomeMessage: i18n.initialization.welcomeMessage,
      },
      preferences: {
        messages: {
          message: {
            header: {
              avatar: {
                botImage: {
                  src: "{{PATH_TO_DOCS_ROOT}}img/favicon.png",
                  width: "1.5em",
                  height: "1.5em",
                },
              },
            },
          },
          footNotes: {
            requireSourcesContent: true,
          },
        },
      },
      wording: {
        messages: {
          message: {
            header: {
              labelBot: "Toki",
              labelUser: i18n.wording.messages.message.header.labelUser,
            },
            footnotes: {
              sources: i18n.wording.messages.message.footnotes.sources,
              showMoreLink:
                i18n.wording.messages.message.footnotes.showMoreLink,
            },
          },
        },
        questionBar: {
          clearHistoryAriaLabel: i18n.wording.questionBar.clearHistoryAriaLabel,
          input: {
            placeholder: i18n.wording.questionBar.input.placeholder,
          },
          submitAriaLabel: i18n.wording.questionBar.submitAriaLabel,
        },
        connectionErrorMessage: i18n.wording.connectionErrorMessage,
      },
    });
  </script>
</div>
<!-- Chat inclusion end -->
