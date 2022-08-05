import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { NbIconModule, NbTooltipModule, NbButtonModule, NbButtonGroupModule } from '@nebular/theme';

import { ZoomInOutComponent } from './zoom-in-out/zoom-in-out.component';
import { CanvasDirective } from './canvas.directive';

@NgModule({
  imports: [CommonModule, NbTooltipModule, NbIconModule, NbButtonModule, NbButtonGroupModule],
  declarations: [ZoomInOutComponent, CanvasDirective],
  exports: [ZoomInOutComponent, CanvasDirective],
  providers: [],
  entryComponents: []
})
export class CanvasModule {}
