import re
import tiktoken  # Assurez-vous d'avoir installé tiktoken: pip install tiktoken


def clean_markdown(md_text: str) -> str:
    # Supprimer le gras et l'italique tout en conservant le texte
    md_text = re.sub(r'\*\*(.*?)\*\*', r'\1', md_text)  # Gras
    md_text = re.sub(r'\*(.*?)\*', r'\1', md_text)  # Italique
    md_text = re.sub(r'__(.*?)__', r'\1', md_text)  # Gras avec __
    md_text = re.sub(r'_(.*?)_', r'\1', md_text)  # Italique avec _

    # Conserver les titres Markdown tout en supprimant les espaces en trop
    md_text = re.sub(r'^(#{1,6})\s+', r'\1 ', md_text, flags=re.MULTILINE)

    # Nettoyer les tableaux Markdown en gardant la structure
    md_text = re.sub(r'\s*\|\s*', '|', md_text)  # Nettoyer les espaces autour des pipes
    md_text = re.sub(r'\|{2,}', '|', md_text)  # Supprimer les pipes en trop

    # Conserver les listes Markdown en supprimant les espaces inutiles
    md_text = re.sub(r'^[\-*+]\s+', '- ', md_text, flags=re.MULTILINE)

    # Assurer qu'il y a un retour à la ligne autour des tableaux
    md_text = re.sub(r'(\n?)(\|.+?\|)(\n?)', r'\n\2\n', md_text)

    # Réduire les sauts de ligne multiples à un seul sauf autour des tableaux
    md_text = re.sub(r'\n{3,}', '\n\n', md_text).strip()

    return md_text


def count_tokens(text: str, model: str = "gpt-3.5-turbo") -> int:
    encoding = tiktoken.encoding_for_model(model)
    return len(encoding.encode(text))


# Exemple d'utilisation
md_example = """
# Titre principal

**Texte en gras** et *texte en italique*.

| Colonne 1 | Colonne 2 |
|-----------|-----------|
| Valeur 1  | Valeur 2  |

- Élément de liste
"""

cleaned_md = clean_markdown(md_example)
n_tokens = count_tokens(cleaned_md)

print("Texte nettoyé:", cleaned_md)
print("Nombre de tokens:", n_tokens)
