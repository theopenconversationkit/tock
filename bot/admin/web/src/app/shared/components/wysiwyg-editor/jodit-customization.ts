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

import { Jodit } from 'jodit';

import 'node_modules/jodit/esm/plugins/fullsize/fullsize.js';
import 'node_modules/jodit/esm/plugins/symbols/symbols.js';
import 'node_modules/jodit/esm/plugins/hr/hr.js';
import 'node_modules/jodit/esm/plugins/clean-html/clean-html.js';
import 'node_modules/jodit/esm/plugins/mobile/mobile.js';
import 'node_modules/jodit/esm/plugins/spellcheck/spellcheck.js';
import 'node_modules/jodit/esm/plugins/image-properties/image-properties.js';
import 'node_modules/jodit/esm/plugins/select/select.js';
import 'node_modules/jodit/esm/plugins/select-cells/select-cells.js';
import 'node_modules/jodit/esm/plugins/table-keyboard-navigation/table-keyboard-navigation.js';
import 'node_modules/jodit/esm/plugins/resizer/resizer.js';
import 'node_modules/jodit/esm/plugins/add-new-line/add-new-line.js';
import 'node_modules/jodit/esm/plugins/justify/justify.js';
import 'node_modules/jodit/esm/plugins/resize-handler/resize-handler.js';

