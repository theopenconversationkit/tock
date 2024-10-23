import { Component, ElementRef, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { NbDialogService, NbTabComponent, NbTagComponent, NbTagInputAddEvent, NbThemeService } from '@nebular/theme';
import { Observable, Subject, of } from 'rxjs';
import { pairwise, startWith, take, takeUntil } from 'rxjs/operators';

import { StateService } from '../../../core-nlp/state.service';
import { PaginatedQuery } from '../../../model/commons';
import { Intent, SearchQuery, SentenceStatus } from '../../../model/nlp';
import { NlpService } from '../../../core-nlp/nlp.service';
import { ChoiceDialogComponent, SentencesGenerationComponent } from '../../../shared/components';
import { FaqDefinitionExtended } from '../faq-management.component';
import { lexer } from 'marked';
import rehypeStringify from 'rehype-stringify';
import remarkGfm from 'remark-gfm';
import remarkParse from 'remark-parse';
import remarkRehype from 'remark-rehype';
import { unified } from 'unified';
import rehypeRaw from 'rehype-raw';
import rehypeSanitize from 'rehype-sanitize';
import rehypeRemark from 'rehype-remark';
import rehypeParse from 'rehype-parse';
import remarkStringify from 'remark-stringify';
import remarkGemoji from 'remark-gemoji';
import { VFile } from 'rehype-raw/lib';
import rehypeFormat from 'rehype-format';
import { Jodit } from 'jodit';
import 'node_modules/jodit/esm/plugins/fullsize/fullsize.js';
import 'node_modules/jodit/esm/plugins/clean-html/clean-html.js';
import 'node_modules/jodit/esm/plugins/hr/hr.js';
import 'node_modules/jodit/esm/plugins/symbols/symbols.js';
import 'node_modules/jodit/esm/plugins/table-keyboard-navigation/table-keyboard-navigation.js';
import 'node_modules/jodit/esm/plugins/select/select.js';
import 'node_modules/jodit/esm/plugins/select-cells/select-cells.js';
import 'node_modules/jodit/esm/plugins/resize-handler/resize-handler.js';
import 'node_modules/jodit/esm/plugins/add-new-line/add-new-line.js';
import 'node_modules/jodit/esm/plugins/backspace/backspace.js';
import 'node_modules/jodit/esm/plugins/focus/focus.js';
import 'node_modules/jodit/esm/plugins/resizer/resizer.js';
import 'node_modules/jodit/esm/plugins/image-properties/image-properties.js';
import 'node_modules/jodit/esm/plugins/mobile/mobile.js';
import 'node_modules/jodit/esm/plugins/spellcheck/spellcheck.js';
import 'node_modules/jodit/esm/plugins/justify/justify.js';
import cells from 'node_modules/jodit/esm/plugins/inline-popup/config/items/cells.js';
import a from 'node_modules/jodit/esm/plugins/inline-popup/config/items/a.js';
import img from 'node_modules/jodit/esm/plugins/inline-popup/config/items/img.js';

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

export enum FaqTabs {
  INFO = 'info',
  QUESTION = 'question',
  ANSWER = 'answer'
}

export enum AnswerExportFormats {
  PLAINTEXT = 'PLAINTEXT',
  MARKDOWN = 'MARKDOWN',
  HTML = 'HTML'
}

interface FaqEditForm {
  title: FormControl<string>;
  description: FormControl<string>;
  tags: FormArray<FormControl<string>>;
  utterances: FormArray<FormControl<string>>;
  answer: FormControl<string>;
  answerExportFormat: FormControl<AnswerExportFormats>;
}

@Component({
  selector: 'tock-faq-management-edit',
  templateUrl: './faq-management-edit.component.html',
  styleUrls: ['./faq-management-edit.component.scss']
})
export class FaqManagementEditComponent implements OnInit, OnChanges {
  destroy$: Subject<unknown> = new Subject();

  @Input() loading: boolean;
  @Input() faq?: FaqDefinitionExtended;
  @Input() tagsCache?: string[];
  @Input() expanded?: boolean;

  @Output() onClose = new EventEmitter<boolean>();
  @Output() onExpandSidePanel = new EventEmitter<boolean>();
  @Output() onSave = new EventEmitter();

  @ViewChild('tagInput') tagInput: ElementRef;
  @ViewChild('addUtteranceInput') addUtteranceInput: ElementRef;
  @ViewChild('utterancesListWrapper') utterancesListWrapper: ElementRef;
  @ViewChild('answerEditorTarget') answerEditorTarget: ElementRef;

  constructor(
    private nbDialogService: NbDialogService,
    private nlp: NlpService,
    private readonly state: StateService,
    private themeService: NbThemeService
  ) {}

  faqTabs: typeof FaqTabs = FaqTabs;
  isSubmitted: boolean = false;
  currentTab = FaqTabs.INFO;

  answerExportFormatsRadios = [
    { label: 'Plain text', value: AnswerExportFormats.PLAINTEXT },
    { label: 'Html', value: AnswerExportFormats.HTML },
    { label: 'Markdown', value: AnswerExportFormats.MARKDOWN }
  ];

  controlsMaxLength = {
    description: 500,
    answer: 5000
  };

  isDarkTheme: boolean = false;

  form = new FormGroup<FaqEditForm>({
    title: new FormControl(undefined, [Validators.required, Validators.minLength(5), Validators.maxLength(40)]),
    description: new FormControl('', Validators.maxLength(this.controlsMaxLength.description)),
    tags: new FormArray([]),
    utterances: new FormArray([], Validators.required),
    answer: new FormControl('', [Validators.required, Validators.maxLength(this.controlsMaxLength.answer)]),
    answerExportFormat: new FormControl(undefined)
  });

  getControlLengthIndicatorClass(controlName: string): string {
    return this.form.controls[controlName].value.length > this.controlsMaxLength[controlName] ? 'text-danger' : 'text-muted';
  }

  validateEditorContent(control: FormControl): ValidationErrors | null {
    if (!this.form?.value) return null;
    if (this.answerIsPlaintext) return null;

    if (!this.joditInstance?.element) return null;

    if (!this.joditInstance?.value.trim().length) {
      return { minlength: { requiredLength: 1 } };
    }
    return null;
  }

  get answer(): FormControl {
    return this.form.get('answer') as FormControl;
  }

  get answerExportFormat(): FormControl {
    return this.form.get('answerExportFormat') as FormControl;
  }

  get description(): FormControl {
    return this.form.get('description') as FormControl;
  }

  get title(): FormControl {
    return this.form.get('title') as FormControl;
  }

  get tags(): FormArray {
    return this.form.get('tags') as FormArray;
  }

  get utterances(): FormArray {
    return this.form.get('utterances') as FormArray;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  get answerIsPlaintext(): boolean {
    return this.answerExportFormat.value === AnswerExportFormats.PLAINTEXT;
  }
  get answerIsMarkdown(): boolean {
    return this.answerExportFormat.value === AnswerExportFormats.MARKDOWN;
  }
  get answerIsHtml(): boolean {
    return this.answerExportFormat.value === AnswerExportFormats.HTML;
  }

  tagsAutocompleteValues: Observable<any[]>;

  ngOnInit(): void {
    this.themeService
      .onThemeChange()
      .pipe(takeUntil(this.destroy$))
      .subscribe((theme: any) => {
        if (theme.name === 'dark') {
          this.isDarkTheme = true;
          if (this.joditInstance) this.joditInstance.container.classList.add('jodit_theme_dark');
        } else {
          this.isDarkTheme = false;
          if (this.joditInstance) this.joditInstance.container.classList.remove('jodit_theme_dark');
        }
      });

    this.answerExportFormat.valueChanges
      .pipe(takeUntil(this.destroy$), startWith(this.answerExportFormat.value), pairwise())
      .subscribe(([prev, next]: [AnswerExportFormats, AnswerExportFormats]) => {
        this.convertAnswerFormat(prev, next);
      });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.faq?.currentValue) {
      const faq: FaqDefinitionExtended = changes.faq.currentValue;

      this.form.reset();
      this.tags.clear();
      this.utterances.clear();
      this.resetAlerts();
      this.isSubmitted = false;

      if (faq) {
        this.form.patchValue(faq);

        if (faq.tags?.length) {
          faq.tags.forEach((tag) => {
            this.tags.push(new FormControl(tag));
          });
        }

        faq.utterances.forEach((utterance) => {
          this.utterances.push(new FormControl(utterance));
        });

        if (faq._initQuestion) {
          this.form.markAsDirty();
          this.form.markAsTouched();

          this.setCurrentTab({ tabTitle: FaqTabs.QUESTION } as NbTabComponent);

          setTimeout(() => {
            this.addUtterance(faq._initQuestion);
            delete faq._initQuestion;
          });
        }

        this.detectAnswerFormat();
      }

      if (!faq.id && !faq._initQuestion) {
        this.setCurrentTab({ tabTitle: FaqTabs.INFO } as NbTabComponent);
      }
    }

    this.tagsAutocompleteValues = of(this.tagsCache);

    if (
      this.currentTab === FaqTabs.ANSWER &&
      [AnswerExportFormats.HTML, AnswerExportFormats.MARKDOWN].includes(this.answerExportFormat.value)
    ) {
      this.joditInstance?.destruct();

      setTimeout(() => {
        this.initEditor();
      });
    }

    this.setAnswerValidators();
  }

  setAnswerValidators() {
    if ([AnswerExportFormats.HTML, AnswerExportFormats.MARKDOWN].includes(this.answerExportFormat.value)) {
      this.answer.setValidators([
        Validators.required,
        Validators.maxLength(this.controlsMaxLength.answer),
        this.validateEditorContent.bind(this)
      ]);
    } else {
      this.answer.clearValidators();
      this.answer.setValidators([Validators.required, Validators.maxLength(this.controlsMaxLength.answer)]);
    }

    this.answer.updateValueAndValidity();
  }

  setCurrentTab(tab: NbTabComponent): void {
    this.currentTab = tab.tabTitle as FaqTabs;
    if (this.currentTab === FaqTabs.ANSWER) {
      setTimeout(() => {
        this.initEditor();
      });
    }
  }

  async convertAnswerFormat(prev: AnswerExportFormats, next: AnswerExportFormats) {
    this.joditInstance?.destruct();

    if (prev === AnswerExportFormats.MARKDOWN && next === AnswerExportFormats.HTML) {
      const html = await this.markdownToHtml(this.answer.value);
      this.answer.patchValue(String(html));
      this.answer.markAsDirty();
    }

    if (prev === AnswerExportFormats.HTML && next === AnswerExportFormats.MARKDOWN) {
      const markdown = await this.htmlToMarkdown(this.answer.value);
      this.answer.patchValue(String(markdown));
      this.answer.markAsDirty();
    }

    if (next !== AnswerExportFormats.PLAINTEXT) {
      setTimeout(() => {
        this.initEditor();
      });
    }

    this.setAnswerValidators();
  }

  joditInstance: Jodit;

  initEditor() {
    if (!this.answerEditorTarget?.nativeElement) return;
    let buttons;
    let buttonsXs;

    if (this.answerIsHtml) {
      buttons =
        'eraser,|,bold,italic,underline,strikethrough,h1,h2,h3,h4,h5,h6,superscript,subscript,---,unorderedlist,orderedlist,font,fontsize,brush,blockquote,code,---,hr,table,link,symbols,image,---,fullsize';
      buttonsXs = 'bold,italic,h1,h2,|,unorderedlist,blockquote,|,link,image,---,dots,|,fullsize';
    }

    if (this.answerIsMarkdown) {
      buttons =
        'eraser,|,bold,italic,underline,strikethrough,h1,h2,---,unorderedlist,orderedlist,blockquote,code,---,hr,table,link,symbols,image,---,fullsize';
      buttonsXs = 'bold,italic,h1,h2,|,unorderedlist,blockquote,|,link,image,---,dots,|,fullsize';
    }

    let cellsPopupItems = cells.filter((item) => {
      if (this.answerIsMarkdown) {
        if (item === 'brushCell') return false;

        if (typeof item !== 'string') {
          if (['align', 'valign', 'splitv', 'merge'].includes(item.name)) return false;
        }
      }
      return true;
    });

    let aPopupItems = a.filter((item) => {
      if (item === 'file') return false;

      if (this.answerIsMarkdown) {
        if (item === 'brush') return false;
      }
      return true;
    });

    let imgPopupItems = img.filter((item) => {
      if (this.answerIsMarkdown) {
        if (typeof item !== 'string') {
          if (['left', 'valign'].includes(item.name)) return false;
        }
      }
      return true;
    });

    let disabledPlugins = 'about,powered-by-jodit';
    if (this.answerIsMarkdown) {
      disabledPlugins += ',resizer';
    }

    this.joditInstance = Jodit.make(this.answerEditorTarget.nativeElement, {
      language: 'en',
      toolbarButtonSize: 'xsmall',

      showCharsCounter: false,
      showWordsCounter: false,
      showXPathInStatusbar: false,
      spellcheck: false,

      disablePlugins: disabledPlugins,

      theme: this.isDarkTheme ? 'dark' : 'default',

      buttons: buttons,
      buttonsMD: buttons,
      buttonsSM: buttons,
      buttonsXS: buttonsXs,

      colorPickerDefaultTab: 'color',

      popup: {
        cells: Jodit.atom(cellsPopupItems),
        a: Jodit.atom(aPopupItems),
        img: Jodit.atom(imgPopupItems)
      },

      controls: {
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
      }
    });

    this.setEditorContent();

    setTimeout(() => {
      this.joditInstance.events.on('change', async (newHtml) => {
        this.updateAnswerFromEditor(newHtml);
      });

      this.joditInstance.events.on('paste', async (newHtml) => {
        this.updateAnswerFromEditor(newHtml);
      });
    });
  }

  async updateAnswerFromEditor(newHtml: string) {
    if (this.answerIsHtml) {
      this.answer.patchValue(newHtml);
    }

    if (this.answerIsMarkdown) {
      const markdown = await this.htmlToMarkdown(newHtml);
      this.answer.patchValue(String(markdown));
    }

    this.answer.markAsDirty();
  }

  async setEditorContent() {
    const rawData = this.answer.value;

    if (this.answerIsMarkdown) {
      const htmlData = await this.markdownToHtml(rawData);
      this.joditInstance.value = String(htmlData);
    }
    if (this.answerIsHtml) {
      this.joditInstance.value = rawData;
    }
  }

  detectAnswerFormat() {
    let rawData = this.answer.value;
    const guessedFormat = this.guessAnswerFormat(rawData);
    this.answerExportFormat.setValue(guessedFormat);
  }

  guessAnswerFormat(data: string) {
    function containsHTML(str) {
      var a = document.createElement('div');
      a.innerHTML = str;

      for (var c = a.childNodes, i = c.length; i--; ) {
        if (c[i].nodeType == 1) return true;
      }

      return false;
    }

    function containsMARKDOWN(text: string): boolean {
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

      const tokens = lexer(text);

      return containsNonTextOrHtmlTokens(tokens);
    }

    if (containsHTML(data)) return AnswerExportFormats.HTML;

    if (containsMARKDOWN(data)) return AnswerExportFormats.MARKDOWN;

    return AnswerExportFormats.PLAINTEXT;
  }

  async markdownToHtml(rawData: string): Promise<VFile> {
    const processor = unified()
      .use(remarkParse)
      .use(remarkGfm)
      .use(remarkGemoji)
      .use(remarkRehype, { allowDangerousHtml: true })
      .use(rehypeFormat)
      .use(rehypeRaw)
      .use(rehypeSanitize)
      .use(rehypeStringify);

    return processor.process(rawData);
  }

  async htmlToMarkdown(rawData: string) {
    const processor = await unified().use(rehypeParse).use(rehypeFormat).use(rehypeRemark).use(remarkGfm).use(remarkStringify);
    return processor.process(rawData);
  }

  updateTagsAutocompleteValues(event: any) {
    this.tagsAutocompleteValues = of(this.tagsCache.filter((tag) => tag.toLowerCase().includes(event.target.value.toLowerCase())));
  }

  tagSelected(value: string) {
    this.onTagAdd({ value, input: this.tagInput });
  }

  onTagAdd({ value, input }: NbTagInputAddEvent): void {
    let deduplicatedSpaces = value.replace(/\s\s+/g, ' ').toLowerCase().trim();
    if (deduplicatedSpaces && !this.tags.value.find((v: string) => v.toUpperCase() === deduplicatedSpaces.toUpperCase())) {
      this.tags.push(new FormControl(deduplicatedSpaces));
      this.form.markAsDirty();
      this.form.markAsTouched();
    }

    input.nativeElement.value = '';
  }

  onTagRemove(tag: NbTagComponent): void {
    const tagToRemove = this.tags.value.findIndex((t: string) => t === tag.text);

    if (tagToRemove !== -1) {
      this.tags.removeAt(tagToRemove);
      this.form.markAsDirty();
      this.form.markAsTouched();
    }
  }

  normalizeString(str: string): string {
    /*
      Remove diacrtitics
      Trim
      Remove western punctuations
      Deduplicate spaces
    */
    return str
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .replace(/[.,\/#!$%\^&\*;:{}=\-_`~()?]/g, '')
      .replace(/\s\s+/g, ' ');
  }

  utterancesInclude(str: string): AbstractControl | undefined {
    return this.utterances.controls.find((u) => {
      return this.normalizeString(u.value) == this.normalizeString(str);
    });
  }

  existingUterranceInOtherintent: string;
  lookingForSameUterranceInOtherInent: boolean = false;

  resetAlerts() {
    this.existingUterranceInOtherintent = undefined;
    this.intentNameExistInApp = undefined;
  }

  addUtterance(utt?: string) {
    this.resetAlerts();

    let utterance = utt || this.addUtteranceInput.nativeElement.value.trim();
    if (utterance) {
      if (!this.utterancesInclude(utterance)) {
        this.lookingForSameUterranceInOtherInent = true;
        const searchQuery: SearchQuery = this.createSearchIntentsQuery({
          searchString: utterance
        });

        this.nlp
          .searchSentences(searchQuery)
          .pipe(take(1))
          .subscribe({
            next: (res) => {
              let existingIntentId;
              res.rows.forEach((sentence) => {
                if (this.normalizeString(sentence.text) == this.normalizeString(utterance)) {
                  if (
                    [SentenceStatus.model, SentenceStatus.validated].includes(sentence.status) &&
                    sentence.classification.intentId != Intent.unknown &&
                    (!this.faq.intentId || sentence.classification.intentId != this.faq.intentId)
                  ) {
                    existingIntentId = sentence.classification.intentId;
                  }
                }
              });
              if (existingIntentId) {
                let intent = this.state.findIntentById(existingIntentId);
                this.existingUterranceInOtherintent = intent?.label || intent?.name || '';
              } else {
                this.utterances.push(new FormControl(utterance));
                this.form.markAsDirty();
                setTimeout(() => {
                  this.addUtteranceInput?.nativeElement.focus();
                  this.utterancesListWrapper.nativeElement.scrollTop = this.utterancesListWrapper.nativeElement.scrollHeight;
                });
              }
              this.lookingForSameUterranceInOtherInent = false;
            },
            error: () => {
              this.lookingForSameUterranceInOtherInent = false;
            }
          });
      }
      this.addUtteranceInput.nativeElement.value = '';
    }
  }

  createSearchIntentsQuery(params: { searchString?: string; intentId?: string }): SearchQuery {
    const cursor: number = 0;
    const paginatedQuery: PaginatedQuery = this.state.createPaginatedQuery(cursor);
    return new SearchQuery(
      paginatedQuery.namespace,
      paginatedQuery.applicationName,
      paginatedQuery.language,
      paginatedQuery.start,
      paginatedQuery.size,
      paginatedQuery.searchMark,
      params.searchString || null,
      params.intentId || null
    );
  }

  editedUtterance: AbstractControl<any, any>;
  editedUtteranceValue: string;
  editUtterance(utterance: string): void {
    const ctrl = this.utterances.controls.find((u) => u.value == utterance);
    this.editedUtterance = ctrl;
    this.editedUtteranceValue = ctrl.value;
  }

  validateEditUtterance(utterance: AbstractControl<any, any>): void {
    utterance.setValue(this.editedUtteranceValue);
    this.cancelEditUtterance();
    this.form.markAsDirty();
    this.form.markAsTouched();
  }

  cancelEditUtterance(): void {
    this.editedUtterance = undefined;
  }

  removeUtterance(utterance: string): void {
    const index = this.utterances.controls.findIndex((u) => u.value == utterance);
    this.utterances.removeAt(index);
    this.form.markAsDirty();
    this.form.markAsTouched();
  }

  close(): Observable<any> {
    const action = 'yes';
    if (this.form.dirty) {
      const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
        context: {
          title: `Cancel ${this.faq?.id ? 'edit' : 'create'} faq`,
          subtitle: 'Are you sure you want to cancel ? Changes will not be saved.',
          actions: [
            { actionName: 'no', buttonStatus: 'basic', ghost: true },
            { actionName: action, buttonStatus: 'danger' }
          ]
        }
      });
      dialogRef.onClose.subscribe((result) => {
        if (result === action) {
          this.onClose.emit(true);
        }
      });
      return dialogRef.onClose;
    } else {
      this.onClose.emit(true);
      return of(action);
    }
  }

  getFormatedIntentName(value: string): string {
    return value
      .replace(/[^A-Za-z_-]*/g, '')
      .toLowerCase()
      .trim();
  }

  intentNameExistInApp: boolean;

  checkIntentNameAndSave(): void {
    this.isSubmitted = true;
    this.resetAlerts();

    if (this.canSave) {
      let faqData = {
        ...this.faq,
        ...this.form.value
      };

      if (!this.faq.id) {
        faqData.intentName = this.getFormatedIntentName(this.title.value);

        let existsInApp = StateService.intentExistsInApp(this.state.currentApplication, faqData.intentName);
        if (existsInApp) {
          this.intentNameExistInApp = true;
          return;
        }

        let existsInOtherApp = this.state.intentExistsInOtherApplication(faqData.intentName);

        if (existsInOtherApp) {
          const shareAction = 'Share the intent';
          const createNewAction = 'Create a new intent';
          const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
            context: {
              title: `This intent is already used in another application`,
              subtitle: 'Do you want to share the intent between the two applications or create a new one ?',
              actions: [{ actionName: shareAction }, { actionName: createNewAction }]
            }
          });
          dialogRef.onClose.subscribe((result) => {
            if (result) {
              if (result === createNewAction.toLocaleLowerCase()) {
                faqData.intentName = this.generateIntentName(faqData);
              }
              this.save(faqData);
            }
          });
          return;
        }
      }

      this.save(faqData);
    }
  }

  private generateIntentName(fq: FaqDefinitionExtended): string {
    let candidate = fq.intentName;
    let count = 1;
    const candidateBase = candidate;
    while (this.state.intentExists(candidate)) {
      candidate = candidateBase + count++;
    }
    return candidate;
  }

  save(faqDFata): void {
    this.onSave.emit(faqDFata);
    if (!this.faq.id) this.onClose.emit(true);
  }

  generateSentences(): void {
    const dialogRef = this.nbDialogService.open(SentencesGenerationComponent, {
      context: {
        sentences: this.utterances.value
      }
    });

    dialogRef.componentRef.instance.onValidateSelection.subscribe((generatedSentences: string[]) => {
      generatedSentences.forEach((generatedSentence: string) => this.addUtterance(generatedSentence));
      dialogRef.close();
    });
  }

  expandSidePanel() {
    this.onExpandSidePanel.emit(true);
  }

  ngOnDestroy(): void {
    this.joditInstance?.destruct();
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
