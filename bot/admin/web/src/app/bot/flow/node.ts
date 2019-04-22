import {DialogFlowStateData, DialogFlowStateTransitionData, DialogFlowStateTransitionType} from "../model/flow";

export class StoryNode {

  public dynamic: boolean;
  public count: number;

  constructor(
    public storyDefinitionId: string,
    public states: DialogFlowStateData[],
    public id: number,
    public entities: string[],
    public intent?: string,
    public step?: string
  ) {
    this.dynamic = storyDefinitionId.match(/^[0-9a-fA-F]{24}$/) != null;
    this.count = this.states.reduce((sum, s) => sum + s.count, 0);
  }

  displayCount(): string {
    if (this.count < 1000) {
      return this.count.toString();
    } else {
      return Math.floor(this.count / 1000) + "k"
    }
  }

  displayName(): string {
    const sId = this.storyDefinitionId;
    const i = this.states[0].intent ? this.states[0].intent : this.intent;
    return sId === 'tock_unknown_story' ? 'unknown' :
      ((sId && i && sId.match(/^[0-9a-fA-F]{24}$/)) ? i : sId);
  }

  nodeName(): string {
    const nodeName = this.displayName();
    const i = this.intent;
    return nodeName
      + (!i || i === nodeName || this.storyDefinitionId === 'tock_unknown_story' ? "" : "/" + i)
      + (this.step ? "*" + this.step : "")
  }

}

export class NodeTransition {

  public count: number;

  constructor(
    public transitions: DialogFlowStateTransitionData[],
    public previousId: number,
    public nextId: number,
    public type: DialogFlowStateTransitionType
  ) {
    this.count = this.transitions.reduce((sum, s) => sum + s.count, 0);
  }
}
