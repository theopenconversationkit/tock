import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { RagAnswerStatusLabels, getExportFileName, readFileAsText } from '../../shared/utils';
import { StateService } from '../../core-nlp/state.service';
import { NbDialogRef, NbDialogService, NbToastrService } from '@nebular/theme';
import { saveAs } from 'file-saver-es';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { FileValidators } from '../../shared/validators';
import { Observable, Subject, from, of, takeUntil } from 'rxjs';
import { RestService } from '../../core-nlp/rest/rest.service';
import { CanComponentDeactivate } from './prompt-context-settings.guard';
import { ChoiceDialogComponent } from '../../shared/components';
import { BotConfigurationService } from '../../core/bot-configuration.service';

interface LexiconGroup {
  id: number;
  terms: string[];
}

interface PromptContext {
  coveredTopics: string[];
  excludedTopics: string[];
  lexiconGroups: LexiconGroup[];
}

@Component({
  selector: 'tock-prompt-context-settings',
  templateUrl: './prompt-context-settings.component.html',
  styleUrls: ['./prompt-context-settings.component.scss']
})
export class PromptContextSettingsComponent implements OnInit, CanComponentDeactivate, OnDestroy {
  destroy$: Subject<unknown> = new Subject();
  loading: boolean = false;
  isSaving = false;

  sections: Record<string, boolean> = {
    covered: true,
    excluded: true,
    lexicon: true
  };

  coveredTopics: string[] = [];
  excludedTopics: string[] = [];
  lexiconGroups: LexiconGroup[] = [];
  lexiconSearch = '';

  private _lexIdSeq = 10;
  private _snapshot: PromptContext | null = null;

  _coveredWarning: string | undefined;
  _excludedWarning: string | undefined;

  @ViewChild('importModal') importModal: TemplateRef<any>;

  constructor(
    private botConfiguration: BotConfigurationService,
    private state: StateService,
    private rest: RestService,
    private toastrService: NbToastrService,
    private nbDialogService: NbDialogService
  ) {}

