import { Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { NbThemeService } from '@nebular/theme';

import { Jodit } from 'jodit';

import cells from 'node_modules/jodit/esm/plugins/inline-popup/config/items/cells.js';
import a from 'node_modules/jodit/esm/plugins/inline-popup/config/items/a.js';
import img from 'node_modules/jodit/esm/plugins/inline-popup/config/items/img.js';

import { customJoditControls } from './jodit-customization';
import { MarkupFormats, htmlToMarkdown, markdownToHtml } from '../../utils/markup.utils';

@Component({
  selector: 'tock-wysiwyg-editor',
  templateUrl: './wysiwyg-editor.component.html',
  styleUrl: './wysiwyg-editor.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => WysiwygEditorComponent),
      multi: true
    }
  ]
})
export class WysiwygEditorComponent implements OnInit, OnChanges, ControlValueAccessor {
  destroy$: Subject<unknown> = new Subject();

  joditInstance: Jodit;

  isDarkTheme: boolean = false;

  resizeObserver: ResizeObserver;

  @Input() editionFormat!: MarkupFormats;
  @Input() placeholder: string = 'Enter text...';
  @Input() rows: number = 12;
  @Input() locale?: string;

  @ViewChild('editorTarget') editorTarget: ElementRef;

  constructor(private themeService: NbThemeService) {}

  get isPlaintext(): boolean {
    return this.editionFormat === MarkupFormats.PLAINTEXT;
  }
  get isMarkdown(): boolean {
    return this.editionFormat === MarkupFormats.MARKDOWN;
  }
  get isHtml(): boolean {
    return this.editionFormat === MarkupFormats.HTML;
  }

  onChange: any = () => {};
  onTouch: any = () => {};

  private val: string;

  get value() {
    return this.val;
  }

  set value(val) {
    this.val = val;
    this.onChange(val);
    this.onTouch(val);
  }

  writeValue(value: any) {
    this.val = value;

    setTimeout(() => {
      this.initEditor();
    });
  }

  registerOnChange(fn: any) {
    this.onChange = fn;
  }

  registerOnTouched(fn: any) {
    this.onTouch = fn;
  }

