import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'tock-scenario-canvas-wrapper',
  templateUrl: './scenario-canvas-wrapper.component.html',
  styleUrls: ['./scenario-canvas-wrapper.component.scss']
})
export class ScenarioCanvasWrapperComponent {
  @Input() hasSidePanel: boolean = true;
  @Input() isSidePanelOpen: boolean = true;
  @Output() onSidePanelChange = new EventEmitter<'open' | 'close'>();

  toggleSidePanel(): void {
    this.isSidePanelOpen = !this.isSidePanelOpen;
    const sidePanelStatus = this.isSidePanelOpen ? 'open' : 'close';
    this.onSidePanelChange.emit(sidePanelStatus);
  }
}
