import { Component, OnInit } from '@angular/core';
import { SentenceTrainingMode } from '../../shared/components/sentence-training/models';

@Component({
  selector: 'tock-rag-excluded',
  templateUrl: './rag-excluded.component.html',
  styleUrls: ['./rag-excluded.component.scss']
})
export class RagExcludedComponent {
  mode = SentenceTrainingMode.RAGEXCLUDED;
}
