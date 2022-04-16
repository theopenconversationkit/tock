/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  Renderer2
} from '@angular/core';

declare var cytoscape: any;

@Component({
  selector: 'tock-cytoscape',
  template: '<div id="cy"></div>',
  styles: [
    `
      #cy {
        height: 75%;
        width: 100%;
        position: relative;
        left: 0;
        top: 0;
      }
    `
  ]
})
export class CytoComponent implements OnChanges, OnDestroy {
  @Input() public elements: any;
  @Input() public style: any;
  @Input() public layout: any;
  @Input() public zoom: any;

  @Output() selectedNode: EventEmitter<string> = new EventEmitter<string>();
  @Output() selectedEdge: EventEmitter<string> = new EventEmitter<string>();

  private cy;

  public constructor(private renderer: Renderer2, private el: ElementRef) {
    this.layout = this.layout || {
      name: 'grid',
      directed: true,
      padding: 0
    };

    this.zoom = this.zoom || {
      min: 0.1,
      max: 10
    };

    this.style =
      this.style ||
      cytoscape
        .stylesheet()

        .selector('node')
        .css({
          shape: 'data(shapeType)',
          width: 'mapData(weight, 0, 100, 18, 60)',
          height: 'mapData(weight, 0, 100, 18, 60)',
          content: 'data(name)',
          'text-valign': 'center',
          'text-outline-width': 1,
          'text-outline-color': 'data(colorCode)',
          'background-color': 'data(colorCode)',
          color: '#fff',
          'font-size': 10
        })
        .selector(':selected')
        .css({
          'border-width': 1,
          'border-color': 'black'
        })
        .selector('edge')
        .css({
          'curve-style': 'bezier',
          opacity: 0.666,
          width: 'mapData(strength, 70, 100, 2, 6)',
          'target-arrow-shape': 'triangle',
          'line-color': 'data(colorCode)',
          'source-arrow-color': 'data(colorCode)',
          'target-arrow-color': 'data(colorCode)',
          'edge-text-rotation': 'autorotate'
        })
        .selector('edge.questionable')
        .css({
          'line-style': 'dotted',
          'target-arrow-shape': 'diamond'
        })
        .selector('edge[label]')
        .css({
          label: 'data(label)',
          width: 1,
          'font-size': 5
        })
        .selector('.faded')
        .css({
          opacity: 0.25,
          'text-opacity': 0
        });
  }

  ngOnDestroy(): void {
    if (this.cy) {
      this.cy.destroy();
    }
  }

  public ngOnChanges(): any {
    this.render();
  }

  public render() {
    setTimeout((_) => {
      let cy_container = this.renderer.selectRootElement('#cy');
      let nodeSelector = this.selectedNode;
      let edgeSelector = this.selectedEdge;
      let cy = cytoscape({
        container: cy_container,
        layout: this.layout,
        minZoom: this.zoom.min,
        maxZoom: this.zoom.max,
        style: this.style,
        elements: this.elements
      });

      cy.on('tap', 'node', function (e) {
        let node = e.target;
        let neighborhood = node.neighborhood().add(node);

        cy.elements().addClass('faded');
        neighborhood.removeClass('faded');
        nodeSelector.emit(node.data('id'));
      });

      cy.on('tap', 'edge', function (e) {
        let node = e.target;
        cy.elements().addClass('faded');
        node.target().removeClass('faded');
        node.source().removeClass('faded');
        node.removeClass('faded');
        edgeSelector.emit(node.data('key'));
      });

      cy.on('tap', function (e) {
        if (e.target === cy) {
          cy.elements().removeClass('faded');
        }
      });
      if (this.cy) {
        this.cy.destroy();
      }
      this.cy = cy;
    });
  }
}
