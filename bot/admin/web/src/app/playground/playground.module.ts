import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlaygroundComponent } from './playground.component';
import { PlaygroundRoutingModule } from './playground-routing.module';
import { BotSharedModule } from '../shared/bot-shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  NbAccordionModule,
  NbButtonModule,
  NbCardModule,
  NbContextMenuModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbRadioModule,
  NbTooltipModule
} from '@nebular/theme';

@NgModule({
  declarations: [PlaygroundComponent],
  imports: [
    CommonModule,
    BotSharedModule,
    FormsModule,
    ReactiveFormsModule,
    NbButtonModule,
    NbTooltipModule,
    NbCardModule,
    NbIconModule,
    NbAccordionModule,
    NbRadioModule,
    NbInputModule,
    NbFormFieldModule,
    NbContextMenuModule,
    PlaygroundRoutingModule
  ]
})
export class PlaygroundModule {}
