import { Component, ElementRef, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { NbDialogService, NbTabComponent, NbTagComponent, NbTagInputAddEvent } from '@nebular/theme';
import { Observable, Subject, of } from 'rxjs';
import { pairwise, startWith, take, takeUntil } from 'rxjs/operators';

import { StateService } from '../../../core-nlp/state.service';
import { PaginatedQuery } from '../../../model/commons';
import { Intent, SearchQuery, SentenceStatus } from '../../../model/nlp';
import { NlpService } from '../../../core-nlp/nlp.service';
import { ChoiceDialogComponent, SentencesGenerationComponent } from '../../../shared/components';
import { FaqDefinitionExtended } from '../faq-management.component';
import Quill, { QuillOptions } from 'quill';
import QuillTableBetter from 'quill-table-better';
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

export enum FaqTabs {
  INFO = 'info',
  QUESTION = 'question',
  ANSWER = 'answer'
}

enum AnswerExportFormats {
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

  constructor(private nbDialogService: NbDialogService, private nlp: NlpService, private readonly state: StateService) {}

  faqTabs: typeof FaqTabs = FaqTabs;
  isSubmitted: boolean = false;
  currentTab = FaqTabs.INFO;

  answerExportFormats = AnswerExportFormats;

  answerExportFormatsRadios = [
    { label: 'Plain text', value: AnswerExportFormats.PLAINTEXT },
    { label: 'Markdown', value: AnswerExportFormats.MARKDOWN },
    { label: 'Html', value: AnswerExportFormats.HTML }
  ];

  controlsMaxLength = {
    description: 500,
    answer: 5000
  };

  setCurrentTab(tab: NbTabComponent): void {
    this.currentTab = tab.tabTitle as FaqTabs;
    if (this.currentTab === FaqTabs.ANSWER) {
      setTimeout(() => {
        this.initQuill();
      });
    }
  }

  ngOnInit(): void {
    this.answerExportFormat.valueChanges
      .pipe(takeUntil(this.destroy$), startWith(this.answerExportFormat.value), pairwise())
      .subscribe(([prev, next]: [AnswerExportFormats, AnswerExportFormats]) => {
        this.convertAnswerFormat(prev, next);
      });
  }

  form = new FormGroup<FaqEditForm>({
    title: new FormControl(undefined, [Validators.required, Validators.minLength(5), Validators.maxLength(40)]),
    description: new FormControl('', Validators.maxLength(this.controlsMaxLength.description)),
    tags: new FormArray([]),
    utterances: new FormArray([], Validators.required),
    answer: new FormControl('', [
      Validators.required,
      Validators.maxLength(this.controlsMaxLength.answer),
      this.validateQuillContent.bind(this)
    ]),
    answerExportFormat: new FormControl(undefined)
  });

  getControlLengthIndicatorClass(controlName: string): string {
    return this.form.controls[controlName].value.length > this.controlsMaxLength[controlName] ? 'text-danger' : 'text-muted';
  }

  validateQuillContent(control: FormControl): ValidationErrors | null {
    if (!this.form?.value) return null;
    if (this.answerExportFormat.value === AnswerExportFormats.PLAINTEXT) return null;
    if (!this.quillInstance) return null;
    if (!this.quillInstance.getText().trim().length) {
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

  get answerIsText(): boolean {
    return this.answerExportFormat.value === AnswerExportFormats.PLAINTEXT;
  }
  get answerIsMarkdown(): boolean {
    return this.answerExportFormat.value === AnswerExportFormats.MARKDOWN;
  }
  get answerIsHtml(): boolean {
    return this.answerExportFormat.value === AnswerExportFormats.HTML;
  }

  tagsAutocompleteValues: Observable<any[]>;

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
      setTimeout(() => {
        this.initQuill();
      });
    }
  }

  async convertAnswerFormat(prev: AnswerExportFormats, next: AnswerExportFormats) {
    if (prev === AnswerExportFormats.MARKDOWN && next === AnswerExportFormats.HTML) {
      const html = await this.markdownToHtml(this.answer.value);
      this.answer.patchValue(String(html));
      this.answer.markAsDirty();

      // update editor
      const delta = this.quillInstance.clipboard.convert({ html: String(html) });
      this.quillInstance.setContents(undefined, 'silent');
      this.quillInstance.updateContents(delta, 'silent');
    }

    if (prev === AnswerExportFormats.HTML && next === AnswerExportFormats.MARKDOWN) {
      const markdown = await this.htmlToMarkdown(this.answer.value);
      this.answer.patchValue(String(markdown));
      this.answer.markAsDirty();

      // update editor
      const previewHtml = await this.markdownToHtml(this.answer.value);
      const delta = this.quillInstance.clipboard.convert({ html: String(previewHtml) });
      this.quillInstance.setContents(undefined, 'silent');
      this.quillInstance.updateContents(delta, 'silent');
    }

    if (prev === AnswerExportFormats.PLAINTEXT) {
      setTimeout(() => {
        this.initQuill();
      });
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
      console.log(tokens);

      return containsNonTextOrHtmlTokens(tokens);
    }

    if (containsHTML(data)) return AnswerExportFormats.HTML;

    if (containsMARKDOWN(data)) return AnswerExportFormats.MARKDOWN;

    return AnswerExportFormats.PLAINTEXT;
  }

  quillInstance: Quill;

  initQuill() {
    if (!this.answerEditorTarget?.nativeElement) return;

    Quill.register(
      {
        'modules/table-better': QuillTableBetter
      },
      true
    );

    // const toolbarOptions = [[{ header: 1 }, { header: 2 }], ['bold', 'italic', 'underline', 'strike'], ['table-better']];

    const options: QuillOptions = {
      // debug: 'info',
      theme: 'snow',
      modules: {
        table: false,
        toolbar: {
          container: '#toolbar-container'
        },
        'table-better': {
          language: 'en_US',
          menus: ['column', 'row', 'merge', 'table', 'cell', 'wrap', 'delete'],
          toolbarTable: true
        },
        keyboard: {
          bindings: QuillTableBetter.keyboardBindings
        }
      },
      placeholder: 'Enter faq answer...'
    };

    this.quillInstance = new Quill(this.answerEditorTarget.nativeElement, options);

    this.setQuillContent();

    this.quillInstance.on('text-change', async (delta, oldDelta, source) => {
      if (this.answerExportFormat.value === AnswerExportFormats.HTML) {
        this.answer.patchValue(this.quillInstance.getSemanticHTML());
      }

      if (this.answerExportFormat.value === AnswerExportFormats.MARKDOWN) {
        const markdown = await this.htmlToMarkdown(this.quillInstance.getSemanticHTML());
        this.answer.patchValue(String(markdown));
      }

      this.answer.markAsDirty();
    });
  }

  async setQuillContent() {
    const rawData = this.answer.value;

    if (this.answerExportFormat.value === AnswerExportFormats.MARKDOWN) {
      const htmlData = await this.markdownToHtml(rawData);
      const delta = this.quillInstance.clipboard.convert({ html: String(htmlData) });
      this.quillInstance.setContents(undefined, 'silent');
      this.quillInstance.updateContents(delta, 'silent');
    }
    if (this.answerExportFormat.value === AnswerExportFormats.HTML) {
      const delta = this.quillInstance.clipboard.convert({ html: rawData });
      this.quillInstance.setContents(undefined, 'silent');
      this.quillInstance.updateContents(delta, 'silent');
    }
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
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
