/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
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
import {SelectBotEvent} from "../../shared/select-bot/select-bot.component";
import {AnswerConfigurationType, StoryDefinitionConfiguration, StorySearchQuery, StoryStep} from "../model/story";
import {Subscription} from "rxjs";
import { NbToastrService } from '@nebular/theme';

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
  minimalTransitionPercentage: number = 10;

  selectedStory: StoryDefinitionConfiguration;
  direction: number;

  storiesById: Map<string, StoryDefinitionConfiguration> = new Map();
  nodesById: Map<string, StoryNode> = new Map();

  selectedEdge: NodeTransition;
  selectedNode: StoryNode;
  allNodes: StoryNode[];
  allTransitions: Map<string, NodeTransition>;
  graphData;

  botConfigurationId: string;
  lastBotSelection: SelectBotEvent;
  userFlow: ApplicationDialogFlow;

  allStories: StoryDefinitionConfiguration[];
  configuredStories: StoryDefinitionConfiguration[];
  staticFlow: ApplicationDialogFlow;
  statsMode: boolean = true;
  displayNodeType: boolean = false;
  mergeOldStories: boolean = true;
  displayDebug: boolean = false;

  private subscription: Subscription;

  valueAscOrder = (a: KeyValue<string, string>, b: KeyValue<string, string>): number => {
    return a.value.localeCompare(b.value);
  };

  constructor(private nlp: NlpService,
              private state: StateService,
              private bot: BotService,
              private botConfiguration: BotConfigurationService,
              private toastrService: NbToastrService) {
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
      console.debug(this.allStories.length + ' stories retrieved, incl. ' + this.configuredStories.length + ' configured stories.');
      this.buildStaticFlowFromStories();
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
      this.buildGraph(this.statsMode ? this.userFlow : this.staticFlow)
    });
  }

  private buildStaticFlowFromStories() {
    const intentStoryMap = new Map<string, StoryDefinitionConfiguration>();
    const states: DialogFlowStateData[] = [];
    const transitions: DialogFlowStateTransitionData[] = [];
    this.allStories.forEach(s => {
      intentStoryMap.set(s.intent.name, s);
      states.push(new DialogFlowStateData(
                                            s._id,
                                            s.intent.name,
                                            [],
                                            null,
                                            s.currentType,
                                            s.name,
                                            0,
                                            s._id
                                          ));
      transitions.push(new DialogFlowStateTransitionData(
                                                      s._id,
                                                      [],
                                                      DialogFlowStateTransitionType.nlp,
                                                      null,
                                                      null,
                                                      0
                                                    ));
    });

    this.allStories.forEach(s => {
      const intents = new Set<string>();
      StoryStep.findOutcomingIntent(intents, s.steps);
      intents.forEach(i => {
        const targetStory = intentStoryMap.get(i);
        if (targetStory) {
          transitions.push(new DialogFlowStateTransitionData(
                                                              targetStory.intent.name,
                                                              [],
                                                              DialogFlowStateTransitionType.nlp,
                                                              s.intent.name,
                                                              null,
                                                              0
                                                            ));
        }
      });
    });
    this.staticFlow = new ApplicationDialogFlow(states, transitions);
    this.reset();
  }

  reset() {
    this.selectedStory = null;
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
    this.reset();
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
          this.userFlow = f;
          console.debug('Application flow retrieved, incl. ' + f.states.length + ' states ' + f.transitions.length + ' transistions.');
          this.reset();
        });
      } else {
        this.graphData = null;
        this.userFlow = null;
        this.reset();
      }
    }
  }

  buildGraph(theFlow: ApplicationDialogFlow) {
    let flow = this.mergeOldStories ? (JSON.parse(JSON.stringify(theFlow))) : theFlow; // clone - states might be modified by merge
    if (flow) {
      const displayOnlyNext: boolean = this.direction === -1;
      const displayOnlyPrev: boolean = this.direction === 1;
      const graph = {
        nodes: [],
        edges: []
      };

      //1 create nodes
      let nodesNumber = 0;
      const nodesByIndex = new Map<number, StoryNode>();
      const nodesByStateId = new Map<string, StoryNode>();
      const nodesByStoryKey = new Map<string, StoryNode>();

      flow.states.forEach(s => {
        if (this.entity && this.step && this.intent) {
          const node = new StoryNode(nodesNumber++, s._id, s.storyDefinitionId, [s], s.entities, s.intent, s.step, s.storyType, s.storyName);
          nodesByIndex.set(node.index, node);
          nodesByStateId.set(s._id, node);
        } else {
          let keyWithoutType = s.storyDefinitionId
            + (this.intent ? "+" + s.intent : "")
            + (this.step ? "+" + s.step : "")
            + (this.entity ? "+" + s.entities.join("%") : "");
          let key = keyWithoutType
            + (s.storyType != undefined ? "+" + s.storyType : "");
          let node = nodesByStoryKey.get(key);
          if (node) {
            node.states.push(s);
          } else {
            if (this.mergeOldStories && key != keyWithoutType) {
              const nodeWithoutType = nodesByStoryKey.get(keyWithoutType);
              if (nodeWithoutType) {
                console.debug('Replacing previous node with typed node...');
                // Merge previous node to a new typed node
                node = new StoryNode(
                  nodeWithoutType.index,
                  s.storyDefinitionId,
                  s.storyDefinitionId,
                  [s],
                  this.entity ? s.entities : [],
                  this.intent ? s.intent : null,
                  this.step ? s.step : null,
                  s.storyType,
                  s.storyName
                );
                node.states.push.apply(node.states, nodeWithoutType.states);

                // Update maps
                nodesByStoryKey.delete(keyWithoutType);
                nodesByStoryKey.set(key, node);
                nodesByIndex.set(node.index, node);
                node.states.forEach(state => {
                  state.storyType = s.storyType;
                  nodesByStateId.set(state._id, node);
                });
              } else {
                node = new StoryNode(
                  nodesNumber++,
                  s.storyDefinitionId,
                  s.storyDefinitionId,
                  [s],
                  this.entity ? s.entities : [],
                  this.intent ? s.intent : null,
                  this.step ? s.step : null,
                  s.storyType,
                  s.storyName
                );
                nodesByStoryKey.set(key, node);
                nodesByIndex.set(node.index, node);
              }
            } else {
              node = new StoryNode(
                nodesNumber++,
                s.storyDefinitionId,
                s.storyDefinitionId,
                [s],
                this.entity ? s.entities : [],
                this.intent ? s.intent : null,
                this.step ? s.step : null,
                s.storyType,
                s.storyName
              );
              nodesByStoryKey.set(key, node);
              nodesByIndex.set(node.index, node);
            }
          }
          nodesByStateId.set(s._id, node);
        }
      });

      //2 set stories map
      const storiesById = new Map<string, StoryDefinitionConfiguration>();
      if (!this.statsMode) {
        nodesByIndex.forEach(s => {
          const theStoryId = s._id;
          const theStoryName = s.storyName;
          const theStory = this.allStories.find(aStory => aStory._id == theStoryId);
          if (theStory) {
            storiesById.set(theStoryId, theStory);
            if (s.intent === 'goldobot') {
              console.debug("Found story: " + theStoryName + " (ID: " + theStoryId + ")");
              console.debug("Mapping storyId " + theStoryId + " to story:");
              console.debug(theStory);
            }
          } else {
            console.warn("Cannot find story: " + theStoryName + " (ID: " + theStoryId + ")");
          }
        }
        );
      }

      //3 filter state by count
      let finalNodes: StoryNode[] = [];
      nodesByIndex.forEach(s => {
        if (this.minimalNodeCount <= s.count) {
          finalNodes[s.index] = s;
        }
      });

      //4 create transitions
      const countTransitionByStartId = [];
      const transitionsByKey = new Map<string, NodeTransition>();
      flow.transitions.forEach(t => {
        const prev = nodesByStateId.get(t.previousStateId);
        const next = nodesByStateId.get(t.nextStateId);
        const prevId = prev ? prev.index : -1;
        //next should always exist but check anyway...
        if (next) {
          const nextId = next.index;
          if (this.recursive || prev !== next) {
            const tId = prevId + "_" + nextId + "_" + t.type;
            const transition = transitionsByKey.get(tId);
            if (!transition) {
              transitionsByKey.set(tId, new NodeTransition([t], prevId, nextId, t.type));
            } else {
              transition.transitions.push(t);
            }
            const oldCount = countTransitionByStartId[prevId];
            countTransitionByStartId[prevId] = oldCount ? oldCount + t.count : t.count;
          }
        }
      });

      //5 filter transitions per percentage
      const finalTransitions = new Map<string, NodeTransition>();
      transitionsByKey.forEach((t, k) => {
        const prev = nodesByIndex.get(t.previousId);
        const next = nodesByIndex.get(t.nextId);
        const prevId = prev ? prev.index : -1;
        const nextId = next.index;
        const finalPrev = prev ? finalNodes[prev.index] : null;
        const finalNext = finalNodes[nextId];
        const percentage = Math.round((t.count * 10000.0) / countTransitionByStartId[prevId]) / 100;
        if ((this.recursive || prev !== next) && finalNodes[nextId] && (t.previousId === -1 || finalNodes[prevId])) {
          if (!this.statsMode
            || (percentage >= this.minimalTransitionPercentage
            && (!this.selectedStory ||
              (!displayOnlyPrev && finalPrev && finalPrev.storyDefinitionId === this.selectedStory._id)
              || (!displayOnlyNext && finalNext && finalNext.storyDefinitionId === this.selectedStory._id)))) {
            finalTransitions.set(k, t);
          }
        }
      });

      //6 filter by selected story and create graph nodes
      let addStartup = true;
      const tmpFinalStates = [];
      const theMinCount = finalNodes.reduce((prev, current) => (prev.count < current.count) ? prev : current ).count;
      const theMaxCount = finalNodes.reduce((prev, current) => (prev.count > current.count) ? prev : current ).count;
      console.debug("Node min visits: " + theMinCount);
      console.debug("Node max visits: " + theMaxCount);

      finalNodes.forEach(s => {
        let include = true;
        if (this.selectedStory) {
          //try to find a transition with this state and selectedStory ID
          include = s.storyDefinitionId === this.selectedStory._id;
          if (!include) {
            finalTransitions.forEach(t => {
              if (!include) {
                const prev = finalNodes[t.previousId];
                const next = finalNodes[t.nextId];
                const prevId = prev ? prev.index : -1;
                if (((!displayOnlyNext && prev && prevId === s.index) || (!displayOnlyPrev && next && next.index === s.index))
                  && (
                    (!displayOnlyPrev && prev && prev.storyDefinitionId === this.selectedStory._id)
                    || (!displayOnlyNext && next && next.storyDefinitionId === this.selectedStory._id)
                  )) {
                  include = true;
                }
              }
            });
          }
        }
        if (include) {
          addStartup = false;
          tmpFinalStates[s.index] = s;
          const theScore = this.statsMode ? Math.round(((s.count - theMinCount) * 100.0) / (theMaxCount - theMinCount)) : 0;
          const theNodeName = s.nodeName() + (this.displayNodeType ? (s.storyType == AnswerConfigurationType.builtin ? ' 🔧'
                                                                    : (s.storyType == undefined ? ' ?'
                                                                    : ' 💬'))
                                                                    : '');
          graph.nodes.push({
            data: {
              id: s.index,
              name: theNodeName,
              weight: theScore,
              colorCode: entityColor(s.storyDefinitionId),
              shapeType: s.dynamic ? 'ellipse' : 'roundrectangle'
            }
          })
        }
      });
      finalNodes = tmpFinalStates;

      if (finalTransitions.size > 1000) {
        this.toastrService.show("More than 1000 nodes to render - please change your options to decrease the number of nodes", "Error", {duration: 5000})
      } else {

        //7 create graph edges
        finalTransitions.forEach((t, k) => {
          const prev = nodesByIndex.get(t.previousId);
          const next = nodesByIndex.get(t.nextId);
          const prevId = prev ? prev.index : -1;
          let percentage = Math.round((t.count * 10000.0) / countTransitionByStartId[prevId]) / 100;
          let fPrev = finalNodes[prevId];
          let fNext = finalNodes[next.index];
          if (fNext && (t.previousId === -1 || fPrev)
            && (!this.selectedStory
              || (!displayOnlyPrev && fPrev && this.selectedStory._id === fPrev.storyDefinitionId)
              || (!displayOnlyNext && this.selectedStory._id === fNext.storyDefinitionId))) {

            if (t.previousId === -1) {
              addStartup = true;
            }
            //console.log(t);
            graph.edges.push({
              data: {
                source: prevId,
                target: next.index,
                key: k,
                colorCode: entityColor(DialogFlowStateTransitionType[t.type] ? DialogFlowStateTransitionType[t.type] : DialogFlowStateTransitionType[DialogFlowStateTransitionType.nlp]),
                strength: t.count,
                label: this.statsMode ? percentage + '%' : '',
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
        this.maxNodeCount = theMaxCount;
        this.storiesById = storiesById;
        this.nodesById = nodesByStoryKey;
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
