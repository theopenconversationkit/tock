import os
from mkdocs.config.defaults import MkDocsConfig
from mkdocs.structure.files import Files
from mkdocs.structure.pages import Page

def on_page_markdown(
    markdown: str, *, page: Page, config: MkDocsConfig, files: Files
):   
    docs_dir = config['docs_dir']
       
    includes_dir = os.path.join(docs_dir, '../includes') 
    chatbot_path = os.path.join(includes_dir, 'chatbot.md') 
    
    if not os.path.exists(chatbot_path):
        print(f"⚠️ Fichier 'chatbot.md' introuvable à {chatbot_path}")
        return markdown  

    with open(chatbot_path, 'r', encoding='utf-8') as f:
        chatbot_content = f.read()
   
    markdown += "\n\n" + chatbot_content  
   
 
    if page.file.url in ('fr/index.md', 'en/index.md'):
        path_to_docs_root = './'  
    else:

        depth = len(page.file.url.split('/')) - 1
        path_to_docs_root = '../' * depth

    markdown = markdown.replace('{{PATH_TO_DOCS_ROOT}}', path_to_docs_root)

    return markdown