Jodit.modules.Icon.set(
  'h1',
  '<svg viewBox="0 0 16 16"><path d="M7.648 13V3H6.3v4.234H1.348V3H0v10h1.348V8.421H6.3V13zM14 13V3h-1.333l-2.381 1.766V6.12L12.6 4.443h.066V13z"/></svg>'
);
Jodit.modules.Icon.set(
  'h2',
  '<svg viewBox="0 0 16 16"><path d="M7.495 13V3.201H6.174v4.15H1.32V3.2H0V13h1.32V8.513h4.854V13zm3.174-7.071v-.05c0-.934.66-1.752 1.801-1.752 1.005 0 1.76.639 1.76 1.651 0 .898-.582 1.58-1.12 2.19l-3.69 4.2V13h6.331v-1.149h-4.458v-.079L13.9 8.786c.919-1.048 1.666-1.874 1.666-3.101C15.565 4.149 14.35 3 12.499 3 10.46 3 9.384 4.393 9.384 5.879v.05z"/></svg>'
);
Jodit.modules.Icon.set(
  'h3',
  '<svg viewBox="0 0 16 16"><path d="M11.07 8.4h1.049c1.174 0 1.99.69 2.004 1.724s-.802 1.786-2.068 1.779c-1.11-.007-1.905-.605-1.99-1.357h-1.21C8.926 11.91 10.116 13 12.028 13c1.99 0 3.439-1.188 3.404-2.87-.028-1.553-1.287-2.221-2.096-2.313v-.07c.724-.127 1.814-.935 1.772-2.293-.035-1.392-1.21-2.468-3.038-2.454-1.927.007-2.94 1.196-2.981 2.426h1.23c.064-.71.732-1.336 1.744-1.336 1.027 0 1.744.64 1.744 1.568.007.95-.738 1.639-1.744 1.639h-.991V8.4ZM7.495 13V3.201H6.174v4.15H1.32V3.2H0V13h1.32V8.513h4.854V13z"/></svg>'
);
Jodit.modules.Icon.set(
  'h4',
  '<svg viewBox="0 0 16 16"><path d="M13.007 3H15v10h-1.29v-2.051H8.854v-1.18C10.1 7.513 11.586 5.256 13.007 3m-2.82 6.777h3.524v-5.62h-.074a95 95 0 0 0-3.45 5.554zM7.495 13V3.201H6.174v4.15H1.32V3.2H0V13h1.32V8.513h4.854V13z"/></svg>'
);
Jodit.modules.Icon.set(
  'h5',
  '<svg viewBox="0 0 16 16"><path d="M9 10.516h1.264c.193.976 1.112 1.364 2.01 1.364 1.005 0 2.067-.782 2.067-2.247 0-1.292-.983-2.082-2.089-2.082-1.012 0-1.658.596-1.924 1.077h-1.12L9.646 3h5.535v1.141h-4.415L10.5 7.28h.072c.201-.316.883-.84 1.967-.84 1.709 0 3.13 1.177 3.13 3.158 0 2.025-1.407 3.403-3.475 3.403-1.809 0-3.1-1.048-3.194-2.484ZM7.495 13V3.201H6.174v4.15H1.32V3.2H0V13h1.32V8.512h4.854V13z"/></svg>'
);
Jodit.modules.Icon.set(
  'h6',
  '<svg viewBox="0 0 16 16"><path d="M15.596 5.178H14.3c-.106-.444-.62-1.072-1.706-1.072-1.332 0-2.325 1.269-2.325 3.947h.07c.268-.67 1.043-1.445 2.445-1.445 1.494 0 3.017 1.064 3.017 3.073C15.8 11.795 14.37 13 12.48 13c-1.036 0-2.093-.36-2.77-1.452C9.276 10.836 9 9.808 9 8.37 9 4.656 10.494 3 12.636 3c1.812 0 2.883 1.113 2.96 2.178m-5.151 4.566c0 1.367.944 2.15 2.043 2.15 1.128 0 2.037-.684 2.037-2.136 0-1.41-1-2.065-2.03-2.065-1.19 0-2.05.853-2.05 2.051M7.495 13V3.201H6.174v4.15H1.32V3.2H0V13h1.32V8.513h4.854V13z"/></svg>'
);
Jodit.modules.Icon.set(
  'unorderedlist',
  '<svg viewBox="0 0 16 16"><path d="M5.75 2.5h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1 0-1.5Zm0 5h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1 0-1.5Zm0 5h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1 0-1.5ZM2 14a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm1-6a1 1 0 1 1-2 0 1 1 0 0 1 2 0ZM2 4a1 1 0 1 1 0-2 1 1 0 0 1 0 2Z"></path></svg>'
);
Jodit.modules.Icon.set(
  'orderedlist',
  '<svg viewBox="0 0 16 16"><path d="M5 3.25a.75.75 0 0 1 .75-.75h8.5a.75.75 0 0 1 0 1.5h-8.5A.75.75 0 0 1 5 3.25Zm0 5a.75.75 0 0 1 .75-.75h8.5a.75.75 0 0 1 0 1.5h-8.5A.75.75 0 0 1 5 8.25Zm0 5a.75.75 0 0 1 .75-.75h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1-.75-.75ZM.924 10.32a.5.5 0 0 1-.851-.525l.001-.001.001-.002.002-.004.007-.011c.097-.144.215-.273.348-.384.228-.19.588-.392 1.068-.392.468 0 .858.181 1.126.484.259.294.377.673.377 1.038 0 .987-.686 1.495-1.156 1.845l-.047.035c-.303.225-.522.4-.654.597h1.357a.5.5 0 0 1 0 1H.5a.5.5 0 0 1-.5-.5c0-1.005.692-1.52 1.167-1.875l.035-.025c.531-.396.8-.625.8-1.078a.57.57 0 0 0-.128-.376C1.806 10.068 1.695 10 1.5 10a.658.658 0 0 0-.429.163.835.835 0 0 0-.144.153ZM2.003 2.5V6h.503a.5.5 0 0 1 0 1H.5a.5.5 0 0 1 0-1h.503V3.308l-.28.14a.5.5 0 0 1-.446-.895l1.003-.5a.5.5 0 0 1 .723.447Z"></path></svg>'
);
Jodit.modules.Icon.set(
  'blockquote',
  '<svg viewBox="0 0 18 18"><rect height="3" width="3" x="4" y="5"></rect><rect height="3" width="3" x="11" y="5"></rect><path d="M7,8c0,4.031-3,5-3,5"></path><path d="M14,8c0,4.031-3,5-3,5"></path></svg>'
);
Jodit.modules.Icon.set(
  'code',
  '<svg viewBox="0 0 16 16"><path d="m11.28 3.22 4.25 4.25a.75.75 0 0 1 0 1.06l-4.25 4.25a.749.749 0 0 1-1.275-.326.749.749 0 0 1 .215-.734L13.94 8l-3.72-3.72a.749.749 0 0 1 .326-1.275.749.749 0 0 1 .734.215Zm-6.56 0a.751.751 0 0 1 1.042.018.751.751 0 0 1 .018 1.042L2.06 8l3.72 3.72a.749.749 0 0 1-.326 1.275.749.749 0 0 1-.734-.215L.47 8.53a.75.75 0 0 1 0-1.06Z"></path></svg>'
);

