import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Location } from '@angular/common';
import { FormControl, FormGroup } from '@angular/forms';
import { Observable, Subject, of } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { SentenceTrainingMode } from './../models';
import { Options } from '@angular-slider/ngx-slider';
import { EntityDefinition, EntityType, Intent, IntentsCategory, SearchQuery, SentenceStatus, getRoles } from '../../../../model/nlp';
import { StateService } from '../../../../core-nlp/state.service';
import { NlpService } from '../../../../core-nlp/nlp.service';
import { UserRole } from '../../../../model/auth';

interface SentenceTrainingFilterForm {
  search: FormControl<string>;
  status: FormControl<SentenceStatus | ''>;
  onlyToReview: FormControl<boolean>;
  intent: FormControl<Intent>;
  entityType: FormControl<string>;
  searchSubEntities: FormControl<boolean>;
  entityRolesToInclude: FormControl<string[]>;
  entityRolesToExclude: FormControl<string[]>;
  modifiedBefore: FormControl<Date>;
  modifiedAfter: FormControl<Date>;
  intentProbability: FormControl<number[]>;
  user: FormControl<string>;
  allButUser: FormControl<string>;
  configuration: FormControl<string>;
}

@Component({
  selector: 'tock-sentence-training-filters',
  templateUrl: './sentence-training-filters.component.html',
  styleUrls: ['./sentence-training-filters.component.scss']
})
export class SentenceTrainingFiltersComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() sentenceTrainingMode: SentenceTrainingMode;

  @Output() onFilter = new EventEmitter<Partial<SearchQuery>>();

  @Output() onChangeIntent = new EventEmitter<string>();

  @Output() onChangeEntity = new EventEmitter<{ old: EntityDefinition; new: EntityDefinition }>();

  @Output() onTranslateSentences = new EventEmitter<string>();

  SentenceTrainingModes = SentenceTrainingMode;

  userRoles = UserRole;

  advanced: boolean = false;

  advancedTools: boolean = false;

  sentenceStatus = SentenceStatus;

  users: string[];

  configurations: string[];

  filteredFilterIntentsGroups$: Observable<IntentsCategory[]>;

  filteredSwapIntentsGroups$: Observable<IntentsCategory[]>;

  entityTypes: EntityType[];

  entityRolesToIncludeList: string[];

  entityRolesToExcludeList: string[];

  unknownIntent: Partial<Intent> = {
    label: 'Unknown',
    _id: Intent.unknown,
    entities: []
  };

  ragExcludedIntent: Partial<Intent> = {
    label: 'RAG excluded',
    _id: Intent.ragExcluded,
    entities: []
  };

  changeIntent: Intent;

  swapEntitiesOrigin: EntityDefinition;

  swapEntitiesTarget: EntityDefinition;

  translateTargetLocale: string;

  constructor(public state: StateService, private nlp: NlpService, private location: Location) {
    const search = (this.location.getState() as any)?.searchIntent;
    if (search) {
      this.form.patchValue({
        search: search
      });
      setTimeout(() => {
        this.submitFiltersChange();
      }, 500);
    }
  }

  form = new FormGroup<SentenceTrainingFilterForm>({
    search: new FormControl(),
    status: new FormControl(''),
    onlyToReview: new FormControl(),
    intent: new FormControl(),
    entityType: new FormControl(),
    searchSubEntities: new FormControl(),
    entityRolesToInclude: new FormControl([]),
    entityRolesToExclude: new FormControl([]),
    modifiedBefore: new FormControl(),
    modifiedAfter: new FormControl(),
    intentProbability: new FormControl([0, 100]),
    user: new FormControl(),
    allButUser: new FormControl(),
    configuration: new FormControl()
  });

  ngOnInit(): void {
    this.nlp.findUsers(this.state.currentApplication).subscribe((users) => {
      this.users = users;
    });

    this.nlp.findConfigurations(this.state.currentApplication).subscribe((configurations) => {
      this.configurations = configurations;
    });

    this.getFormControl('onlyToReview')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((state: boolean) => {
        if (state) this.form.patchValue({ status: '' });
      });

    this.getFormControl('status')
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((status: SentenceStatus | '') => {
        if (status !== '') this.form.patchValue({ onlyToReview: false });
      });

    ['intent', 'entityType', 'entityRolesToInclude', 'entityRolesToExclude'].forEach((formName: string) => {
      this.getFormControl(formName)
        .valueChanges.pipe(takeUntil(this.destroy$))
        .subscribe(() => {
          this.updateEntitiesFilters();
        });
    });

    this.updateEntitiesFilters();

    this.form.valueChanges.pipe(debounceTime(500), takeUntil(this.destroy$)).subscribe(() => this.submitFiltersChange());
  }

  submitFiltersChange(): void {
    const formValue = this.form.value;

    this.onFilter.emit({
      search: formValue.search,
      status: formValue.status !== '' ? [formValue.status] : [],
      onlyToReview: formValue.onlyToReview,
      intentId: formValue.intent?._id,
      entityType: formValue.entityType,
      searchSubEntities: formValue.searchSubEntities,
      entityRolesToInclude: formValue.entityRolesToInclude,
      entityRolesToExclude: formValue.entityRolesToExclude,
      modifiedBefore: formValue.modifiedBefore,
      modifiedAfter: formValue.modifiedAfter,
      minIntentProbability: formValue.intentProbability[0],
      maxIntentProbability: formValue.intentProbability[1],
      user: formValue.user,
      allButUser: formValue.allButUser,
      configuration: formValue.configuration
    });
  }

  getFormControl(formControlName: string): FormControl {
    return this.form.get(formControlName) as FormControl;
  }

  resetControl(ctrl: FormControl, input?: HTMLInputElement): void {
    ctrl.reset();
    if (input) {
      input.value = '';
    }
  }

  swapAdvanced(): void {
    this.advanced = !this.advanced;
  }

  get currentIntentsCategories(): IntentsCategory[] {
    return this.state.currentIntentsCategories.getValue();
  }

  filterIntentsList(which: string, event: KeyboardEvent): void {
    if (['ArrowDown', 'ArrowUp', 'Escape'].includes(event.key)) return;

    let str = (event.target as HTMLInputElement).value.toLowerCase();
    let result: IntentsCategory[] = [];
    this.currentIntentsCategories.forEach((group) => {
      group.intents.forEach((intent) => {
        if (intent.label?.toLowerCase().includes(str) || intent.name?.toLowerCase().includes(str)) {
          let cat = result.find((cat) => cat.category == group.category);
          if (!cat) {
            cat = { category: group.category, intents: [] };
            result.push(cat);
          }
          cat.intents.push(intent);
        }
      });
    });

    if (which === 'filter') this.filteredFilterIntentsGroups$ = of(result);

    if (which === 'swap') this.filteredSwapIntentsGroups$ = of(result);
  }

  setIntentsListFilter(which: string): void {
    if (which === 'filter') this.filteredFilterIntentsGroups$ = of(this.currentIntentsCategories);

    if (which === 'swap') this.filteredSwapIntentsGroups$ = of(this.currentIntentsCategories);
  }

  onFocusIntentsInput(which: string): void {
    this.setIntentsListFilter(which);
  }

  onBlurIntentsInput(which: string, event: KeyboardEvent | Event): void {
    if (which === 'filter') {
      if (!this.getFormControl('intent').value) (event.target as HTMLInputElement).value = '';
      else {
        (event.target as HTMLInputElement).value = this.getFormControl('intent').value.label || this.getFormControl('intent').value.name;
      }
    }

    if (which === 'swap') {
      if (!this.isIntentSelected()) {
        (event.target as HTMLInputElement).value = '';
      }
    }
  }

  intentsAutocompleteViewHandle(stringOrIntent: string | Intent): string {
    if (typeof stringOrIntent === 'object') {
      return stringOrIntent?.label || stringOrIntent?.name || '';
    } else {
      return stringOrIntent;
    }
  }

  selectIntent(intent: Intent): void {
    this.getFormControl('intent').patchValue(intent);
  }

  private updateEntitiesFilters(): void {
    this.state.entityTypesSortedByName().subscribe((entities) => {
      if (!this.getFormControl('intent').value) {
        this.entityTypes = entities;
        const roles = getRoles(this.state.currentIntents.value, entities, this.getFormControl('entityType').value);
        this.entityRolesToIncludeList = roles;
        this.entityRolesToExcludeList = roles;
      } else {
        const intent = this.getFormControl('intent').value;
        if (intent) {
          this.entityTypes = this.findEntitiesAndSubEntities(entities, intent);
          const roles = getRoles([intent], entities, this.getFormControl('entityType').value);
          this.entityRolesToIncludeList = roles;
          this.entityRolesToExcludeList = roles;
        } else {
          this.entityTypes = [];
          this.entityRolesToIncludeList = [];
          this.entityRolesToExcludeList = [];
        }
      }

      let shouldSearchSubentities = false;

      if (this.getFormControl('entityType').value) {
        const entity = entities.find((e) => e.name === this.getFormControl('entityType').value);
        const hasSubentities = entity && entity.allSuperEntities(entities, new Set()).size !== 0;

        if (hasSubentities) shouldSearchSubentities = true;
      }

      if (this.getFormControl('entityRolesToInclude').value?.length) {
        const hasSubentities =
          entities.find(
            (e) => e.subEntities.find((s) => this.getFormControl('entityRolesToInclude').value.includes(s.role)) != undefined
          ) != undefined;

        if (hasSubentities) {
          shouldSearchSubentities = true;
        }
      }

      if (this.getFormControl('entityRolesToExclude').value?.length) {
        const hasSubentities =
          entities.find(
            (e) => e.subEntities.find((s) => this.getFormControl('entityRolesToExclude').value.includes(s.role)) != undefined
          ) != undefined;

        if (hasSubentities) shouldSearchSubentities = true;
      }

      if (shouldSearchSubentities && !this.getFormControl('searchSubEntities').value) {
        this.getFormControl('searchSubEntities').patchValue(true);
      }
      if (!shouldSearchSubentities && this.getFormControl('searchSubEntities').value) {
        this.getFormControl('searchSubEntities').patchValue(false);
      }
    });
  }

  private findEntitiesAndSubEntities(entities: EntityType[], intent: Intent): EntityType[] {
    return entities.filter((e) =>
      intent.entities.some((intentEntity) => intentEntity.entityTypeName === e.name || e.containsSuperEntity(intentEntity, entities))
    );
  }

  updateFilter(filter): void {
    this.form.patchValue({
      search: filter.text
    });
  }

  intentProbaSliderOptions: Options = {
    floor: 0,
    ceil: 100,
    step: 1,
    noSwitching: true,
    translate: (value: number): string => {
      return `${value}%`;
    }
  };

  swapAdvancedTools(): void {
    this.advancedTools = !this.advancedTools;
  }

  isIntentSelected(): boolean {
    return this.changeIntent instanceof Intent;
  }

  applyChangeIntent(): void {
    this.onChangeIntent.emit(this.changeIntent._id);
  }

  applyChangeEntity(): void {
    this.onChangeEntity.emit({
      old: this.swapEntitiesOrigin,
      new: this.swapEntitiesTarget
    });
  }

  translateSentences(): void {
    this.onTranslateSentences.emit(this.translateTargetLocale);
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
