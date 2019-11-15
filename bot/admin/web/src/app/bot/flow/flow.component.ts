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

import {Component, OnDestroy, OnInit} from "@angular/core";
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
import {NodeTransition, StoryNode} from "./node";
import {MatSnackBar} from "@angular/material";
import {SelectBotEvent} from "../../shared/select-bot/select-bot.component";
import {StoryDefinitionConfiguration, StorySearchQuery, StoryStep} from "../model/story";
import {Subscription} from "rxjs";

@Component({
  selector: 'tock-flow',
  templateUrl: './flow.component.html',
  styleUrls: ['./flow.component.css']
})
export class FlowComponent implements OnInit, OnDestroy {

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
  minimalTransitionPercentage: number = 0;

  selectedStoryId: string;
  direction: number;

  stories: Map<string, string> = new Map();

  selectedEdge: NodeTransition;
  selectedNode: StoryNode;
  allNodes: StoryNode[];
  allTransitions: Map<string, NodeTransition>;
  graphData;

  botConfigurationId: string;
  lastBotSelection: SelectBotEvent;
  flow: ApplicationDialogFlow;

  allStories: StoryDefinitionConfiguration[];
  configuredStories: StoryDefinitionConfiguration[];
  configuredFlow: ApplicationDialogFlow;
  statsMode: boolean = false;

  private subscription: Subscription;

  valueAscOrder = (a: KeyValue<string, string>, b: KeyValue<string, string>): number => {
    return a.value.localeCompare(b.value);
  };

  constructor(private nlp: NlpService,
              private state: StateService,
              private bot: BotService,
              private botConfiguration: BotConfigurationService,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    this.reload();
    this.subscription = this.state.configurationChange.subscribe(_ => this.reload());
  }

