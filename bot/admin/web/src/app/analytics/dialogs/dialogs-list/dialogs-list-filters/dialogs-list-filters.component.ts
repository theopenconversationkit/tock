import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ConnectorType } from '../../../../core/model/configuration';
import { Subject, debounceTime, take, takeUntil } from 'rxjs';
import { ExtractFormControlTyping } from '../../../../shared/utils/typescript.utils';
import { BotSharedService } from '../../../../shared/bot-shared.service';
import { StateService } from '../../../../core-nlp/state.service';
import { BotConfigurationService } from '../../../../core/bot-configuration.service';

interface DialogListFiltersForm {
  exactMatch: FormControl<boolean>;
  displayTests: FormControl<boolean>;
  dialogId?: FormControl<string>;
  text?: FormControl<string>;
  intentName?: FormControl<string>;
  connectorType?: FormControl<ConnectorType>;
  ratings?: FormControl<number[]>;
  configuration?: FormControl<string>;
  intentsToHide?: FormControl<string[]>;
  isGenAiRagDialog?: FormControl<boolean>;
}

export type DialogListFilters = ExtractFormControlTyping<DialogListFiltersForm>;

@Component({
  selector: 'tock-dialogs-list-filters',
  templateUrl: './dialogs-list-filters.component.html',
  styleUrl: './dialogs-list-filters.component.scss'
})
export class DialogsListFiltersComponent implements OnInit {
  private readonly destroy$: Subject<boolean> = new Subject();

  advanced: boolean = false;
  connectorTypes: ConnectorType[] = [];
  configurationNameList: string[];

  @Input() initialFilters: Partial<DialogListFilters>;
  @Output() onFilter = new EventEmitter<Partial<DialogListFilters>>();

  constructor(public botSharedService: BotSharedService, public state: StateService, private botConfiguration: BotConfigurationService) {}

  ngOnInit() {
    this.botSharedService
      .getConnectorTypes()
      .pipe(take(1))
      .subscribe((conf) => {
        this.connectorTypes = conf.map((it) => it.connectorType);
      });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((configs) => {
      this.configurationNameList = configs
        .filter((item) => item.targetConfigurationId == null)
        .map((item) => {
          return item.applicationId;
        });
    });

    if (this.initialFilters) {
      this.form.patchValue(this.initialFilters);
    }

    this.form.valueChanges.pipe(debounceTime(500), takeUntil(this.destroy$)).subscribe(() => this.submitFiltersChange());
  }

  form = new FormGroup<DialogListFiltersForm>({
    exactMatch: new FormControl(),
    displayTests: new FormControl(),
    dialogId: new FormControl(),
    text: new FormControl(),
    intentName: new FormControl(),
    connectorType: new FormControl(),
    ratings: new FormControl(),
    configuration: new FormControl(),
    intentsToHide: new FormControl([]),
    isGenAiRagDialog: new FormControl()
  });

  getFormControl(formControlName: string): FormControl {
    return this.form.get(formControlName) as FormControl;
  }

  submitFiltersChange(): void {
    const formValue = this.form.value;

    this.onFilter.emit(formValue);
  }

  resetControl(ctrl: FormControl, input?: HTMLInputElement): void {
    ctrl.reset();
    if (input) {
      input.value = '';
    }
  }

  patchControl(ctrl: FormControl, value: any): void {
    ctrl.patchValue(value);
  }

  swapAdvanced(): void {
    this.advanced = !this.advanced;
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
