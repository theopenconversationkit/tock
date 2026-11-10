import { Component, inject, OnDestroy, OnInit, QueryList, TemplateRef, ViewChild, ViewChildren } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import {
  debounceTime,
  forkJoin,
  merge,
  Observable,
  of,
  Subject,
  switchMap,
  takeUntil,
  pairwise,
  from,
  catchError,
  map,
  distinctUntilChanged,
  filter
} from 'rxjs';
import { NbDialogRef, NbDialogService, NbToastrService, NbTooltipDirective, NbWindowService } from '@nebular/theme';

import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import {
  EnginesConfigurations,
  QuestionCondensing_prompt,
  QuestionAnswering_prompt,
  DocumentSearchTypes
} from './models/engines-configurations';
import { RagEmbeddingCoherence, RagIndexState, RagIndexStatus, RagSettings } from './models';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { DebugViewerWindowComponent } from '../../shared/components/debug-viewer-window/debug-viewer-window.component';
import { deepCopy, getExportFileName, readFileAsText } from '../../shared/utils';
import {
  AiEngineSettingKeyName,
  EnginesConfiguration,
  AiEngineProvider,
  ProvidersConfigurationParam,
  PromptTypeKeyName,
  PromptDefinitionFormatter
} from '../../shared/model/ai-settings';
import { ChoiceDialogComponent } from '../../shared/components';
import { saveAs } from 'file-saver-es';
import { FileValidators } from '../../shared/validators';
import { CanComponentDeactivate } from '../../shared/guards/can-deactivate.guard';
import { DirtyStateGuard, DirtyStateService } from '../../core/dirty-state.service';
import { TranslocoService } from '@jsverse/transloco';
import { IndexListResponse, VectorStoreIndex } from '../../vector-store-inspection/models/vector-store-inspection.models';

interface RagSettingsForm {
  id: FormControl<string>;
  enabled: FormControl<boolean>;

  debugEnabled: FormControl<boolean>;
  explainabilityEnabled: FormControl<boolean>;

  indexSessionId: FormControl<string>;
  documentSearchType: FormControl<string>;

  maxDocumentsRetrieved: FormControl<number>;

  questionCondensingLlmProvider: FormControl<AiEngineProvider>;
  questionCondensingLlmSetting: FormGroup<any>;
  questionCondensingPrompt: FormGroup<any>;

  maxMessagesFromHistory: FormControl<number>;

  questionAnsweringLlmProvider: FormControl<AiEngineProvider>;
  questionAnsweringLlmSetting: FormGroup<any>;
  questionAnsweringPrompt: FormGroup<any>;

  emProvider: FormControl<AiEngineProvider>;
  emSetting: FormGroup<any>;
}

@Component({
  selector: 'tock-rag-settings',
  templateUrl: './rag-settings.component.html',
  styleUrls: ['./rag-settings.component.scss'],
  standalone: false
})
export class RagSettingsComponent implements OnInit, CanComponentDeactivate, DirtyStateGuard, OnDestroy {
  destroy$: Subject<unknown> = new Subject();

  configurations: BotApplicationConfiguration[];

  enginesConfigurations = EnginesConfigurations;

  engineSettingKeyName = AiEngineSettingKeyName;

  documentSearchTypes = DocumentSearchTypes;

  questionCondensing_prompt = QuestionCondensing_prompt;

  questionAnswering_prompt = QuestionAnswering_prompt;

  settingsBackup: RagSettings;

  isSubmitted: boolean = false;

  loading: boolean = false;

  /** State and embedding coherence of the collection the configured index session resolves to, refreshed as it is edited. */
  indexStatus: RagIndexStatus | null = null;

  /** Session id the current `indexStatus` was obtained for: a status only speaks for its own candidate. */
  indexStatusSession: string | null = null;
  private indexStatusTrigger$: Subject<void> = new Subject();

  /**
   * Selectable indexes of the current bot, most recently fed first (ordered server side). Only collections
   * whose chunks carry an index session id: any other collection is invalid and cannot back the RAG.
   */
  indexes: VectorStoreIndex[] = [];

  /** Collections found for the bot whose chunks carry no index session id: reported, never selectable. */
  invalidIndexes: VectorStoreIndex[] = [];

  /** The index list was fetched successfully. */
  indexesLoaded: boolean = false;

  /** The index list could not be fetched. Lasts until the page is reloaded. */
  indexesLoadFailed: boolean = false;

