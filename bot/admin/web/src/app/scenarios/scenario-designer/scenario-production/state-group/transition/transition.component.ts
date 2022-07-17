import { Component, ElementRef, EventEmitter, Input, OnDestroy, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ScenarioProductionService } from '../../scenario-production.service';

@Component({
  selector: 'scenario-transition',
  templateUrl: './transition.component.html',
  styleUrls: ['./transition.component.scss']
})
export class ScenarioTransitionComponent implements OnDestroy {
  destroy = new Subject();
  @Output() removeTransition = new EventEmitter();
  @Input() transition;

  constructor(
    public elementRef: ElementRef,
    private scenarioProductionService: ScenarioProductionService
  ) {}

  ngAfterViewInit(): void {
    this.scenarioProductionService.registerTransitionComponent(this);
    setTimeout(() => {
      this.setTransitionTop();
    });
  }

  setTransitionTop() {
    this.elementRef.nativeElement.style.top = this.getTransitionTop() + 'px';
  }

  getTransitionTop() {
    const stateComponent =
      this.scenarioProductionService.scenarioProductionStateComponents[this.transition.target];
    const transitionComponent =
      this.scenarioProductionService.scenarioProductionTransitionsComponents[this.transition.name];
    if (stateComponent && transitionComponent) {
      const stateElem = stateComponent.elementRef.nativeElement;
      const transitionElem = transitionComponent.elementRef.nativeElement;
      return stateElem.offsetTop + stateElem.offsetHeight / 2 - transitionElem.offsetHeight / 2;
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
