import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { NbIconModule, NbTooltipModule, NbButtonModule, NbButtonGroupModule } from '@nebular/theme';

import { BottomActionsComponent } from './bottom-actions/bottom-actions.component';
import { CanvasComponent } from './canvas.component';

@NgModule({
  imports: [CommonModule, NbTooltipModule, NbIconModule, NbButtonModule, NbButtonGroupModule],
  declarations: [BottomActionsComponent, CanvasComponent],
  exports: [BottomActionsComponent, CanvasComponent],
  providers: [],
  entryComponents: []
})
export class CanvasModule {}
