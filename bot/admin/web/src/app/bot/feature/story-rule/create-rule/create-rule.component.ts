import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { RuleType, StoryDefinitionConfiguration, StoryFeature, StorySearchQuery } from '../../../model/story';
import { NbDialogRef } from '@nebular/theme';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { BotService } from '../../../bot-service';
import { StateService } from '../../../../core-nlp/state.service';
import { Observable, of, take } from 'rxjs';
import { getStoryIcon } from '../../../../shared/utils';

enum mainOrTarget {
  mainStoryId = 'mainStoryId',
  targetStoryId = 'targetStoryId'
}

interface CreateRuleForm {
  mainStoryId: FormControl<string>;
  enabled: FormControl<boolean>;
  targetStoryId: FormControl<string>;
}

@Component({
  selector: 'tock-create-rule',
  templateUrl: './create-rule.component.html',
  styleUrls: ['./create-rule.component.scss']
})
export class CreateRuleComponent implements OnInit {
  loading: boolean = false;

  ruleType = RuleType;

  mainOrTarget = mainOrTarget;

  botApplicationConfigurationId: string;

  availableStories: StoryDefinitionConfiguration[];

  filteredStories$: Observable<StoryDefinitionConfiguration[]>;

  isSubmitted: boolean = false;

  getStoryIcon = getStoryIcon;

  @Input() type: RuleType;

  @Output() onSave = new EventEmitter();

  constructor(private nbDialogRef: NbDialogRef<CreateRuleComponent>, private state: StateService, private botService: BotService) {}

  ngOnInit(): void {
    this.loadStories();

    if (this.hasTarget()) {
      this.targetStoryId.addValidators(Validators.required);
    }
  }

  loadStories(): void {
    this.loading = true;

    this.botService
      .getStories(
        new StorySearchQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          0,
          10000
        )
      )
      .pipe(take(1))
      .subscribe((stories) => {
        this.availableStories = stories;
        this.loading = false;
      });
  }

  hasTarget(): boolean {
    return [RuleType.Redirection, RuleType.Ending].includes(this.type);
  }

  form = new FormGroup<CreateRuleForm>({
    mainStoryId: new FormControl(undefined, [Validators.required]),
    enabled: new FormControl(true),
    targetStoryId: new FormControl(undefined)
  });

  get mainStoryId(): FormControl {
    return this.form.get('mainStoryId') as FormControl;
  }

  get enabled(): FormControl {
    return this.form.get('enabled') as FormControl;
  }

  get targetStoryId(): FormControl {
    return this.form.get('targetStoryId') as FormControl;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  filterStoriesList(e: string): void {
    this.filteredStories$ = of(
      this.availableStories.filter((optionValue) => optionValue.name.toLowerCase().trim().includes(e.toLowerCase().trim()))
    );
  }

  getCurrentStoryLabel(wich: mainOrTarget): string {
    const currentStory = this.availableStories?.find((story) => story.storyId === this.form.get(wich as string).value);
    return currentStory?.name || '';
  }

  storyInputFocus(): void {
    this.filteredStories$ = of(this.availableStories);
  }

  storyInputBlur(wich: mainOrTarget, e: FocusEvent): void {
    setTimeout(() => {
      // timeout needed to avoid reseting input and filtered stories when clicking on autocomplete suggestions (which fires blur event)
      const target: HTMLInputElement = e.target as HTMLInputElement;
      target.value = this.getCurrentStoryLabel(wich);

      this.filteredStories$ = of(this.availableStories);
    }, 100);
  }

  onStorySelectionChange(wich: mainOrTarget, storyId: string): void {
    this.form.get(wich as string).patchValue(storyId);
    this.form.markAsDirty();
  }

  onStoryChange(wich: mainOrTarget, value: string): void {
    if (value?.trim() == '') {
      this.form.get(wich as string).patchValue(null);
      this.form.markAsDirty();
    }
  }

  save(): void {
    this.isSubmitted = true;

    if (this.canSave) {
      const newFeature = new StoryFeature(
        this.botApplicationConfigurationId,
        this.enabled.value,
        this.type === RuleType.Redirection ? this.targetStoryId.value : null,
        this.type === RuleType.Ending ? this.targetStoryId.value : null
      );

      newFeature.story = this.availableStories?.find((story) => story.storyId === this.mainStoryId.value);
      newFeature.story.features.push(newFeature);

      this.botService.saveStory(newFeature.story).subscribe((_) => {
        this.onSave.emit();
        this.cancel();
      });
    }
  }

  cancel(): void {
    this.nbDialogRef.close({});
  }
}
