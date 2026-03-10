/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { lexer, Marked } from 'marked';
import type { Tokens, TokenizerExtension, RendererExtension } from 'marked';
import katex from 'katex';

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
import { markedHighlight } from 'marked-highlight';
import DOMPurify from 'dompurify';
import hljs from 'highlight.js';

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

export async function htmlToMarkdown(rawData: string): Promise<VFile> {
  const processor = unified().use(rehypeParse).use(rehypeSanitize).use(rehypeRemark).use(remarkGfm).use(remarkStringify);
  return await processor.process(rawData);
}

export function htmlToPlainText(rawData: string): string {
  const a = document.createElement('div');
  a.innerHTML = rawData;

  return a.textContent || a.innerText || '';
}

export function containsHTML(str: string): boolean {
  const a = document.createElement('div');
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

export function detectMarkupFormat(data: string, options = { checkForHtml: true, checkForMarkdown: true }) {
  if (options.checkForHtml && containsHTML(data)) return MarkupFormats.HTML;

  if (options.checkForMarkdown && containsMARKDOWN(data)) return MarkupFormats.MARKDOWN;

  return MarkupFormats.PLAINTEXT;
}

interface katexBlockToken extends Tokens.Generic {
  type: 'katexBlock';
  raw: string;
  text: string;
  displayMode: true;
}

interface katexInlineToken extends Tokens.Generic {
  type: 'katexInline';
  raw: string;
  text: string;
  displayMode: false;
}

export const katexBlockExtension: TokenizerExtension & RendererExtension = {
  name: 'katexBlock',
  level: 'block',

  start(src: string): number | undefined {
    const match = src.match(/(\${2}|\\\[)/);
    return match ? match.index : -1;
  },

  tokenizer(src: string): katexBlockToken | undefined {
    // 1) $$ ... $$
    const rule1 = /^\${2}([\s\S]+?)\${2}/;
    const match1 = rule1.exec(src);
    if (match1) {
      const token: katexBlockToken = {
        type: 'katexBlock',
        raw: match1[0],
        text: match1[1].trim(),
        displayMode: true
      };
      return token;
    }

    // 2) \[ ... \]
    const rule2 = /^\\\[([\s\S]+?)\\\]/;
    const match2 = rule2.exec(src);
    if (match2) {
      const token: katexBlockToken = {
        type: 'katexBlock',
        raw: match2[0],
        text: match2[1].trim(),
        displayMode: true
      };
      return token;
    }

    return undefined;
  },

  renderer(token) {
    if (token.type === 'katexBlock') {
      return katex.renderToString(token.text, {
        throwOnError: false,
        displayMode: token.displayMode
      });
    }

    return undefined;
  }
};

export const katexInlineExtension: TokenizerExtension & RendererExtension = {
  name: 'katexInline',
  level: 'inline',

  start(src: string): number | undefined {
    const match = src.match(/(\$|\\\()/);
    return match ? match.index : -1;
  },

  tokenizer(src: string): katexInlineToken | undefined {
    // Matches $...$ inline math only when:
    // - not preceded by a digit (avoids currency like $20)
    // - no whitespace immediately after opening $ or before closing $
    // - content is non-empty and does not itself contain $
    // This avoids false positives on prices, variable names like $t, $route, etc.
    const rule1 = /^\$(?!\s)([^$\n]+?)(?<!\s)\$(?!\d)/;
    const match1 = rule1.exec(src);
    if (match1) {
      const token: katexInlineToken = {
        type: 'katexInline',
        raw: match1[0],
        text: match1[1].trim(),
        displayMode: false
      };
      return token;
    }

    // 2) \(...\)
    const rule2 = /^\\\(([\s\S]+?)\\\)/;
    const match2 = rule2.exec(src);
    if (match2) {
      const token: katexInlineToken = {
        type: 'katexInline',
        raw: match2[0],
        text: match2[1].trim(),
        displayMode: false
      };
      return token;
    }

    return undefined;
  },

  renderer(token) {
    if (token.type === 'katexInline') {
      return katex.renderToString(token.text, {
        throwOnError: false,
        displayMode: token.displayMode
      });
    }
    return undefined;
  }
};

export const markedParser = new Marked({
  async: false,
  ...markedHighlight({
    emptyLangClass: 'hljs',
    langPrefix: 'hljs language-',
    highlight(code, lang, info) {
      const language = hljs.getLanguage(lang) ? lang : 'plaintext';
      return hljs.highlight(code, { language }).value;
    }
  }),
  hooks: {
    postprocess: (html) => DOMPurify.sanitize(html)
  },
  extensions: [katexBlockExtension, katexInlineExtension],
  breaks: true,
  gfm: true
});

export const markedParserForDiff = new Marked({
  async: false,
  ...markedHighlight({
    emptyLangClass: 'hljs',
    langPrefix: 'hljs language-',
    highlight(code, lang, info) {
      const language = hljs.getLanguage(lang) ? lang : 'plaintext';
      return hljs.highlight(code, { language }).value;
    }
  }),
  hooks: {
    postprocess: (html) =>
      DOMPurify.sanitize(html, {
        // Allow spans with class 'added' or 'removed' for diff highlighting, but no other classes or tags to minimize XSS risks
        ADD_TAGS: ['span'],
        ADD_ATTR: ['class']
      })
  },
  breaks: true,
  gfm: true
});
