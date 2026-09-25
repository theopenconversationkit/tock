import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { Subject, take } from 'rxjs';
import { RuleType, StoryDefinitionConfiguration, StoryFeature } from '../../../model/story';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { CreateRuleComponent } from '../create-rule/create-rule.component';
import { ChoiceDialogComponent } from '../../../../shared/components';
import { BotService } from '../../../bot-service';
import { getStoryIcon } from '../../../../shared/utils';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'tock-story-rules-table',
    templateUrl: './story-rules-table.component.html',
    styleUrls: ['./story-rules-table.component.scss'],
    standalone: false
})
export class StoryRulesTableComponent implements OnChanges, OnDestroy {
  destroy = new Subject();

  ruleType = RuleType;

  filteredFeatures: StoryFeature[] = [];

  searchFilterString: string;

  getStoryIcon = getStoryIcon;

  @Input() type: RuleType;

  @Input() features: StoryFeature[] = [];

  @Output() onRefresh = new EventEmitter<boolean>();

  constructor(
    private nbDialogService: NbDialogService,
    private botService: BotService,
    private toastrService: NbToastrService,
    private transloco: TranslocoService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.features?.currentValue) {
      this.resetSearch();
    }
  }

  hasTarget(): boolean {
    return [RuleType.Redirection, RuleType.Ending].includes(this.type);
  }

  newRule(): void {
    this.nbDialogService
      .open(CreateRuleComponent, {
        context: {
          type: this.type
        }
      })
      .componentRef.instance.onSave.pipe(take(1))
      .subscribe((res) => {
        this.onRefresh.emit(true);
      });
  }

  toggle(feature: StoryFeature, newState: boolean): void {
    feature.enabled = newState;
    this.botService.saveStory(feature.story).subscribe(() => {
      const status = newState
        ? this.transloco.translate('bot.story-rule-table.activated')
        : this.transloco.translate('bot.story-rule-table.deactivated');
      this.toastrService.show(
        this.transloco.translate('bot.story-rule-table.ruleToggledMessage', {
          ruleType: this.type,
          status: status
        }),
        this.transloco.translate('bot.story-rule-table.ruleUpdateTitle'),
        { duration: 3000 }
      );
    });
  }

  askDeleteFeature(feature: StoryFeature): void {
    const confirmAction = this.transloco.translate('common.actions.delete');
    const cancelAction = this.transloco.translate('common.actions.cancel');

    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('bot.story-rule-table.deleteRuleTitle', {
          ruleType: this.type
        }),
        subtitle: this.transloco.translate('bot.story-rule-table.deleteRuleSubtitle', {
          ruleType: this.type,
          storyName: feature.story.name
        }),
        modalStatus: 'danger',
        actions: [
          { actionName: cancelAction, buttonStatus: 'basic' },
          { actionName: confirmAction, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result?.toLowerCase() === confirmAction.toLowerCase()) {
        this.deleteFeature(feature);
      }
    });
  }

  deleteFeature(feature: StoryFeature): void {
    const story = feature.story;

    story.features.splice(story.features.indexOf(feature), 1);

    this.botService.saveStory(story).subscribe(() => {
      this.onRefresh.emit(true);
      this.toastrService.show(
        this.transloco.translate('bot.story-rule-table.ruleToggledMessage', {
          ruleType: this.type,
          status: this.transloco.translate('bot.story-rule-table.deleted')
        }),
        this.transloco.translate('bot.story-rule-table.ruleDeletionTitle'),
        { duration: 3000 }
      );
    });
  }

  filterFeatures(): void {
    this.filteredFeatures = this.features.filter((feature) => {
      return feature.story.name.toLowerCase().trim().includes(this.searchFilterString.toLowerCase().trim());
    });
  }

  resetSearch(): void {
    this.searchFilterString = undefined;
    this.filteredFeatures = this.features;
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