  ngOnInit(): void {
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs) => {
      this.loadSettings();
    });
  }

  loadSettings() {
    this.loading = true;
    this.settingsLoader().subscribe((res) => {
      if (res?.coveredTopics) {
        this.coveredTopics = res.coveredTopics;
        this.excludedTopics = res.excludedTopics;
        this.lexiconGroups = res.lexiconGroups;
        this._takeSnapshot();
      } else {
        this.coveredTopics = [RagAnswerStatusLabels.small_talk];
        this.excludedTopics = [];
        this.lexiconGroups = [];
        this._takeSnapshot();
      }
      this.loading = false;
    });
  }

  private settingsLoader(): Observable<PromptContext> {
    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/business-rules`;
    return this.rest.get<PromptContext>(url, (settings: PromptContext) => settings);
  }

  // ─── Validation ──────────────────────────────────────────────────────────────

  get invalidLexiconGroups(): LexiconGroup[] {
    return this.lexiconGroups.filter((g) => g.terms.length < 2);
  }

  get isValid(): boolean {
    return this.invalidLexiconGroups.length === 0;
  }

  // ─── Dirty state ─────────────────────────────────────────────────────────────

  get isDirty(): boolean {
    if (!this._snapshot) return false;
    return JSON.stringify(this._currentState()) !== JSON.stringify(this._snapshot);
  }

  private _currentState(): PromptContext {
    if (!this.coveredTopics) {
      return {
        coveredTopics: [],
        excludedTopics: [],
        lexiconGroups: []
      };
    }

    return {
      coveredTopics: [...this.coveredTopics],
      excludedTopics: [...this.excludedTopics],
      // Deep clone so nested arrays don't share references with the snapshot
      lexiconGroups: this.lexiconGroups.map((g) => ({ id: g.id, terms: [...g.terms] }))
    };
  }

  private _takeSnapshot(): void {
    this._snapshot = this._currentState();
  }

  // ─── Sections ────────────────────────────────────────────────────────────────

  toggleSection(key: string): void {
    this.sections[key] = !this.sections[key];
  }

  // ─── Topics (covered / excluded) ─────────────────────────────────────────────

  isTopicRemovable(topic: string): boolean {
    if (topic === RagAnswerStatusLabels.small_talk) return false;

    return true;
  }

  addTag(key: 'covered' | 'excluded', inputEl: HTMLInputElement): void {
    this._coveredWarning = undefined;
    this._excludedWarning = undefined;

    const value = inputEl.value.trim();
    if (!value) return;

    const list = key === 'covered' ? this.coveredTopics : this.excludedTopics;
    const otherList = key === 'covered' ? this.excludedTopics : this.coveredTopics;
    const normalizedValue = value.toLowerCase().replace(/\s+/g, '');

    const isInList = (lst: string[]) => lst.some((item) => item.toLowerCase().replace(/\s+/g, '') === normalizedValue);

    if (!isInList(list) && !isInList(otherList)) {
      list.push(value);
    } else {
      if (isInList(list)) {
        if (key === 'covered') this._coveredWarning = 'This topic is already listed';
        if (key === 'excluded') this._excludedWarning = 'This topic is already listed';
      }

      if (isInList(otherList)) {
        if (key === 'covered')
          this._coveredWarning = 'An excluded topic with the same name already exists. A topic cannot be both covered and excluded';
        if (key === 'excluded')
          this._excludedWarning = 'A covered topic with the same name already exists. A topic cannot be both covered and excluded';
      }

      setTimeout(() => {
        this._coveredWarning = undefined;
        this._excludedWarning = undefined;
      }, 3000);
    }

    inputEl.value = '';
  }

  removeTag(key: 'covered' | 'excluded', tag: { text: string }): void {
    const list = key === 'covered' ? this.coveredTopics : this.excludedTopics;
    const idx = list.indexOf(tag.text);
    if (idx !== -1) list.splice(idx, 1);
  }

  // ─── Lexicon groups ───────────────────────────────────────────────────────────

  get filteredLexiconGroups(): LexiconGroup[] {
    const q = this.lexiconSearch.toLowerCase().trim();
    if (!q) return this.lexiconGroups;
    return this.lexiconGroups.filter((g) => g.terms.some((t) => t.toLowerCase().includes(q)));
  }

  getLexLabel(group: LexiconGroup): string | null {
    const nonEmpty = group.terms.filter((t) => t.trim());
    if (!nonEmpty.length) return null;
    if (nonEmpty.length === 1) return nonEmpty[0];
    return `${nonEmpty[0]} / ${nonEmpty[1]}`;
  }

  addLexGroup(): void {
    this.lexiconGroups.unshift({ id: this._lexIdSeq++, terms: [] });
    this.lexiconSearch = '';
  }

  deleteLexGroup(id: number): void {
    this.lexiconGroups = this.lexiconGroups.filter((g) => g.id !== id);
  }

  addTerm(groupId: number, inputEl: HTMLInputElement): void {
    const value = inputEl.value?.trim();
    if (!value) return;
    const group = this.lexiconGroups.find((g) => g.id === groupId);
    if (group && !group.terms.includes(value)) group.terms.push(value);
    inputEl.value = '';
  }

  removeTerm(groupId: number, tag: { text: string }): void {
    const group = this.lexiconGroups.find((g) => g.id === groupId);
    if (!group) return;
    const idx = group.terms.indexOf(tag.text);
    if (idx !== -1) group.terms.splice(idx, 1);
  }

  // ─── Tracking ────────────────────────────────────────────────────────────────

  trackById(_: number, item: LexiconGroup): number {
    return item.id;
  }

  // ─── Export ─────────────────────────────────────────────────────────────────

  get hasExportableData(): boolean {
    const currentState = this._currentState();
    return currentState.coveredTopics.length > 0 || currentState.excludedTopics.length > 0 || currentState.lexiconGroups.length > 0;
  }

  exportSettings() {
    const currentState = this._currentState();

    const jsonBlob = new Blob([JSON.stringify(currentState)], {
      type: 'application/json'
    });

    const exportFileName = getExportFileName(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      'Rag prompt context settings',
      'json'
    );

    saveAs(jsonBlob, exportFileName);

    this.toastrService.show(`Rag prompt context settings dump provided`, 'Rag prompt context settings dump', {
      duration: 3000,
      status: 'success'
    });
  }

  // ─── Import ─────────────────────────────────────────────────────────────────

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

        if (!settings.coveredTopics?.length && !settings.excludedTopics?.length && !settings.lexiconGroups?.length) {
          this.toastrService.show(
            `The file provided does not contain the expected data. Please check the file.`,
            'Rag prompt context import fails',
            {
              duration: 6000,
              status: 'danger'
            }
          );
          return;
        }

        if (settings.coveredTopics?.length) this.coveredTopics = settings.coveredTopics;
        if (settings.excludedTopics?.length) this.excludedTopics = settings.excludedTopics;
        if (settings.lexiconGroups?.length) this.lexiconGroups = settings.lexiconGroups;

        this.closeImportModal();
      });
    }
  }

  // ─── Actions ─────────────────────────────────────────────────────────────────

  cancel(): void {
    if (!this._snapshot) return;
    this.coveredTopics = [...this._snapshot.coveredTopics];
    this.excludedTopics = [...this._snapshot.excludedTopics];
    this.lexiconGroups = this._snapshot.lexiconGroups.map((g) => ({ id: g.id, terms: [...g.terms] }));
    this.lexiconSearch = '';
  }

  save(): void {
    if (!this.isValid) return;

    this.isSaving = true;

    const payload = this._currentState();

    const url = `/gen-ai/bots/${this.state.currentApplication.name}/configuration/business-rules`;

    this.rest
      .post(url, payload, (settings: PromptContext) => settings, null, true)
      .subscribe({
        next: () => {
          this._takeSnapshot();
          this.isSaving = false;
        },
        error: () => {
          this.isSaving = false;
        }
      });
  }

  // ─── Deactivation guard ─────────────────────────────────────────────────────────────────

  canDeactivate(): Observable<boolean> | boolean {
    if (!this.isDirty) return true;

    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: 'Unsaved changes',
        subtitle: 'You have unsaved changes. What would you like to do?',
        actions: [
          { actionName: 'Stay on page', buttonStatus: 'basic', ghost: true, returnValue: false },
          { actionName: 'Leave without saving', buttonStatus: 'danger', returnValue: true }
        ],
        modalStatus: 'danger'
      }
    });

    return from(dialogRef.onClose) as Observable<boolean>;
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
