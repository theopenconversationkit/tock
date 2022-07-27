import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output
} from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { intentDefinition, TickActionDefinition } from '../../../../models';
import { ScenarioProductionService } from '../../scenario-production.service';

@Component({
  selector: 'scenario-transition',
  templateUrl: './transition.component.html',
  styleUrls: ['./transition.component.scss']
})
export class ScenarioTransitionComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @Output() removeTransition = new EventEmitter();
  @Input() transition: { name: string; target: string };
  @Input() parentState;
  @Input() intents: intentDefinition[];
  @Input() actions: TickActionDefinition[];
  constructor(
    public elementRef: ElementRef,
    private scenarioProductionService: ScenarioProductionService
  ) {
    this.scenarioProductionService.scenarioProductionItemsCommunication
      .pipe(takeUntil(this.destroy))
      .subscribe((evt) => {
        if (evt.type == 'redrawIntents') {
          this.setTransitionTop();
        }
      });
  }

  intent;
  ngOnInit(): void {
    this.intent = this.intents.find((i) => {
      return i.name === this.transition.name;
    });
  }

  ngAfterViewInit(): void {
    this.scenarioProductionService.registerTransitionComponent(this);
    setTimeout(() => {
      this.setTransitionTop();
    });
  }

  getIntentTooltip() {
    return this.intent.label ? this.intent.label : this.intent.name;
  }

  setTransitionTop() {
    this.elementRef.nativeElement.style.top = this.getTransitionTop() + 'px';
  }

  getTransitionTop() {
    const stateComponent =
      this.scenarioProductionService.scenarioProductionStateComponents[
        this.transition.target.replace(/^#/, '')
      ];
    const transitionComponent =
      this.scenarioProductionService.scenarioProductionTransitionsComponents[this.transition.name];

    if (stateComponent && transitionComponent) {
      const stateElem = stateComponent.elementRef.nativeElement;
      const transitionElem = transitionComponent.elementRef.nativeElement;
      let averageHeight = transitionElem.offsetHeight;

      let siblings = [];
      let i, index;
      i = index = 0;

      for (let sibling in this.parentState.on) {
        if (this.parentState.on[sibling] === this.transition.target) {
          if (sibling == this.transition.name) {
            index = i;
          } else {
            siblings.push(sibling);
          }
          i++;
        }
      }

      let totalHeight = averageHeight;
      let margin = 0;
      if (siblings.length) {
        margin = 5;
        totalHeight = totalHeight * i + margin * (i - 1);
      }

      return (
        stateElem.offsetTop +
        stateElem.offsetHeight / 2 -
        totalHeight / 2 +
        averageHeight * index +
        margin * index
      );
    }
    return 0;
  }

  remove(transition) {
    this.removeTransition.emit(transition);
  }

  ngOnDestroy(): void {
    this.scenarioProductionService.unRegisterTransitionComponent(this.transition.name);
    this.destroy.next();
    this.destroy.complete();
  }
}
