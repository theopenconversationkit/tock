import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { ScenarioStateGroupComponent } from './state-group/state-group.component';
import { ScenarioTransitionComponent } from './state-group/transition/transition.component';

@Injectable({
  providedIn: 'root'
})
export class ScenarioProductionService {
  public scenarioProductionItemsCommunication = new Subject<any>();
  public scenarioProductionTransitionsComponents = {};
  public scenarioProductionStateComponents = {};

  unRegisterTransitionComponent(name: string) {
    delete this.scenarioProductionTransitionsComponents[name];
  }
  registerTransitionComponent(component: ScenarioTransitionComponent) {
    this.scenarioProductionTransitionsComponents[component.transition.name] = component;
  }

  unRegisterStateComponent(name: string) {
    delete this.scenarioProductionStateComponents[name];
  }
  registerStateComponent(component: ScenarioStateGroupComponent) {
    this.scenarioProductionStateComponents[component.state.id] = component;
  }

  redrawPaths() {
    this.scenarioProductionItemsCommunication.next({
      type: 'redrawPaths'
    });
  }
  redrawActions() {
    this.scenarioProductionItemsCommunication.next({
      type: 'redrawActions'
    });
  }
  redrawIntents() {
    this.scenarioProductionItemsCommunication.next({
      type: 'redrawIntents'
    });
  }
  updateLayout() {
    this.redrawActions();
    setTimeout(() => {
      this.redrawIntents();
      setTimeout(() => {
        this.redrawPaths();
      }, 100);
    }, 100);
  }

  itemDropped(stateId: string, dropped: object): void {
    this.scenarioProductionItemsCommunication.next({
      type: 'itemDropped',
      stateId: stateId,
      dropped: dropped
    });
  }

  addStateGroup(stateId: string, groupName: string): void {
    this.scenarioProductionItemsCommunication.next({
      type: 'addStateGroup',
      stateId: stateId,
      groupName: groupName
    });
  }
  removeState(stateId: string): void {
    this.scenarioProductionItemsCommunication.next({
      type: 'removeState',
      stateId: stateId
    });
  }
}