  private reload() {
    this.bot.getStories(
      new StorySearchQuery(
        this.state.currentApplication.namespace,
        this.state.currentApplication.name,
        this.state.currentLocale,
        0,
        10000
      )).subscribe(s => {
      this.allStories = s;
      this.configuredStories = s.filter(story => !story.isBuiltIn());
      this.fillConfiguration();
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  changeLayout() {
    const layout = this.selectedLayout;
    this.layout = this.layouts.find(l => l.name === layout);
  }

  updateCount() {
    setTimeout(_ => {
      this.toGraphData(this.statsMode ? this.flow : this.configuredFlow)
    });
  }

  private fillConfiguration() {
    const intentStoryMap = new Map<string, StoryDefinitionConfiguration>();
    const states: DialogFlowStateData[] = [];
    const transitions: DialogFlowStateTransitionData[] = [];
    this.allStories.forEach(s => {
      intentStoryMap.set(s.intent.name, s);
      states.push(
        new DialogFlowStateData(
          s.intent.name,
          s.intent.name,
          [],
          null,
          1,
          s.intent.name
        )
      );
      transitions.push(
        new DialogFlowStateTransitionData(
          s.intent.name,
          [],
          DialogFlowStateTransitionType.nlp,
          null,
          null,
          1
        )
      );
    });

    this.allStories.forEach(s => {
      const intents = new Set<string>();
      StoryStep.findOutcomingIntent(intents, s.steps);
      intents.forEach(i => {
        const targetStory = intentStoryMap.get(i);
        if (targetStory) {
          transitions.push(
            new DialogFlowStateTransitionData(
              targetStory.intent.name,
              [],
              DialogFlowStateTransitionType.nlp,
              s.intent.name,
              null,
              1
            )
          );
        }
      });
    });
    this.configuredFlow = new ApplicationDialogFlow(states, transitions);
    this.reset();
  }

  reset() {
    this.selectedStoryId = null;
    this.selectedNode = null;
    this.selectedEdge = null;
    this.direction = null;
    this.update();
  }

  update() {
    this.minimalNodeCount = 0;
    this.updateCount();
  }

  changeMode() {
    this.statsMode = !this.statsMode;
    this.minimalTransitionPercentage = this.statsMode ? 10 : 0;
    setTimeout(_ => this.reset());
  }

  displayFlow(event?: SelectBotEvent) {
    if (!event || !event.equals(this.lastBotSelection)) {
      this.lastBotSelection = event;
      if (event && (event.all || event.configurationName)) {
        this.bot.getApplicationFlow(
          new DialogFlowRequest(
            this.state.currentApplication.namespace,
            this.state.currentApplication.name,
            this.state.currentLocale,
            this.state.currentApplication.name,
            event.configurationName,
            event.configurationId
          )
        ).subscribe(f => {
          this.flow = f;
          this.reset();
        });
      } else {
        this.graphData = null;
        this.flow = null;
        this.reset();
      }
    }
  }

  toGraphData(flow: ApplicationDialogFlow) {
    if (flow) {
      const displayOnlyNext: boolean = this.direction === -1;
      const displayOnlyPrev: boolean = this.direction === 1;
      const graph = {
        nodes: [],
        edges: []
      };

      //1 create nodes
      let nodeCount = 0;
      const nodesMap = new Map<number, StoryNode>();
      const stateIdNodeMap = new Map<string, StoryNode>();

      const nodeByKey = [];
      flow.states.forEach(s => {
        if (this.entity && this.step && this.intent) {
          const node = new StoryNode(s.storyDefinitionId, [s], nodeCount++, s.entities, s.intent, s.step);
          nodesMap.set(node.id, node);
          stateIdNodeMap.set(s._id, node);
        } else {
          let key = s.storyDefinitionId
            + (this.intent ? "+" + s.intent : "")
            + (this.step ? "+" + s.step : "")
            + (this.entity ? "+" + s.entities.join("%") : "");
          let node = nodeByKey[key];
          if (!node) {
            node = new StoryNode(
              s.storyDefinitionId,
              [s],
              nodeCount++,
              this.entity ? s.entities : [],
              this.intent ? s.intent : null,
              this.step ? s.step : null
            );
            nodeByKey[key] = node;
            nodesMap.set(node.id, node);
          } else {
            node.states.push(s);
            node.count += s.count;
          }
          stateIdNodeMap.set(s._id, node);
        }
      });

      //2 set stories map
      const stories = new Map<string, string>();
      nodesMap.forEach(s => stories.set(s.storyDefinitionId, s.displayName()));

      let finalNodes: StoryNode[] = [];
      //3 filter state by count
      nodesMap.forEach(s => {
        if (this.minimalNodeCount <= s.count) {
          finalNodes[s.id] = s;
        }
      });

      //4 create transitions
      const countTransitionByStartId = [];
      const transitionsByKey = new Map<string, NodeTransition>();
      flow.transitions.forEach(t => {
        const prev = stateIdNodeMap.get(t.previousStateId);
        const next = stateIdNodeMap.get(t.nextStateId);
        const prevId = prev ? prev.id : -1;
        //next should always exist but check anyway...
        if (next) {
          const nextId = next.id;
          if (this.recursive || prev !== next) {
            const tId = prevId + "_" + nextId + "_" + t.type;
            const transition = transitionsByKey.get(tId);
            if (!transition) {
              transitionsByKey.set(tId, new NodeTransition([t], prevId, nextId, t.type));
            } else {
              transition.transitions.push(t);
              transition.count = transition.count + t.count;
            }
            const oldCount = countTransitionByStartId[prevId];
            countTransitionByStartId[prevId] = oldCount ? oldCount + t.count : t.count;
          }
        }
      });

      //5 filter transitions per percentage
      const finalTransitions = new Map<string, NodeTransition>();
      transitionsByKey.forEach((t, k) => {
        const prev = nodesMap.get(t.previousId);
        const next = nodesMap.get(t.nextId);
        const prevId = prev ? prev.id : -1;
        const nextId = next.id;
        const finalPrev = prev ? finalNodes[prev.id] : null;
        const finalNext = finalNodes[nextId];
        const percentage = Math.round((t.count * 10000.0) / countTransitionByStartId[prevId]) / 100;
        if ((this.recursive || prev !== next) && finalNodes[nextId] && (t.previousId === -1 || finalNodes[prevId])) {
          if (percentage >= this.minimalTransitionPercentage
            && (!this.selectedStoryId ||
              (!displayOnlyPrev && finalPrev && finalPrev.storyDefinitionId === this.selectedStoryId)
              || (!displayOnlyNext && finalNext && finalNext.storyDefinitionId === this.selectedStoryId))) {
            finalTransitions.set(k, t);
          }
        }
      });

      //6 filter by selected story and create graph nodes
      let maxCount = 1;
      let addStartup = true;
      const tmpFinalStates = [];
      finalNodes.forEach(s => {
        let include = true;
        if (this.selectedStoryId) {
          //try to find a transition with this state and selectedStoryId
          include = s.storyDefinitionId === this.selectedStoryId;
          if (!include) {
            finalTransitions.forEach(t => {
              if (!include) {
                const prev = finalNodes[t.previousId];
                const next = finalNodes[t.nextId];
                const prevId = prev ? prev.id : -1;
                if (((!displayOnlyNext && prev && prevId === s.id) || (!displayOnlyPrev && next && next.id === s.id))
                  && (
                    (!displayOnlyPrev && prev && prev.storyDefinitionId === this.selectedStoryId)
                    || (!displayOnlyNext && next && next.storyDefinitionId === this.selectedStoryId)
                  )) {
                  include = true;
                }
              }
            });
          }
        }
        if (include) {
          addStartup = false;
          tmpFinalStates[s.id] = s;
          if (this.selectedStoryId) {
            if (s.storyDefinitionId === this.selectedStoryId) {
              maxCount = Math.max(maxCount, s.count);
            }
          } else {
            maxCount = Math.max(maxCount, s.count);
          }
          graph.nodes.push({
            data: {
              id: s.id,
              name: s.nodeName(),
              weight: s.count,
              colorCode: entityColor(s.storyDefinitionId),
              shapeType: s.dynamic ? 'ellipse' : 'roundrectangle'
            }
          })
        }
      });
      finalNodes = tmpFinalStates;

      if (finalTransitions.size > 1000) {
        this.snackBar.open("More than 1000 nodes to render - please change your options to decrease the number of nodes", "Error", {duration: 5000})
      } else {
        //7 create graph edges
        finalTransitions.forEach((t, k) => {
          const prev = nodesMap.get(t.previousId);
          const next = nodesMap.get(t.nextId);
          const prevId = prev ? prev.id : -1;
          let percentage = Math.round((t.count * 10000.0) / countTransitionByStartId[prevId]) / 100;
          let fPrev = finalNodes[prevId];
          let fNext = finalNodes[next.id];
          if (fNext && (t.previousId === -1 || fPrev)
            && (!this.selectedStoryId
              || (!displayOnlyPrev && fPrev && this.selectedStoryId === fPrev.storyDefinitionId)
              || (!displayOnlyNext && this.selectedStoryId === fNext.storyDefinitionId))) {

            if (t.previousId === -1) {
              addStartup = true;
            }
            //console.log(t);
            graph.edges.push({
              data: {
                source: prevId,
                target: next.id,
                key: k,
                colorCode: entityColor(DialogFlowStateTransitionType[t.type] ? DialogFlowStateTransitionType[t.type] : DialogFlowStateTransitionType[DialogFlowStateTransitionType.nlp]),
                strength: t.count,
                label: percentage + '%',
                classes: 'autorotate'
              }
            });
          }
        });

        //8 add startup if useful
        if (addStartup) {
          graph.nodes.push({data: {id: -1, name: 'Startup', weight: 1, colorCode: 'blue', shapeType: 'vee'}});
        }

        //9 init vars
        this.maxNodeCount = maxCount;
        this.stories = stories;
        this.allNodes = finalNodes;
        this.allTransitions = finalTransitions;
        this.graphData = graph;
      }
    }
  }

  nodeChange(id: string) {
    this.selectedNode = this.allNodes[id];
    this.selectedEdge = null;
  }

  edgeChange(key: string) {
    this.selectedEdge = this.allTransitions.get(key);
    this.selectedNode = null;
  }
}
