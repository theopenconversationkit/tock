import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

import { ScenarioStateGroupComponent } from './state-group/state-group.component';
import { ScenarioTransitionComponent } from './state-group/transition/transition.component';

@Injectable({
  providedIn: 'root'
})
export class ScenarioProductionService {
  public scenarioProductionItemsCommunication = new Subject<any>();
  public scenarioProductionTransitionsComponents: ScenarioTransitionComponent[] = [];
  public scenarioProductionStateComponents: { [key: string]: ScenarioStateGroupComponent } = {};

  registerTransitionComponent(component: ScenarioTransitionComponent): void {
    this.scenarioProductionTransitionsComponents.push(component);
  }

  unRegisterTransitionComponent(name: string): void {
    this.scenarioProductionTransitionsComponents = this.scenarioProductionTransitionsComponents.filter(
      (entry) => entry.transition.name !== name
    );
  }

  registerStateComponent(component: ScenarioStateGroupComponent): void {
    this.scenarioProductionStateComponents[component.state.id] = component;
  }

  unRegisterStateComponent(name: string): void {
    delete this.scenarioProductionStateComponents[name];
  }

  redrawPaths(): void {
    this.scenarioProductionItemsCommunication.next({
      type: 'redrawPaths'
    });
  }

  private redrawActions(): void {
    this.scenarioProductionItemsCommunication.next({
      type: 'redrawActions'
    });
  }

  private redrawIntents(): void {
    this.scenarioProductionItemsCommunication.next({
      type: 'redrawIntents'
    });
  }

  updateLayout(): void {
    setTimeout(() => {
      this.redrawActions();
      setTimeout(() => {
        this.redrawIntents();
        setTimeout(() => {
          this.redrawPaths();
        });
      });
    });
  }

  itemDropped(stateId: string, dropped: object): void {
    this.scenarioProductionItemsCommunication.next({
      type: 'itemDropped',
      stateId,
      dropped
    });
  }

  addStateGroup(stateId: string, groupName: string): void {
    this.scenarioProductionItemsCommunication.next({
      type: 'addStateGroup',
      stateId,
      groupName
    });
  }

  removeState(stateId: string): void {
    this.scenarioProductionItemsCommunication.next({
      type: 'removeState',
      stateId
    });
  }
}