  ngOnInit(): void {
    this.themeService
      .onThemeChange()
      .pipe(takeUntil(this.destroy$))
      .subscribe((theme: any) => {
        if (theme.name === 'dark') {
          this.isDarkTheme = true;
          if (this.joditInstance) this.joditInstance.container?.classList.add('jodit_theme_dark');
        } else {
          this.isDarkTheme = false;
          if (this.joditInstance) this.joditInstance.container?.classList.remove('jodit_theme_dark');
        }
      });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.editionFormat.currentValue) {
      this.convertMarkupFormat(changes.editionFormat.previousValue, changes.editionFormat.currentValue);
    }
  }

  initEditor() {
    if (!this.editorTarget?.nativeElement) {
      // Jodit probably hasn't finished destroying itself yet
      setTimeout(() => this.initEditor());
      return;
    }

    let buttons;
    let buttonsXs;

    if (this.isHtml) {
      buttons =
        'eraser,|,bold,italic,underline,strikethrough,h1,h2,h3,h4,h5,h6,superscript,subscript,---,unorderedlist,orderedlist,font,fontsize,brush,blockquote,code,---,hr,table,link,symbols,image,---,fullsize';
      buttonsXs = 'bold,italic,h1,h2,|,unorderedlist,blockquote,|,link,image,---,dots,|,fullsize';
    }

    if (this.isMarkdown) {
      buttons =
        'eraser,|,bold,italic,underline,strikethrough,h1,h2,---,unorderedlist,orderedlist,blockquote,code,---,hr,table,link,symbols,image,---,fullsize';
      buttonsXs = 'bold,italic,h1,h2,|,unorderedlist,blockquote,|,link,image,---,dots,|,fullsize';
    }

    let cellsPopupItems = cells.filter((item) => {
      if (this.isMarkdown) {
        if (item === 'brushCell') return false;

        if (typeof item !== 'string') {
          if (['align', 'valign', 'splitv', 'merge'].includes(item.name)) return false;
        }
      }
      return true;
    });

    let aPopupItems = a.filter((item) => {
      if (item === 'file') return false;

      if (this.isMarkdown) {
        if (item === 'brush') return false;
      }
      return true;
    });

    let imgPopupItems = img.filter((item) => {
      if (this.isMarkdown) {
        if (typeof item !== 'string') {
          if (['left', 'valign'].includes(item.name)) return false;
        }
      }
      return true;
    });

    let disabledPlugins = 'about,powered-by-jodit';
    if (this.isMarkdown) {
      disabledPlugins += ',resizer';
    }

    let tableCreateAttributesStyle = 'border-collapse:collapse;';
    if (this.isHtml) {
      tableCreateAttributesStyle += 'width: 100%;';
    }

    this.joditInstance = Jodit.make(this.editorTarget.nativeElement, {
      language: 'en',
      placeholder: this.placeholder,

      theme: this.isDarkTheme ? 'dark' : 'default',

      toolbarButtonSize: 'xsmall',

      buttons: buttons,
      buttonsMD: buttons,
      buttonsSM: buttons,
      buttonsXS: buttonsXs,

      colorPickerDefaultTab: 'color',

      showCharsCounter: false,
      showWordsCounter: false,
      showXPathInStatusbar: false,
      spellcheck: false,

      disablePlugins: disabledPlugins,

      createAttributes: {
        table: {
          style: tableCreateAttributesStyle
        }
      },

      popup: {
        cells: Jodit.atom(cellsPopupItems),
        a: Jodit.atom(aPopupItems),
        img: Jodit.atom(imgPopupItems)
      },

      controls: customJoditControls
    });

    this.isInitingEditor = true;
    this.setEditorContent();

    this.observeResize();

    this.joditInstance.events.on('change', async (newHtml) => {
      if (!this.isInitingEditor) this.updateValueFromEditor(newHtml);
    });

    this.joditInstance.events.on('paste', async (newHtml) => {
      this.updateValueFromEditor(newHtml);
    });
  }

  isInitingEditor: boolean;

  async updateValueFromEditor(newHtml: string) {
    if (this.isHtml) {
      this.value = newHtml;
    }

    if (this.isMarkdown) {
      const markdown = await htmlToMarkdown(newHtml);
      if (this.value !== String(markdown)) this.value = String(markdown);
    }
  }

  async setEditorContent() {
    if (!this.joditInstance) return;

    const rawData = this.value;

    if (this.isMarkdown) {
      const htmlData = await markdownToHtml(rawData);
      this.joditInstance.value = String(htmlData);
    }

    if (this.isHtml) {
      this.joditInstance.value = rawData;
    }

    this.isInitingEditor = false;
  }

  async convertMarkupFormat(prev: MarkupFormats, next: MarkupFormats) {
    this.unobserveResize();
    this.joditInstance?.destruct();

    if (prev === MarkupFormats.MARKDOWN && next === MarkupFormats.HTML) {
      const html = await markdownToHtml(this.value);
      if (String(html) !== this.value) this.value = String(html);
    }

    if (prev === MarkupFormats.HTML && next === MarkupFormats.MARKDOWN) {
      const markdown = await htmlToMarkdown(this.value);
      if (String(markdown) !== this.value) this.value = String(markdown);
    }

    if (next !== MarkupFormats.PLAINTEXT) {
      this.initEditor();
    }
  }

  observeResize() {
    this.unobserveResize();
    if (!this.joditInstance?.container) return;

    this.resizeObserver = new ResizeObserver((entries) => {
      entries.forEach((entry) => {
        this.joditInstance?.events.fire('resize');
      });
    });

    this.resizeObserver.observe(this.joditInstance.container);
  }

  unobserveResize() {
    if (!this.joditInstance?.container) return;

    this.resizeObserver?.unobserve(this.joditInstance.container);
  }

  ngOnDestroy(): void {
    this.unobserveResize();
    this.joditInstance?.destruct();
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