export const customJoditControls = {
  h1: {
    name: 'h1',
    icon: 'h1',
    tooltip: 'Heading 1',
    exec: function (editor) {
      editor.execCommand('formatBlock', false, 'h1');
    },
    isActive: (editor, control) => {
      const current = editor.s.current();
      return Boolean(current && Jodit.modules.Dom.closest(current, 'h1', editor.editor));
    }
  },
  h2: {
    name: 'h2',
    icon: 'h2',
    tooltip: 'Heading 2',
    exec: function (editor) {
      editor.execCommand('formatBlock', false, 'h2');
    },
    isActive: (editor, control) => {
      const current = editor.s.current();
      return Boolean(current && Jodit.modules.Dom.closest(current, 'h2', editor.editor));
    }
  },
  h3: {
    name: 'h3',
    icon: 'h3',
    tooltip: 'Heading 3',
    exec: function (editor) {
      editor.execCommand('formatBlock', false, 'h3');
    },
    isActive: (editor, control) => {
      const current = editor.s.current();
      return Boolean(current && Jodit.modules.Dom.closest(current, 'h3', editor.editor));
    }
  },
  h4: {
    name: 'h4',
    icon: 'h4',
    tooltip: 'Heading 4',
    exec: function (editor) {
      editor.execCommand('formatBlock', false, 'h4');
    },
    isActive: (editor, control) => {
      const current = editor.s.current();
      return Boolean(current && Jodit.modules.Dom.closest(current, 'h4', editor.editor));
    }
  },
  h5: {
    name: 'h5',
    icon: 'h5',
    tooltip: 'Heading 5',
    exec: function (editor) {
      editor.execCommand('formatBlock', false, 'h5');
    },
    isActive: (editor, control) => {
      const current = editor.s.current();
      return Boolean(current && Jodit.modules.Dom.closest(current, 'h5', editor.editor));
    }
  },
  h6: {
    name: 'h6',
    icon: 'h6',
    tooltip: 'Heading 6',
    exec: function (editor) {
      editor.execCommand('formatBlock', false, 'h6');
    },
    isActive: (editor, control) => {
      const current = editor.s.current();
      return Boolean(current && Jodit.modules.Dom.closest(current, 'h6', editor.editor));
    }
  },
  unorderedlist: {
    name: 'unorderedlist',
    icon: 'unorderedlist',
    tooltip: 'Insert unordered list',
    exec: function (editor) {
      editor.execCommand('formatBlock', false, 'ul');
    },
    isActive: (editor, control) => {
      const current = editor.s.current();
      return Boolean(current && Jodit.modules.Dom.closest(current, 'ul', editor.editor));
    }
  },
  orderedlist: {
    name: 'orderedlist',
    icon: 'orderedlist',
    tooltip: 'Insert ordered list',
    exec: function (editor) {
      editor.execCommand('formatBlock', false, 'ol');
    },
    isActive: (editor, control) => {
      const current = editor.s.current();
      return Boolean(current && Jodit.modules.Dom.closest(current, 'ol', editor.editor));
    }
  },
  code: {
    name: 'code',
    icon: 'code',
    tooltip: 'Insert Code Block',
    exec: function (editor) {
      editor.execCommand('formatBlock', false, 'code');
    },
    isActive: (editor, control) => {
      const current = editor.s.current();
      return Boolean(current && Jodit.modules.Dom.closest(current, 'code', editor.editor));
    }
  },
  blockquote: {
    name: 'blockquote',
    icon: 'blockquote',
    tooltip: 'Insert blockquote',
    exec: function (editor) {
      editor.execCommand('formatBlock', false, 'blockquote');
    },
    isActive: (editor, control) => {
      const current = editor.s.current();
      return Boolean(current && Jodit.modules.Dom.closest(current, 'blockquote', editor.editor));
    }
  }
};
