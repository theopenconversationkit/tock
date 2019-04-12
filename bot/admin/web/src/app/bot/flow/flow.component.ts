/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, OnInit} from "@angular/core";
import {BotService} from "../bot-service";
import {NlpService} from "../../nlp-tabs/nlp.service";
import {StateService} from "../../core-nlp/state.service";
import {
  ApplicationDialogFlow,
  DialogFlowRequest,
  DialogFlowStateData,
  DialogFlowStateTransitionData,
  DialogFlowStateTransitionType
} from "../model/flow";
import {BotConfigurationService} from "../../core/bot-configuration.service";
import {entityColor} from "../../model/nlp";
import {KeyValue} from "@angular/common";

@Component({
  selector: 'tock-flow',
  templateUrl: './flow.component.html',
  styleUrls: ['./flow.component.css']
})
export class FlowComponent implements OnInit {

  layouts = [
    {
      name: 'cola',
      nodeDimensionsIncludeLabels: true,
      animate: true,
      flow: {axis: 'x', minSeparation: 30}
    },
    {
      name: 'dagre',
      rankDir: 'LR',
      directed: true,
      nodeDimensionsIncludeLabels: true,
      animate: true
    },
    {
      name: 'cose',
      rankDir: 'LR',
      nodeDimensionsIncludeLabels: true,
      animate: true
    },
    {
      name: 'cose-bilkent',
      rankDir: 'LR',
      nodeDimensionsIncludeLabels: true,
      animate: true
    },
    {
      name: 'elk',
      nodeDimensionsIncludeLabels: true,
      elk: {
        direction: 'RIGHT',
        edgeRouting: 'SPLINES',
      }
    },
    {
      name: 'grid',
      nodeDimensionsIncludeLabels: true,
      directed: true,
      animate: true,
      spacingFactor: 0.5
    },
    {
      name: 'circle',
      nodeDimensionsIncludeLabels: true,
      directed: true,
      animate: true,
      spacingFactor: 0.5
    },
    {
      name: 'concentric',
      nodeDimensionsIncludeLabels: true,
      directed: true,
      animate: true
    },
    {
      name: 'breadthfirst',
      nodeDimensionsIncludeLabels: true,
      padding: 10,
      directed: true,
      animate: true,
      maximal: false,
      grid: true,
      spacingFactor: 0.5
    }
  ];
  layout = this.layouts[0];
  selectedLayout = this.layout.name;
  recursive: boolean = false;
  entity: boolean = false;
  step: boolean = false;
  intent: boolean = false;
  minimalNodeCount: number = 0;
  maxNodeCount: number = 1;
  minimalTransitionPercentage: number = 10;

  selectedStoryId: string;
  displayOnlyNext: boolean = false;
  stories: Map<string, string> = new Map();

  selectedNode: DialogFlowStateData;

  graphData;

  botConfigurationId: string;
  lastBotId: string;
  flow: ApplicationDialogFlow;

  valueAscOrder = (a: KeyValue<string, string>, b: KeyValue<string, string>): number => {
    return a.value.localeCompare(b.value);
  }

  constructor(private nlp: NlpService,
              private state: StateService,
              private bot: BotService,
              private botConfiguration: BotConfigurationService) {
  }

  ngOnInit(): void {
  }

  changeLayout(layout: string) {
    this.layout = this.layouts.find(l => l.name === layout);
  }

  update() {
    setTimeout(_ => this.toGraphData(this.flow));
  }

  displayFlow(event: string) {
    this.botConfiguration.configurations.subscribe(c => {
      const all = event === "all";
      const conf = c.find(c => c._id === this.botConfigurationId);
      if (conf || all) {
        if (!all) {
          this.lastBotId = conf.botId;
        }
        this.bot.getApplicationFlow(
          new DialogFlowRequest(
            this.state.currentApplication.namespace,
            this.state.currentApplication.name,
            this.state.currentLocale,
            this.lastBotId,
            conf ? conf._id : null
          )
        ).subscribe(f => this.toGraphData(f));
      } else {
        this.graphData = null;
      }
    });
  }

