import { lexer } from 'marked';
import { VFile } from 'rehype-raw/lib';
import { unified } from 'unified';
import remarkParse from 'remark-parse';
import remarkGfm from 'remark-gfm';
import remarkGemoji from 'remark-gemoji';
import remarkRehype from 'remark-rehype';
import remarkStringify from 'remark-stringify';
import rehypeFormat from 'rehype-format';
import rehypeRaw from 'rehype-raw';
import rehypeSanitize from 'rehype-sanitize';
import rehypeRemark from 'rehype-remark';
import rehypeParse from 'rehype-parse';
import rehypeStringify from 'rehype-stringify';

export enum MarkupFormats {
  PLAINTEXT = 'PLAINTEXT',
  MARKDOWN = 'MARKDOWN',
  HTML = 'HTML'
}

export async function markdownToHtml(rawData: string): Promise<VFile> {
  const processor = unified()
    .use(remarkParse)
    .use(remarkGfm)
    .use(remarkGemoji)
    .use(remarkRehype, { allowDangerousHtml: true })
    .use(rehypeRaw)
    .use(rehypeSanitize)
    .use(rehypeFormat)
    .use(rehypeStringify);

  return await processor.process(rawData);
}

export async function htmlToMarkdown(rawData: string) {
  const processor = unified().use(rehypeParse).use(rehypeSanitize).use(rehypeRemark).use(remarkGfm).use(remarkStringify);
  return await processor.process(rawData);
}

export function containsHTML(str: string) {
  var a = document.createElement('div');
  a.innerHTML = str;

  for (var c = a.childNodes, i = c.length; i--; ) {
    if (c[i].nodeType == 1) return true;
  }

  return false;
}

export function containsMARKDOWN(str: string): boolean {
  function containsNonTextOrHtmlTokens(tokens) {
    return tokens.some((token) => {
      if (!['text', 'paragraph', 'html', 'space'].includes(token.type)) {
        return true;
      }
      // Check recursively for nested tokens
      if (token.tokens && containsNonTextOrHtmlTokens(token.tokens)) {
        return true;
      }
      return false;
    });
  }

  return containsNonTextOrHtmlTokens(lexer(str));
}

export function detectMarkupFormat(data: string) {
  if (containsHTML(data)) return MarkupFormats.HTML;

  if (containsMARKDOWN(data)) return MarkupFormats.MARKDOWN;

  return MarkupFormats.PLAINTEXT;
}
