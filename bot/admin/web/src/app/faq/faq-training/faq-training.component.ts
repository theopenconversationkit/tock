import { Component } from '@angular/core';
import { SentenceTrainingMode } from '../../shared/components/sentence-training/models';

@Component({
  selector: 'tock-faq-training',
  templateUrl: './faq-training.component.html',
  styleUrls: ['./faq-training.component.scss']
})
export class FaqTrainingComponent {
  mode = SentenceTrainingMode.INBOX;
}
