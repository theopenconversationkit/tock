import { Component, Injectable, Input, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { Scenario, SCENARIO_ITEM_FROM_BOT, SCENARIO_ITEM_FROM_CLIENT } from '../../models';

@Component({
  selector: 'scenario-publishing',
  templateUrl: './scenario-publishing.component.html',
  styleUrls: ['./scenario-publishing.component.scss']
})
export class ScenarioPublishingComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @Input() scenario: Scenario;

  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;

  tickStoryJson: string;
  ngOnInit(): void {
    this.tickStoryJson = this.compileTickStory();
  }

  compileTickStory(): string {
    let tickStory = {
      name: this.scenario.name,
      sagaId: 321658,
      primaryIntents: ['62bb118e49e78735af27aa98'],
      secondaryIntents: ['65sd99ze1sd6ert6df21se89', 'df5d58ze54ds875q45sdf89'],
      tickContexts: this.scenario.data.contexts,
      tickActions: [],
      stateMachine: this.scenario.data.stateMachine
    };

    this.scenario.data.scenarioItems.forEach((item) => {
      if (item.from == SCENARIO_ITEM_FROM_BOT && item.tickActionDefinition) {
        tickStory.tickActions.push(item.tickActionDefinition);
      }
    });

    console.log(tickStory);
    return JSON.stringify(tickStory, null, 4);
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}