  private toGraphData(flow: ApplicationDialogFlow) {
    this.flow = flow;

    //set all stories
    const stories = new Map<string, string>();
    flow.states.forEach(s => stories.set(s.storyDefinitionId, s.displayName()));
    this.stories = stories;

    const graph = {
      nodes: [],
      edges: []
    };

    const originalStatesById = new Map<string, DialogFlowStateData>();
    const states = [];
    const statesByKey = [];
    const oldStateIdNewStateIdMap = [];
    this.flow.states.forEach(s => {
      originalStatesById.set(s._id, s);
      if (this.entity && this.step && this.intent) {
        oldStateIdNewStateIdMap[s._id] = s._id;
        states.push(s);
      } else {
        let key = s.storyDefinitionId
          + (this.intent ? "+" + s.intent : "")
          + (this.step ? "+" + s.step : "")
          + (this.entity ? "+" + s.entities.join("%") : "");
        let state = statesByKey[key];
        if (!state) {
          state = DialogFlowStateData.fromJSON(s);
          if (!this.entity) {
            state.entities = [];
          }
          if (!this.intent) {
            state.intent = null;
          }
          if (!this.step) {
            state.step = null;
          }
          states.push(state);
          statesByKey[key] = state;
          oldStateIdNewStateIdMap[s._id] = s._id;
        } else {
          state.count += s.count;
          oldStateIdNewStateIdMap[s._id] = state._id;
        }
      }
    });
    let maxCount = 1;
    let finalStates = [];
    //1 filter state by count
    states.forEach(s => {
      if (this.minimalNodeCount <= s.count) {
        finalStates[s._id] = s;
      }
    });

    //count transitions
    let transitionTotal = [];
    let transitions = new Map<string, DialogFlowStateTransitionData>();
    this.flow.transitions.forEach(t => {
      let prev = oldStateIdNewStateIdMap[t.previousStateId];
      let next = oldStateIdNewStateIdMap[t.nextStateId];
      if (this.recursive || prev !== next) {
        const tId = prev + "_" + next + "_" + t.type;
        const transition = transitions.get(tId);
        const oldCount = transitionTotal[prev];
        transitionTotal[prev] = (oldCount ? oldCount : 0) + t.count;
        if (!transition) {
          transitions.set(tId, Object.assign({}, t));
        } else {
          transition.count += t.count;
        }
      }
    });

    //2 filter transition per percentage
    let finalTransitions = new Map<string, DialogFlowStateTransitionData>();
    transitions.forEach((t, k) => {
      let prev = oldStateIdNewStateIdMap[t.previousStateId];
      let next = oldStateIdNewStateIdMap[t.nextStateId];
      let prevState = finalStates[prev];
      let nextState = finalStates[next];
      let percentage = Math.round((t.count * 10000.0) / transitionTotal[prev]) / 100;
      if ((this.recursive || prev !== next) && finalStates[next] && (!t.previousStateId || finalStates[prev])) {
        if (percentage >= this.minimalTransitionPercentage
          && (!this.selectedStoryId ||
            (prevState && prevState.storyDefinitionId === this.selectedStoryId)
            || (!this.displayOnlyNext && nextState && nextState.storyDefinitionId === this.selectedStoryId))) {
          finalTransitions.set(k, t);
        }
      }
    });

    //3 filter by selected story
    let addStartup = true;
    const tmpFinalStates = [];
    states.forEach(s => {
      if (finalStates[s._id]) {
        let include = true;
        if (this.selectedStoryId) {
          //try to find a transition with this state and selectedStoryId
          include = s.storyDefinitionId === this.selectedStoryId;
          if (!include) {
            finalTransitions.forEach(t => {
              if (!include) {
                let prev = finalStates[oldStateIdNewStateIdMap[t.previousStateId]];
                let next = finalStates[oldStateIdNewStateIdMap[t.nextStateId]];
                if (((!this.displayOnlyNext && prev && prev._id === s._id) || (next && next._id === s._id))
                  && (
                    (prev && prev.storyDefinitionId === this.selectedStoryId)
                    || (!this.displayOnlyNext && next && next.storyDefinitionId === this.selectedStoryId)
                  )) {
                  include = true;
                }
              }
            });
          }
        }
        if (include) {
          addStartup = false;
          tmpFinalStates[s._id] = s;
          maxCount = Math.max(maxCount, s.count);
          const originalState = originalStatesById.get(s._id);
          const storyId = s.storyDefinitionId;
          graph.nodes.push({
            data: {
              id: s._id,
              name: s.nodeName(originalState.intent),
              weight: s.count,
              colorCode: entityColor(storyId ? storyId : "start"),
              shapeType: 'roundrectangle'
            }
          })
        }
      }
    });
    this.maxNodeCount = maxCount;
    finalStates = tmpFinalStates;

    finalTransitions.forEach(t => {
      let prev = oldStateIdNewStateIdMap[t.previousStateId];
      let next = oldStateIdNewStateIdMap[t.nextStateId];
      let percentage = Math.round((t.count * 10000.0) / transitionTotal[prev]) / 100;
      let fPrev = finalStates[prev];
      let fNext = finalStates[next];
      if (fNext && (!t.previousStateId || fPrev)
        && (!this.selectedStoryId
          || (fPrev && this.selectedStoryId == fPrev.storyDefinitionId)
          || (fNext && this.selectedStoryId == fNext.storyDefinitionId))) {

        if (!t.previousStateId && (!this.selectedStoryId || (!this.displayOnlyNext && this.selectedStoryId == fNext.storyDefinitionId))) {
          console.log(t);
          addStartup = true;
        }
        graph.edges.push({
          data: {
            source: prev ? prev : 'null',
            target: next,
            colorCode: entityColor(DialogFlowStateTransitionType[t.type] ? DialogFlowStateTransitionType[t.type] : DialogFlowStateTransitionType[DialogFlowStateTransitionType.nlp]),
            strength: t.count,
            label: percentage + '%',
            classes: 'autorotate'
          }
        });
      }
    });
    //4.0 add startup if useful
    if (addStartup) {
      graph.nodes.push({data: {id: 'null', name: 'Startup', weight: 1, colorCode: 'blue', shapeType: 'ellipse'}});
    }

    this.graphData = graph;
  }

  nodeChange(id: string) {
    this.selectedNode = this.flow.states.find(s => s._id === id);
  }
}
