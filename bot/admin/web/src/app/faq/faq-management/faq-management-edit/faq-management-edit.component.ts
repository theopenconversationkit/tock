import { Component, ElementRef, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { NbDialogService, NbPopoverDirective, NbTabComponent, NbTagComponent, NbTagInputAddEvent } from '@nebular/theme';
import { Observable, Subject, forkJoin, of } from 'rxjs';
import { take } from 'rxjs/operators';
import { StateService } from '../../../core-nlp/state.service';
import { PaginatedQuery } from '../../../model/commons';
import { Intent, SearchQuery, SentenceStatus } from '../../../model/nlp';
import { NlpService } from '../../../core-nlp/nlp.service';
import { ChoiceDialogComponent, SentencesGenerationComponent } from '../../../shared/components';
import { FaqDefinitionExtended } from '../faq-management.component';
import { MarkupFormats, detectMarkupFormat, htmlToMarkdown, htmlToPlainText } from '../../../shared/utils/markup.utils';
import { CreateI18nLabelRequest, I18nLocalizedLabel } from '../../../bot/model/i18n';
import { ConnectorType, ConnectorTypeConfiguration, UserInterfaceType } from '../../../core/model/configuration';
import { BotSharedService } from '../../../shared/bot-shared.service';
import { Connectors, deepCopy, getConnectorLabel, normalize } from '../../../shared/utils';
import { RestService } from '../../../core-nlp/rest/rest.service';
import { BotService } from '../../../bot/bot-service';
import { KeyValue } from '@angular/common';
import { ExtractFormControlTyping, GenericObject } from '../../../shared/utils/typescript.utils';
import { BotConfigurationService } from '../../../core/bot-configuration.service';

export enum FaqTabs {
  INFO = 'info',
  QUESTION = 'question',
  ANSWER = 'answer'
}

interface AnswerCombinationSelector {
  locale: string;
  connector: string;
  interface: UserInterfaceType;
}

interface I18nEditForm {
  locale: FormControl<string>;
  interfaceType: FormControl<UserInterfaceType>;
  connectorId: FormControl<string>;
  label: FormControl<string>;
  answerExportFormat: FormControl<MarkupFormats>;
}

type i18nValue = ExtractFormControlTyping<I18nEditForm>;

interface FaqEditForm {
  title: FormControl<string>;
  description: FormControl<string>;
  tags: FormArray<FormControl<string>>;
  utterances: FormArray<FormControl<string>>;
  answers: FormArray<FormGroup<I18nEditForm>>;
}

@Component({
  selector: 'tock-faq-management-edit',
  templateUrl: './faq-management-edit.component.html',
  styleUrls: ['./faq-management-edit.component.scss']
})
export class FaqManagementEditComponent implements OnChanges {
  destroy$: Subject<unknown> = new Subject();

  faqTabs: typeof FaqTabs = FaqTabs;
  currentTab = FaqTabs.INFO;
  isSubmitted: boolean = false;

  controlsMaxLength = {
    description: 500,
    answer: 5000
  };

  getConnectorLabel = getConnectorLabel;
  connectorsList = Connectors;

  connectorTypes: ConnectorTypeConfiguration[] = [];

  supportedConnectors: ConnectorType[];

  userInterfaceType = UserInterfaceType;

  answerExportFormatsRadios = [
    { label: 'Plain text', value: MarkupFormats.PLAINTEXT },
    { label: 'Html', value: MarkupFormats.HTML }
    // { label: 'Markdown', value: MarkupFormats.MARKDOWN }
  ];

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
  @ViewChild(NbPopoverDirective) answerCombinationSelectorPopoverRef: NbPopoverDirective;

  constructor(
    private nbDialogService: NbDialogService,
    private nlp: NlpService,
    private state: StateService,
    public botSharedService: BotSharedService,
    private botService: BotService,
    private botConfiguration: BotConfigurationService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.connectorTypes.length || !this.supportedConnectors) {
      const loaders = [this.botSharedService.getConnectorTypes().pipe(take(1)), this.botConfiguration.supportedConnectors.pipe(take(1))];

      forkJoin(loaders).subscribe(([connectorTypes, supportedConnectors]: [ConnectorTypeConfiguration[], ConnectorType[]]) => {
        // We don't want the Test connector in our list
        this.connectorTypes = connectorTypes.filter((conn) => !conn.connectorType.isRest());

        this.supportedConnectors = supportedConnectors;

        this.ngOnChanges(changes);
      });

      // we need the connectorTypes and supportedConnectors list before going further
      return;
    }

    if (changes.faq?.currentValue) {
      const faq: FaqDefinitionExtended = changes.faq.currentValue;

      this.form.reset();
      this.answers.clear();
      this.selectedAnswerI18nValue = undefined;

      this.tags.clear();
      this.utterances.clear();
      this.resetAlerts();
      this.isSubmitted = false;

      if (faq) {
        this.form.patchValue(faq);

        this.initFormAnswers(faq);

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

          this.addUtterance(faq._initQuestion);
          delete faq._initQuestion;
        }
      }

      if (!faq.id) {
        if (!faq._initQuestion) {
          this.setCurrentTab({ tabTitle: FaqTabs.INFO } as NbTabComponent);
        }
      }
    }

    if (changes.faq) {
      this.tagsAutocompleteValues = of(this.tagsCache);

      this.initSelectedAnswerI18nValue();
    }
  }

  setCurrentTab(tab: NbTabComponent): void {
    this.currentTab = tab.tabTitle as FaqTabs;
  }

  form = new FormGroup<FaqEditForm>({
    title: new FormControl(undefined, [Validators.required, Validators.minLength(5), Validators.maxLength(40)]),
    description: new FormControl('', Validators.maxLength(this.controlsMaxLength.description)),
    tags: new FormArray([]),
    utterances: new FormArray([], Validators.required),
    answers: new FormArray([], Validators.required)
  });

  get answers(): FormArray {
    return this.form.get('answers') as FormArray;
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

  tagsAutocompleteValues: Observable<any[]>;

  initFormAnswers(faq: FaqDefinitionExtended): void {
    if (!faq.answer) {
      // we're creating a new Faq; we should create a default answer for the current locale and its label is required

      // TODO : check if we have a web connector in the supportedConnectors, otherwise we should pick one supportedConnector and define an interfaceType accordingly

      const label = faq._initAnswer ? faq._initAnswer : '';

      delete faq._initAnswer;

      this.answers.push(
        new FormGroup({
          locale: new FormControl(this.state.currentLocale),
          interfaceType: new FormControl(UserInterfaceType.textChat),
          connectorId: new FormControl(),
          label: new FormControl(label, [Validators.required, this.validateAnswerMarkupContent.bind(this)]),
          answerExportFormat: new FormControl(MarkupFormats.PLAINTEXT)
        })
      );
    } else {
      faq.answer?.i18n.forEach((i18nLabel: I18nLocalizedLabel) => {
        const guessedFormat = !i18nLabel.connectorId
          ? detectMarkupFormat(i18nLabel.label, { checkForHtml: true, checkForMarkdown: false })
          : MarkupFormats.PLAINTEXT;

        this.answers.push(
          new FormGroup({
            locale: new FormControl(i18nLabel.locale),
            interfaceType: new FormControl(i18nLabel.interfaceType),
            connectorId: new FormControl(i18nLabel.connectorId),
            // We make the label mandatory if a string is already defined. We don't do this systematically, as we know that the database contains a large number of empty and unwanted I18nLocalizedLabels.
            label: new FormControl(
              i18nLabel.label,
              i18nLabel.label?.trim().length ? [Validators.required, this.validateAnswerMarkupContent.bind(this)] : []
            ),
            answerExportFormat: new FormControl(guessedFormat)
          })
        );
      });
    }
  }

  getControlLengthIndicatorClass(controlName: string): string {
    return this.form.controls[controlName].value.length > this.controlsMaxLength[controlName] ? 'text-danger' : 'text-muted';
  }

  validateAnswerMarkupContent(control: FormControl): ValidationErrors | null {
    if (!this.form?.value) return null;

    const exportFormat = control.parent?.get('answerExportFormat');

    if (exportFormat?.value === MarkupFormats.HTML) {
      const plain = htmlToPlainText(control.value);
      if (!plain.trim().length) return { minlength: { requiredLength: 1 } };
    }

    return null;
  }

  getAnswersLocales(): { first: string; second: string }[] {
    const locales = new Set<{ first: string; second: string }>();
    this.form.controls.answers.controls.forEach((ctrl) => {
      locales.add(this.state.locales.find((l) => l.first === ctrl.value.locale));
    });

    return [...locales];
  }

  getConnectorTypeById(connectorId: string): ConnectorTypeConfiguration {
    if (!connectorId) connectorId = 'web';
    return this.connectorTypes.find((ct) => ct.connectorType.id === connectorId);
  }

  isInterfaceTypeAllowedForConnector(connectorId: string, interfaceType): boolean {
    const connector = this.getConnectorTypeById(connectorId);

    if (
      connector.connectorType.userInterfaceType !== UserInterfaceType.textAndVoiceAssistant &&
      connector.connectorType.userInterfaceType !== interfaceType
    ) {
      return false;
    }

    return true;
  }

  getAnswerI18nValueByLocale(locale: string): i18nValue[] {
    return this.form.controls.answers.controls
      .filter(
        (ctrl) => ctrl.value.locale === locale && this.isInterfaceTypeAllowedForConnector(ctrl.value.connectorId, ctrl.value.interfaceType)
      )
      .map((ctrl) => ctrl.value as i18nValue);
  }

  longLocaleName(locale: string): string {
    return this.state.localeName(locale);
  }

  getAnswerConnectorLabel(i18nValue: i18nValue) {
    let label = getConnectorLabel(i18nValue.connectorId);

    const connector = this.getConnectorTypeById(i18nValue.connectorId);
    if (connector.connectorType.userInterfaceType === UserInterfaceType.textAndVoiceAssistant) {
      if (i18nValue.interfaceType === UserInterfaceType.textChat) {
        label += ' (Text channel)';
      }
      if (i18nValue.interfaceType === UserInterfaceType.voiceAssistant) {
        label += ' (Voice channel)';
      }
    }

    return label;
  }

  getAnswerConnectorLabelTooltip(i18nValue: i18nValue): string {
    let tooltip = `Edit answer in ${this.state.localeName(i18nValue.locale)} for connector ${getConnectorLabel(i18nValue.connectorId)}`;

    const connector = this.getConnectorTypeById(i18nValue.connectorId);
    if (connector.connectorType.userInterfaceType === UserInterfaceType.textAndVoiceAssistant) {
      if (i18nValue.interfaceType === UserInterfaceType.textChat) {
        tooltip += ' and text channel';
      }
      if (i18nValue.interfaceType === UserInterfaceType.voiceAssistant) {
        tooltip += ' and voice channel';
      }
    }

    return tooltip;
  }

  getAnswerI18nLocalizedLabel(i18nValue: i18nValue): I18nLocalizedLabel {
    return this.faq.answer?.i18n.find(
      (i18n) =>
        i18n.locale === i18nValue.locale &&
        i18n.interfaceType === i18nValue.interfaceType &&
        (i18nValue.connectorId ? i18n.connectorId === i18nValue.connectorId : true)
    );
  }

  getConnectorTypeIconById(connectorId: string): string {
    if (connectorId === null) connectorId = 'web';
    return RestService.connectorIconUrl(connectorId);
  }

  initSelectedAnswerI18nValue(): void {
    this.selectedAnswerI18nValue = this.getAnswerI18nValueByLocale(this.state.currentLocale)[0] as i18nValue;
  }

  selectedAnswerI18nValue: i18nValue;

  getSelectedAnswerI18nControl(): FormGroup<I18nEditForm> {
    return this.form.controls.answers.controls.find(
      (ctrl) =>
        ctrl.value.locale === this.selectedAnswerI18nValue.locale &&
        ctrl.value.connectorId === this.selectedAnswerI18nValue.connectorId &&
        ctrl.value.interfaceType === this.selectedAnswerI18nValue.interfaceType
    );
  }

  doesSelectedAnswerSupportsRichText(): boolean {
    return this.selectedAnswerI18nValue.connectorId === null;
  }

  compareSelectedAnswerByOptions(a, b): boolean {
    return a?.locale === b?.locale && a?.connectorId === b?.connectorId && a?.interfaceType === b?.interfaceType;
  }

  getSupportedLocalesWithUnassignedAnswerCombinations(): { first: string; second: string }[] {
    const locales = new Set<{ first: string; second: string }>();
    this.state.currentApplication.supportedLocales.forEach((loc) => {
      if (this.hasUnassignedAnswerCombinationsForLocales([loc])) locales.add(this.state.locales.find((l) => l.first === loc));
    });

    return [...locales];
  }

  hasUnassignedAnswerCombinationsForLocales(locales: string[]): boolean {
    return locales.some((locale) => {
      return this.supportedConnectors.some((connector) => {
        let interfaceTypes =
          connector.userInterfaceType === UserInterfaceType.textAndVoiceAssistant
            ? [UserInterfaceType.textChat, UserInterfaceType.voiceAssistant]
            : [connector.userInterfaceType];

        return interfaceTypes.some((interfaceType) => {
          return !this.answers.value.find((ctrl) => {
            return (
              ctrl.locale === locale &&
              (connector.id === 'web' ? !ctrl.connectorId : ctrl.connectorId === connector.id) &&
              ctrl.interfaceType === interfaceType
            );
          });
        });
      });
    });
  }

  hasUnassignedAnswerCombinations(): boolean {
    return this.hasUnassignedAnswerCombinationsForLocales(this.state.currentApplication.supportedLocales);
  }

  getUnassignedConnectorsForLocale(locale: string): GenericObject<string> {
    const connectors = deepCopy(this.connectorsList);
    const supportedConnectorsIds = this.supportedConnectors.map((c) => c.id);

    // We only keep supported connectors
    Object.entries(this.connectorsList).forEach((c) => {
      if (!supportedConnectorsIds.includes(c[0])) {
        delete connectors[c[0]];
      }
    });

    this.answers.controls
      .filter((ctrl) => ctrl.value.locale === locale)
      .forEach((ctrl) => {
        const connector = this.getConnectorTypeById(ctrl.value.connectorId);
        if (connector.connectorType.userInterfaceType !== UserInterfaceType.textAndVoiceAssistant) {
          delete connectors[connector.connectorType.id];
        } else {
          if (
            this.answers.controls.filter((ctrl) => ctrl.value.locale === locale && ctrl.value.connectorId === connector.connectorType.id)
              .length > 1
          ) {
            delete connectors[ctrl.value.connectorId];
          }
        }
      });

    return connectors;
  }

  originalOrder = (a: KeyValue<string, string>, b: KeyValue<string, string>): number => {
    return 0;
  };

  getUnassignedInterfacesForLocaleAndConnector(
    locale: string,
    connector: string
  ): GenericObject<{ label: string; value: UserInterfaceType }> {
    let interfaces = {
      textChat: {
        label: 'Text Chat channel',
        value: UserInterfaceType.textChat
      },
      voiceAssistant: {
        label: 'Voice Assistant channel',
        value: UserInterfaceType.voiceAssistant
      }
    };

    this.answers.controls
      .filter((ctrl) => ctrl.value.locale === locale && ctrl.value.connectorId === connector)
      .forEach((ctrl) => {
        if (ctrl.value.interfaceType === UserInterfaceType.textChat) {
          delete interfaces['textChat'];
        }
        if (ctrl.value.interfaceType === UserInterfaceType.voiceAssistant) {
          delete interfaces['voiceAssistant'];
        }
      });

    return interfaces;
  }

  trackByInterface(index: number, item: KeyValue<string, { label: string; value: UserInterfaceType }>) {
    return item.key;
  }

  answerCombinationSelector: AnswerCombinationSelector;

  resetAnswerCombinationSelector(): void {
    this.answerCombinationSelector = {
      locale: undefined,
      connector: undefined,
      interface: undefined
    };
  }

  setAnswerCombinationSelector<S extends keyof AnswerCombinationSelector>(stage: S, value: AnswerCombinationSelector[S]) {
    this.answerCombinationSelector[stage] = value;

    if (stage === 'connector') {
      const connector = this.getConnectorTypeById(value as string);
      if (connector.connectorType.userInterfaceType !== UserInterfaceType.textAndVoiceAssistant) {
        this.answerCombinationSelectorPopoverRef.hide();

        // if we add a Web connector, we should not give its id but a falsy value
        const connectorId = this.answerCombinationSelector.connector === 'web' ? null : this.answerCombinationSelector.connector;

        this.answers.push(
          new FormGroup({
            locale: new FormControl(this.answerCombinationSelector.locale),
            connectorId: new FormControl(connectorId),
            interfaceType: new FormControl(connector.connectorType.userInterfaceType),
            // It is assumed that if the user adds a response combination, the label of that combination is required.
            label: new FormControl('', [Validators.required, this.validateAnswerMarkupContent.bind(this)]),
            answerExportFormat: new FormControl(MarkupFormats.PLAINTEXT)
          })
        );

        this.selectedAnswerI18nValue = this.answers.controls.find(
          (ctrl) =>
            ctrl.value.locale === this.answerCombinationSelector.locale &&
            ctrl.value.connectorId === connectorId &&
            ctrl.value.interfaceType === connector.connectorType.userInterfaceType
        ).value;
      }
    }

    if (stage === 'interface') {
      this.answerCombinationSelectorPopoverRef.hide();
      this.answers.push(
        new FormGroup({
          locale: new FormControl(this.answerCombinationSelector.locale),
          connectorId: new FormControl(this.answerCombinationSelector.connector),
          interfaceType: new FormControl(this.answerCombinationSelector.interface),
          // It is assumed that if the user adds a response combination, the label of that combination is required.
          label: new FormControl('', [Validators.required, this.validateAnswerMarkupContent.bind(this)]),
          answerExportFormat: new FormControl(MarkupFormats.PLAINTEXT)
        })
      );

      this.selectedAnswerI18nValue = this.answers.controls.find(
        (ctrl) =>
          ctrl.value.locale === this.answerCombinationSelector.locale &&
          ctrl.value.connectorId === this.answerCombinationSelector.connector &&
          ctrl.value.interfaceType === this.answerCombinationSelector.interface
      ).value;
    }
  }

  stopPropagation(event: MouseEvent): void {
    event.stopPropagation();
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
    return normalize(str)
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
                  if (this.utterancesListWrapper) {
                    this.utterancesListWrapper.nativeElement.scrollTop = this.utterancesListWrapper.nativeElement.scrollHeight;
                  }
                });
              }
              this.lookingForSameUterranceInOtherInent = false;
            },
            error: () => {
              this.lookingForSameUterranceInOtherInent = false;
            }
          });
      }

      if (this.addUtteranceInput?.nativeElement) this.addUtteranceInput.nativeElement.value = '';
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
      let faqData: FaqDefinitionExtended & Partial<i18nValue> = deepCopy({
        ...this.faq,
        ...this.form.value
      });

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
              this.saveAnswers(faqData);
            }
          });
          return;
        }
      }

      this.saveAnswers(faqData);
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

  saveAnswers(faqData: FaqDefinitionExtended & Partial<ExtractFormControlTyping<FaqEditForm>>) {
    const shouldSaveAnswers =
      !faqData.id ||
      this.form.controls.answers.controls.some((ctrl) => {
        return ctrl.dirty;
      });

    if (shouldSaveAnswers) {
      // Existing answer, we should update
      if (faqData.answer?.i18n) {
        // Exclude I18nLocalizedLabels whose interface type is incompatible with their connector type
        faqData.answer.i18n = faqData.answer.i18n.filter((i18n) =>
          this.isInterfaceTypeAllowedForConnector(i18n.connectorId, i18n.interfaceType)
        );

        faqData.answers.forEach((answer) => {
          const targetI18n = faqData.answer.i18n.find(
            (i18n) =>
              i18n.locale === answer.locale &&
              i18n.interfaceType === answer.interfaceType &&
              (answer.connectorId ? i18n.connectorId === answer.connectorId : !i18n.connectorId)
          );

          if (targetI18n) {
            // I18nLocalizedLabel exists, we should update
            targetI18n.label = answer.label;
            targetI18n.interfaceType = UserInterfaceType[targetI18n.interfaceType] as any;
          } else {
            // new I18nLocalizedLabel, we should create
            faqData.answer.i18n.push(
              new I18nLocalizedLabel(answer.locale, answer.interfaceType, answer.label, false, answer.connectorId, [])
            );
          }
        });

        // createI18nLabel returns an I18nLabel whose “namespace” attribute has been changed to lowercase; we need to correct this if this faq was previously created.
        // TODO : Fix back behavior
        faqData.answer.namespace = this.state.currentApplication.namespace;

        // Save the I18nLabel
        this.botService.saveI18nLabel(faqData.answer).subscribe((_) => {
          delete faqData.answers;
          // We can now save the Faq
          this.save(faqData);
        });
      } else {
        // We are creating a new Faq, we should create the answer I18nLabel
        const currentLocaleAnswerLabel = faqData.answers.find((answer) => answer.locale === this.state.currentLocale);

        const i18nLabelCreationRequest = new CreateI18nLabelRequest('faq', currentLocaleAnswerLabel.label, this.state.currentLocale);

        this.botService.createI18nLabel(i18nLabelCreationRequest).subscribe((i18n) => {
          // We associate the newly created I18nLabel to the Faq
          faqData.answer = i18n;

          // As the answer I18nLabel now exists, we can add the other I18nLocalizedLabels if any
          if (faqData.answers.length > 1) {
            faqData.answers.forEach((answer) => {
              if (answer !== currentLocaleAnswerLabel) {
                faqData.answer.i18n.push(
                  new I18nLocalizedLabel(answer.locale, answer.interfaceType, answer.label, false, answer.connectorId, [], [])
                );
              }
            });

            // createI18nLabel returns an I18nLabel whose “namespace” attribute has been changed to lowercase; we need to correct this.
            // TODO : Fix back behavior
            faqData.answer.namespace = this.state.currentApplication.namespace;

            // Save the I18nLabel with its new I18nLocalizedLabel
            this.botService.saveI18nLabel(faqData.answer).subscribe((_) => {
              // We can now save the Faq
              delete faqData.answers;
              this.save(faqData);
            });
          } else {
            // There was only 1 I18nLocalizedLabel, we can save the Faq
            delete faqData.answers;
            this.save(faqData);
          }
        });
      }
    } else {
      // The answers haven't been touched, we just save the faq.
      delete faqData.answers;
      this.save(faqData);
    }
  }

  save(faqData): void {
    this.onSave.emit(faqData);
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