  /**
   * An index status check failed on the server side. Only a later successful check clears it: a skipped check
   * (blank session, incomplete form) proves nothing about the outage.
   */
  indexStatusUnavailable: boolean = false;

  /** The form content comes from an import and was never saved: a save then always carries more than an activation change. */
  private importedSettings: boolean = false;

  @ViewChild('exportConfirmationModal') exportConfirmationModal: TemplateRef<any>;
  @ViewChild('importModal') importModal: TemplateRef<any>;
  @ViewChildren(NbTooltipDirective) tooltips: QueryList<NbTooltipDirective>;

  private state = inject(StateService);
  private rest = inject(RestService);
  private toastrService = inject(NbToastrService);
  private botConfiguration = inject(BotConfigurationService);
  private nbWindowService = inject(NbWindowService);
  private nbDialogService = inject(NbDialogService);
  private dirtyState = inject(DirtyStateService);
  private translocoService = inject(TranslocoService);

  constructor() {}

  ngOnInit(): void {
    this.dirtyState.register(this);

    // Index status is refreshed on load and on every edit of the session id or the embedding. Debounced,
    // and switchMap cancels an in-flight check so a burst of edits only surfaces the last answer.
    this.indexStatusTrigger$
      .pipe(
        debounceTime(400),
        switchMap(() => this.loadIndexStatus()),
        takeUntil(this.destroy$)
      )
      .subscribe(({ session, status }) => {
        this.indexStatusSession = session;
        this.indexStatus = status;
      });

    // The check also runs when the form becomes complete, as it is skipped while incomplete.
    merge(
      this.indexSessionId.valueChanges,
      this.emProvider.valueChanges,
      this.form.get('emSetting').valueChanges,
      this.form.statusChanges.pipe(
        distinctUntilChanged(),
        filter((status) => status === 'VALID')
      )
    )
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.indexStatusTrigger$.next());

    this.form.valueChanges.pipe(takeUntil(this.destroy$), debounceTime(200)).subscribe(() => {
      this.setActivationDisabledState();
    });

    this.form
      .get('questionCondensingLlmProvider')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((engine: AiEngineProvider) => {
        this.initFormSettings(AiEngineSettingKeyName.questionCondensingLlmSetting, engine);
      });

    this.form
      .get('questionAnsweringLlmProvider')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((engine: AiEngineProvider) => {
        this.initFormSettings(AiEngineSettingKeyName.questionAnsweringLlmSetting, engine);
      });

    this.form
      .get('emProvider')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((engine: AiEngineProvider) => {
        this.initFormSettings(AiEngineSettingKeyName.emSetting, engine);
      });

    [PromptTypeKeyName.questionCondensingPrompt, PromptTypeKeyName.questionAnsweringPrompt].forEach((wich) => {
      this.form
        .get(wich)
        .valueChanges.pipe(pairwise(), takeUntil(this.destroy$))
        .subscribe(([prev, next]) => {
          if (
            next?.formatter === PromptDefinitionFormatter.jinja2 &&
            prev?.formatter === PromptDefinitionFormatter.fstring &&
            next?.template?.length
          ) {
            this.fStringToJinja(wich);
          }
        });
    });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs: BotApplicationConfiguration[]) => {
      delete this.settingsBackup;

      // Reset form on configuration change
      this.form.reset();
      this.form.markAsPristine();

      this.setFormDefaultValues();

      // Reset formGroup controls too, if any
      this.resetFormGroupControls(AiEngineSettingKeyName.questionCondensingLlmSetting);
      this.resetFormGroupControls(AiEngineSettingKeyName.questionAnsweringLlmSetting);
      this.resetFormGroupControls(AiEngineSettingKeyName.emSetting);

      // Reset accordions states
      this.accordionItemsExpandedState = undefined;

      this.initFormPrompt(PromptTypeKeyName.questionCondensingPrompt);
      this.initFormPrompt(PromptTypeKeyName.questionAnsweringPrompt);

      this.loading = true;
      this.configurations = confs;

      this.indexStatus = null;
      this.indexStatusSession = null;
      this.indexes = [];
      this.invalidIndexes = [];
      this.indexesLoaded = false;
      this.indexesLoadFailed = false;
      this.indexStatusUnavailable = false;
      this.importedSettings = false;

      if (confs.length) {
        // The index list is loaded with the settings and assigned before the form is patched: nb-select drops a
        // selection whose option is not rendered yet.
        forkJoin([this.getRagSettingsLoader(), this.getIndexesLoader()]).subscribe((res) => {
          const settings = res[0];
          this.applyIndexes(res[1]);
          if (settings?.id) {
            this.settingsBackup = deepCopy(settings);
            setTimeout(() => {
              this.initForm(settings);
              this.indexStatusTrigger$.next();
            });
          } else {
            this.indexStatusTrigger$.next();
          }

          this.loading = false;
        });
      } else {
        this.loading = false;
      }
    });
  }

  private getRagSettingsLoader(): Observable<RagSettings> {
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/rag`;
    return this.rest.get<RagSettings>(url, (settings: RagSettings) => settings);
  }

  /**
   * Indexes of the current bot. Never errors: a failure resolves to null. Silent call, the failure is reported by
   * the page itself. An orchestrator error reaches the admin as an empty body, which the rest service turns into `{}`:
   * a response without an `indexes` array is a failure too, while a real empty list arrives as `{ indexes: [] }`.
   */
  private getIndexesLoader(): Observable<VectorStoreIndex[] | null> {
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/vector-store/indexes`;
    return this.rest
      .get<VectorStoreIndex[] | null>(
        url,
        (response: IndexListResponse) => (Array.isArray(response?.indexes) ? response.indexes : null),
        true
      )
      .pipe(catchError(() => of(null)));
  }

  private applyIndexes(indexes: VectorStoreIndex[] | null): void {
    if (indexes === null) {
      this.indexes = [];
      this.invalidIndexes = [];
      this.indexesLoaded = false;
      this.indexesLoadFailed = true;
      return;
    }
    this.indexes = indexes.filter((index) => !!index.indexSessionId?.trim());
    this.invalidIndexes = indexes.filter((index) => !index.indexSessionId?.trim());
    this.indexesLoaded = true;
  }

  /**
   * The candidate configuration as the save endpoint expects it. Reused as is for the read only
   * index-status check: the server reads its `indexSessionId` and `emSetting` only, but the endpoint
   * deserializes the full configuration DTO, exactly like the save.
   */
  private buildRagConfigBody(): RagSettings {
    const formValue: RagSettings = deepCopy(this.form.value) as unknown as RagSettings;
    delete formValue['questionCondensingLlmProvider'];
    delete formValue['questionAnsweringLlmProvider'];
    delete formValue['emProvider'];
    formValue.namespace = this.state.currentApplication.namespace;
    formValue.botId = this.state.currentApplication.name;
    return formValue;
  }

  /**
   * Reads the state and embedding coherence of the collection the current session id resolves to, along with the
   * session it was read for. Only a valid answer from the server clears the outage flag; a skipped check leaves it
   * untouched.
   */
  private loadIndexStatus(): Observable<{ session: string | null; status: RagIndexStatus | null }> {
    const session = (this.indexSessionId.value ?? '').trim() || null;

    // A blank session resolves to NONE server side; report it directly without a round trip.
    if (!session) {
      return of({
        session,
        status: { indexState: RagIndexState.NONE, collectionEmbeddingModel: null, coherence: RagEmbeddingCoherence.UNKNOWN }
      });
    }

    // The endpoint deserializes the full configuration DTO: an incomplete form would fail server side for reasons
    // unrelated to the vector store. No check until the form is complete; the hint is cleared meanwhile.
    if (!this.form.valid) {
      return of({ session, status: null });
    }

    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/rag/index-status`;
    return this.rest.post<RagSettings, RagIndexStatus>(url, this.buildRagConfigBody(), null, null, true).pipe(
      map((status) => {
        if (!status?.indexState) {
          // Empty answer: the orchestrator error surfaced as an empty body.
          this.indexStatusUnavailable = true;
          return { session, status: null };
        }
        this.indexStatusUnavailable = false;
        return { session, status };
      }),
      catchError((error) => {
        // A 400/422 is the admin refusing to read the candidate, before any orchestrator call: it proves neither an
        // outage nor a recovery, the flag is left as is. Anything else means the vector store cannot be read.
        if (!(error instanceof HttpErrorResponse && [400, 422].includes(error.status))) {
          this.indexStatusUnavailable = true;
        }
        return of({ session, status: null });
      })
    );
  }

  form = new FormGroup<RagSettingsForm>({
    id: new FormControl(null),
    enabled: new FormControl({ value: undefined, disabled: !this.canRagBeActivated() }),

    debugEnabled: new FormControl({ value: undefined, disabled: !this.canRagBeActivated() }),
    explainabilityEnabled: new FormControl({ value: undefined, disabled: !this.canRagBeActivated() }),

    indexSessionId: new FormControl(undefined),
    documentSearchType: new FormControl(undefined),

    maxDocumentsRetrieved: new FormControl(undefined),

    questionCondensingLlmProvider: new FormControl(undefined, [Validators.required]),
    questionCondensingLlmSetting: new FormGroup({}),
    questionCondensingPrompt: new FormGroup({}),

    maxMessagesFromHistory: new FormControl(undefined),

    questionAnsweringLlmProvider: new FormControl(undefined, [Validators.required]),
    questionAnsweringLlmSetting: new FormGroup({}),
    questionAnsweringPrompt: new FormGroup({}),

    emProvider: new FormControl(undefined, [Validators.required]),
    emSetting: new FormGroup({})
  });

  get enabled(): FormControl {
    return this.form.get('enabled') as FormControl;
  }

  get debugEnabled(): FormControl {
    return this.form.get('debugEnabled') as FormControl;
  }

  get explainabilityEnabled(): FormControl {
    return this.form.get('explainabilityEnabled') as FormControl;
  }

  get questionCondensingLlmProvider(): FormControl {
    return this.form.get('questionCondensingLlmProvider') as FormControl;
  }

  get questionAnsweringLlmProvider(): FormControl {
    return this.form.get('questionAnsweringLlmProvider') as FormControl;
  }

  get emProvider(): FormControl {
    return this.form.get('emProvider') as FormControl;
  }

  get maxMessagesFromHistory(): FormControl {
    return this.form.get('maxMessagesFromHistory') as FormControl;
  }

  get indexSessionId(): FormControl {
    return this.form.get('indexSessionId') as FormControl;
  }

  get maxDocumentsRetrieved(): FormControl {
    return this.form.get('maxDocumentsRetrieved') as FormControl;
  }

  get documentSearchType(): FormControl {
    return this.form.get('documentSearchType') as FormControl;
  }

  /**
   * The vector store data could not be retrieved from the orchestrator (index list or index status check).
   * Unstable situation: no save is allowed, except disabling RAG without any other change.
   */
  get vectorStoreUnavailable(): boolean {
    return this.indexesLoadFailed || this.indexStatusUnavailable;
  }

  /** Session id currently held by the form, trimmed; null when blank. */
  get configuredSessionId(): string | null {
    return (this.indexSessionId.value ?? '').trim() || null;
  }

  /**
   * A session id is configured but its collection is missing or unusable. Two sources:
   * - the index list loaded with the page: no valid index matches (deleted collection, import from another
   *   environment, invalid collection). Only asserted once the list is known: a failed load proves nothing.
   *   Exact match on the raw session id, the value the ingestion wrote in the chunks;
   * - the index status check: the server answered MISSING for this very session, which also covers an index
   *   removed after the list was loaded.
   */
  get configuredIndexMissing(): boolean {
    const session = this.configuredSessionId;
    if (!session) return false;
    const missingPerStatus = this.indexStatus?.indexState === RagIndexState.MISSING && this.indexStatusSession === session;
    const missingFromList = this.indexesLoaded && !this.indexes.some((index) => index.indexSessionId === session);
    return missingPerStatus || missingFromList;
  }

  /**
   * Nebular mounts tooltips in a detached overlay: when the options list closes, an option is destroyed without a
   * mouseleave and its tooltip stays on screen. Closing them by hand is the only reliable fix.
   */
  hideIndexTooltips(): void {
    this.tooltips?.forEach((tooltip) => tooltip.hide());
  }

  /** The index saved in the configuration, recomputed client side so it follows each confirmed save. */
  isCurrentIndex(index: VectorStoreIndex): boolean {
    return !!this.settingsBackup?.indexSessionId && index.indexSessionId === this.settingsBackup.indexSessionId;
  }

  /** RAG activation as it would be submitted: a disabled toggle is left out of the form value, hence saved as false. */
  get submittedEnabled(): boolean {
    return this.form.value.enabled === true;
  }

  /**
   * The only change is turning off a RAG saved as enabled. That save carries the stored configuration untouched,
   * so the server neither validates it nor calls the orchestrator: the one write allowed when the vector store is
   * unreachable.
   */
  private get isDeactivationOnly(): boolean {
    if (!this.settingsBackup?.enabled || this.submittedEnabled || this.importedSettings) return false;
    return Object.entries(this.form.controls)
      .filter(([key]) => key !== 'enabled')
      .every(([, control]) => control.pristine);
  }

  /** Why the form cannot be saved right now, as a translation key; null when saving is allowed. */
  get saveBlockReason(): string | null {
    if (this.vectorStoreUnavailable) {
      return this.isDeactivationOnly ? null : 'rag.rag-settings.save_blocked_unavailable';
    }
    if (this.configuredIndexMissing && this.submittedEnabled) {
      return 'rag.rag-settings.save_blocked_index_missing';
    }
    return null;
  }

  /** The index exists but does not declare the model it was built with, so coherence cannot be asserted. */
  get indexStatusUnknownModel(): boolean {
    return this.indexStatus?.indexState === RagIndexState.READY && this.indexStatus?.coherence === RagEmbeddingCoherence.UNKNOWN;
  }

  /** The index was built with a different embedding model: the save will be refused. */
  get indexStatusMismatch(): boolean {
    return this.indexStatus?.coherence === RagEmbeddingCoherence.MISMATCH;
  }

  get indexStatusIndexModel(): string | null {
    return this.indexStatus?.collectionEmbeddingModel ?? null;
  }

  get indexStatusBotModel(): string {
    return this.form.get('emSetting')?.get('model')?.value ?? '';
  }

  get canSave(): boolean {
    if (this.saveBlockReason) return false;
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  accordionItemsExpandedState: Map<string, boolean>;

  isAccordionItemsExpanded(itemName: string): boolean {
    if (!this.accordionItemsExpandedState) {
      this.accordionItemsExpandedState = new Map();
    }

    if (!this.accordionItemsExpandedState.has(itemName)) {
      switch (itemName) {
        case 'questionConsensingConfiguration':
          this.accordionItemsExpandedState.set(
            'questionConsensingConfiguration',
            this.form.get('questionCondensingLlmProvider').invalid ||
              this.form.get(AiEngineSettingKeyName.questionCondensingLlmSetting).invalid
          );
          break;
        case 'questionCondensingPrompt':
          this.accordionItemsExpandedState.set(
            'questionCondensingPrompt',
            this.form.get(PromptTypeKeyName.questionCondensingPrompt).invalid
          );
          break;
        case 'questionAnsweringConfiguration':
          this.accordionItemsExpandedState.set(
            'questionAnsweringConfiguration',
            this.form.get('questionAnsweringLlmProvider').invalid ||
              this.form.get(AiEngineSettingKeyName.questionAnsweringLlmSetting).invalid
          );
          break;
        case 'questionAnsweringPrompt':
          this.accordionItemsExpandedState.set('questionAnsweringPrompt', this.form.get(PromptTypeKeyName.questionAnsweringPrompt).invalid);
          break;
        case 'embeddingConfiguration':
          this.accordionItemsExpandedState.set(
            'embeddingConfiguration',
            this.form.get('emProvider').invalid || this.form.get('emSetting').invalid
          );
          break;
      }
    }

    return this.accordionItemsExpandedState.get(itemName);
  }

  shouldDisplayPromptParam(parentGroup: string, param: ProvidersConfigurationParam) {
    // Goal : We want templates to use the Jinja2 format by default.
    if (param.key === 'formatter') {
      // We only care about the "formatter" param
      if (this.form.get(parentGroup).get(param.key).value === PromptDefinitionFormatter.jinja2) {
        // If the format is already Jinja2, we can hide the choice control
        return false;
      }
    }
    return true;
  }

  fStringToJinja(group: string): void {
    const source = this.form.get(group).get('template').value;
    const result = source.replace(/{(.*?)}/g, '{{$1}}');
    this.form.get(group).patchValue({
      template: result
    });
  }

  initFormPrompt(group: PromptTypeKeyName): void {
    this.resetFormGroupControls(group);

    let params: ProvidersConfigurationParam[];

    if (group === PromptTypeKeyName.questionCondensingPrompt) {
      params = QuestionCondensing_prompt;
    }
    if (group === 'questionAnsweringPrompt') {
      params = QuestionAnswering_prompt;
    }

    params.forEach((param) => {
      this.form.controls[group].addControl(param.key, new FormControl(param.defaultValue, Validators.required));
    });
  }

  initFormSettings(group: AiEngineSettingKeyName, provider: AiEngineProvider): void {
    let requiredConfiguration: EnginesConfiguration = EnginesConfigurations[group].find((c) => c.key === provider);

    if (requiredConfiguration) {
      // Purge existing controls that may contain values incompatible with a new control with the same name after engine change
      this.resetFormGroupControls(group);

      requiredConfiguration.params.forEach((param) => {
        const isParamRequired = param.required === undefined ? true : param.required;
        this.form.controls[group].addControl(param.key, new FormControl(param.defaultValue, isParamRequired ? Validators.required : null));
      });

      this.form.controls[group].addControl('provider', new FormControl(provider));
    }
  }

  resetFormGroupControls(group: string): void {
    const existingGroupKeys = Object.keys(this.form.controls[group].controls);
    existingGroupKeys.forEach((key) => {
      this.form.controls[group].removeControl(key);
    });
  }

  setFormDefaultValues(): void {
    this.form.patchValue({
      debugEnabled: false,
      explainabilityEnabled: true,
      maxMessagesFromHistory: 5,
      maxDocumentsRetrieved: 4
    });
  }

  initForm(settings: RagSettings): void {
    this.initFormSettings(AiEngineSettingKeyName.questionCondensingLlmSetting, settings.questionCondensingLlmSetting?.provider);
    this.initFormSettings(AiEngineSettingKeyName.questionAnsweringLlmSetting, settings.questionAnsweringLlmSetting?.provider);
    this.initFormSettings(AiEngineSettingKeyName.emSetting, settings.emSetting?.provider);

    this.form.patchValue({
      questionCondensingLlmProvider: settings.questionCondensingLlmSetting?.provider,
      questionAnsweringLlmProvider: settings.questionAnsweringLlmSetting?.provider,
      emProvider: settings.emSetting?.provider
    });
    this.form.patchValue(settings);

    this.initFormPrompt(PromptTypeKeyName.questionCondensingPrompt);
    this.initFormPrompt(PromptTypeKeyName.questionAnsweringPrompt);

    this.form.patchValue({
      questionCondensingPrompt: settings.questionCondensingPrompt,
      questionAnsweringPrompt: settings.questionAnsweringPrompt
    });

    this.accordionItemsExpandedState = undefined;

    this.form.markAsPristine();
  }

  canRagBeActivated(): boolean {
    return this.form ? this.form.valid && this.indexSessionId.value?.length : false;
  }

  setActivationDisabledState(): void {
    if (this.canRagBeActivated()) {
      this.enabled.enable();
      this.debugEnabled.enable();
      this.explainabilityEnabled.enable();
    } else {
      this.enabled.disable();
      this.debugEnabled.disable();
      this.explainabilityEnabled.disable();
    }
  }

  get currentQuestionCondensingConfig(): EnginesConfiguration {
    return EnginesConfigurations[AiEngineSettingKeyName.questionCondensingLlmSetting].find(
      (e) => e.key === this.questionCondensingLlmProvider.value
    );
  }

  get currentQuestionAnsweringConfig(): EnginesConfiguration {
    return EnginesConfigurations[AiEngineSettingKeyName.questionAnsweringLlmSetting].find(
      (e) => e.key === this.questionAnsweringLlmProvider.value
    );
  }

  get currentEmConfig(): EnginesConfiguration {
    return EnginesConfigurations[AiEngineSettingKeyName.emSetting].find((e) => e.key === this.emProvider.value);
  }

  cancel(): void {
    this.importedSettings = false;
    this.initForm(this.settingsBackup);
  }

  private normalizeStringValues(control: FormGroup): void {
    Object.entries(control.controls).forEach(([key, childControl]) => {
      if (childControl instanceof FormGroup) {
        this.normalizeStringValues(childControl);
        return;
      }

      const value = childControl.value;

      if (typeof value === 'string') {
        const trimmedValue = value.trim();

        childControl.setValue(trimmedValue || null, { emitEvent: false });
      }
    });
  }

  submit(): void {
    this.isSubmitted = true;

    this.normalizeStringValues(this.form);

    if (this.canSave && this.form.dirty) {
      this.loading = true;
      const formValue: RagSettings = deepCopy(this.form.value) as unknown as RagSettings;
      delete formValue['questionCondensingLlmProvider'];
      delete formValue['questionAnsweringLlmProvider'];
      delete formValue['emProvider'];
      formValue.namespace = this.state.currentApplication.namespace;
      formValue.botId = this.state.currentApplication.name;

      // A configured index that no longer exists is only kept while it can be checked: once the list is known, a
      // save with RAG disabled clears it (saving it with RAG enabled is blocked upstream).
      if (this.configuredIndexMissing) {
        formValue.indexSessionId = null;
      }

      const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/rag`;
      this.rest.post(url, formValue, null, null, true).subscribe({
        next: (ragSettings: RagSettings) => {
          // Becomes the reference of isCurrentIndex: the current index mark moves only once the server confirmed.
          this.settingsBackup = ragSettings;
          this.importedSettings = false;

          this.form.patchValue(ragSettings);
          this.form.markAsPristine();

          this.isSubmitted = false;
          this.toastrService.success(
            this.translocoService.translate('rag.rag-settings.settings_saved_success'),
            this.translocoService.translate('rag.rag-settings.success_title'),
            {
              duration: 5000,
              status: 'success'
            }
          );

          this.loading = false;
        },
        error: (error) => this.onSaveError(error)
      });
    }
  }

  private onSaveError(error: any): void {
    // The server refuses a save whose embedding model contradicts the one recorded on the index.
    const errors = error?.error?.errors;
    const incompatibleIndex = Array.isArray(errors) && errors.some((e: any) => e?.message === 'rag.embedding.incompatible_index');
    const messageKey = incompatibleIndex ? 'rag.rag-settings.embedding_incompatible_error' : 'rag.rag-settings.an_error_occurred';

    this.toastrService.danger(
      this.translocoService.translate(messageKey),
      this.translocoService.translate('rag.rag-settings.error_title'),
      {
        duration: 5000,
        status: 'danger'
      }
    );

    if (error?.error && !incompatibleIndex) {
      this.nbWindowService.open(DebugViewerWindowComponent, {
        title: this.translocoService.translate('rag.rag-settings.an_error_occurred'),
        context: {
          debug: error.error
        }
      });
    }

    this.loading = false;
  }

  get hasExportableData(): boolean {
    if (this.questionCondensingLlmProvider.value || this.questionAnsweringLlmProvider.value || this.emProvider.value) return true;

    const formValue: RagSettings = deepCopy(this.form.value) as unknown as RagSettings;

    return Object.values(formValue).some((entry) => {
      return entry && (typeof entry !== 'object' || Object.keys(entry).length !== 0);
    });
  }

  sensitiveParams: { label: string; key: string; include: boolean; param: ProvidersConfigurationParam }[];

  exportSettings(): void {
    this.sensitiveParams = [];

    const shouldConfirm =
      (this.questionCondensingLlmProvider.value || this.questionAnsweringLlmProvider.value || this.emProvider.value) &&
      [(this.currentQuestionCondensingConfig?.params, this.currentQuestionAnsweringConfig?.params, this.currentEmConfig?.params)].some(
        (engine) => {
          return engine.some((entry) => {
            return entry.confirmExport;
          });
        }
      );

    if (shouldConfirm) {
      [
        {
          label: 'Question condensing LLM engine',
          key: AiEngineSettingKeyName.questionCondensingLlmSetting,
          params: this.currentQuestionCondensingConfig?.params
        },
        {
          label: 'Question answering LLM engine',
          key: AiEngineSettingKeyName.questionAnsweringLlmSetting,
          params: this.currentQuestionAnsweringConfig?.params
        },
        { label: 'Embedding engine', key: AiEngineSettingKeyName.emSetting, params: this.currentEmConfig?.params }
      ].forEach((engine) => {
        engine.params?.forEach((entry) => {
          if (entry.confirmExport) {
            this.sensitiveParams.push({ label: engine.label, key: engine.key, include: false, param: entry });
          }
        });
      });

      this.exportConfirmationModalRef = this.nbDialogService.open(this.exportConfirmationModal);
    } else {
      this.downloadSettings();
    }
  }

  exportConfirmationModalRef: NbDialogRef<any>;

  closeExportConfirmationModal(): void {
    this.exportConfirmationModalRef.close();
  }

  confirmExportSettings(): void {
    this.downloadSettings();
    this.closeExportConfirmationModal();
  }

  downloadSettings(): void {
    const formValue: RagSettings = deepCopy(this.form.value) as unknown as RagSettings;
    delete formValue['questionCondensingLlmProvider'];
    delete formValue['questionAnsweringLlmProvider'];
    delete formValue['emProvider'];
    delete formValue['id'];
    delete formValue['enabled'];

    if (this.sensitiveParams?.length) {
      this.sensitiveParams.forEach((sensitiveParam) => {
        if (!sensitiveParam.include) {
          delete formValue[sensitiveParam.key][sensitiveParam.param.key];
        }
      });
    }

    const jsonBlob = new Blob([JSON.stringify(formValue)], {
      type: 'application/json'
    });

    const exportFileName = getExportFileName(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      'Rag settings',
      'json'
    );

    saveAs(jsonBlob, exportFileName);

    this.toastrService.show(
      this.translocoService.translate('rag.rag-settings.dump_provided'),
      this.translocoService.translate('rag.rag-settings.dump_title'),
      { duration: 3000, status: 'success' }
    );
  }

  importModalRef: NbDialogRef<any>;

  importSettings(): void {
    this.isImportSubmitted = false;
    this.importForm.reset();
    this.importModalRef = this.nbDialogService.open(this.importModal);
  }

  closeImportModal(): void {
    this.importModalRef.close();
  }

  isImportSubmitted: boolean = false;

  importForm: FormGroup = new FormGroup({
    fileSource: new FormControl<File[]>([], {
      nonNullable: true,
      validators: [Validators.required, FileValidators.mimeTypeSupported(['application/json'])]
    })
  });

  get fileSource(): FormControl {
    return this.importForm.get('fileSource') as FormControl;
  }

  get canSaveImport(): boolean {
    return this.isImportSubmitted ? this.importForm.valid : this.importForm.dirty;
  }

  submitImportSettings(): void {
    this.isImportSubmitted = true;
    if (this.canSaveImport) {
      const file = this.fileSource.value[0];

      readFileAsText(file).then((fileContent) => {
        const settings = JSON.parse(fileContent.data);

        const hasCompatibleProvider = Object.values(AiEngineSettingKeyName).some((ekn) => {
          return settings[ekn]?.provider && Object.values(AiEngineProvider).includes(settings[ekn].provider);
        });

        if (!hasCompatibleProvider) {
          this.toastrService.show(
            this.translocoService.translate('rag.rag-settings.incompatible_provider_error'),
            this.translocoService.translate('rag.rag-settings.import_failed_title'),
            {
              duration: 6000,
              status: 'danger'
            }
          );
          return;
        }

        this.initForm(settings);
        this.form.markAsDirty();
        this.importedSettings = true;

        this.closeImportModal();
      });
    }
  }

  confirmSettingsDeletion(): void {
    const confirmAction = this.translocoService.translate('rag.rag-settings.delete_action');
    const cancelAction = this.translocoService.translate('rag.rag-settings.cancel_action');

    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: this.translocoService.translate('rag.rag-settings.delete_settings_title'),
        subtitle: this.translocoService.translate('rag.rag-settings.delete_confirmation_message'),
        modalStatus: 'danger',
        actions: [
          { actionName: cancelAction, buttonStatus: 'basic' },
          { actionName: confirmAction, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result?.toLowerCase() === confirmAction.toLowerCase()) {
        this.deleteSettings();
      }
    });
  }

  deleteSettings(): void {
    if (this.vectorStoreUnavailable) return;
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/rag`;
    this.rest.delete<boolean>(url).subscribe(() => {
      delete this.settingsBackup;
      this.form.reset();
      this.form.markAsPristine();
      this.toastrService.success(
        this.translocoService.translate('rag.rag-settings.settings_deleted_success'),
        this.translocoService.translate('rag.rag-settings.success_title'),
        {
          duration: 5000,
          status: 'success'
        }
      );
    });
  }

  // ─── Deactivation guard ─────────────────────────────────────────────────────────────────

  canDeactivate(): Observable<boolean> | boolean {
    if (!this.form.dirty) return true;

    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      closeOnBackdropClick: false,
      context: {
        title: this.translocoService.translate('rag.rag-settings.unsaved_changes_title'),
        subtitle: this.translocoService.translate('rag.rag-settings.unsaved_changes_message'),
        actions: [
          {
            actionName: this.translocoService.translate('rag.rag-settings.stay_on_page'),
            buttonStatus: 'basic',
            ghost: true,
            returnValue: false
          },
          {
            actionName: this.translocoService.translate('rag.rag-settings.leave_without_saving'),
            buttonStatus: 'danger',
            returnValue: true
          }
        ],
        modalStatus: 'danger'
      }
    });

    return from(dialogRef.onClose) as Observable<boolean>;
  }

  ngOnDestroy(): void {
    this.dirtyState.unregister();
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
