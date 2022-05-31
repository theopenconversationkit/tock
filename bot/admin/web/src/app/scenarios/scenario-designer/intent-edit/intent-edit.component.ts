import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { scenarioItem } from '../../models';
import { Token } from '../../../sentence-analysis/highlight/highlight.component';

@Component({
  selector: 'scenario-intent-edit',
  templateUrl: './intent-edit.component.html',
  styleUrls: ['./intent-edit.component.scss']
})
export class IntentEditComponent implements OnInit {
  @Input() item: scenarioItem;
  @Output() saveModifications = new EventEmitter();
  itemCopy: scenarioItem;
  constructor(public dialogRef: NbDialogRef<IntentEditComponent>, protected state: StateService) {}

  ngOnInit(): void {
    this.itemCopy = JSON.parse(JSON.stringify(this.item));

    if (this.item.intentDefinition?._sentences) {
      this.item.intentDefinition?._sentences.forEach((sentence) => {
        this.initTokens(sentence);
      });
    }
  }

  dissociateIntent() {
    delete this.item.intentDefinition;
    this.cancel();
  }

  addSentence($event) {
    if ($event.target.value.trim()) {
      if (!this.itemCopy.intentDefinition.sentences) this.itemCopy.intentDefinition.sentences = [];
      this.itemCopy.intentDefinition.sentences.push($event.target.value.trim());
      $event.target.value = '';
    }
  }

  removeSentence(sentence) {
    this.itemCopy.intentDefinition.sentences = this.itemCopy.intentDefinition.sentences.filter(
      (s) => s != sentence
    );
  }

  save() {
    this.saveModifications.emit(this.itemCopy);
  }

  cancel(): void {
    this.dialogRef.close();
  }

  private initTokens(sentence) {
    let i = 0;
    let entityIndex = 0;
    const text = sentence.getText();
    const entities = sentence.getEntities();
    const result: Token[] = [];
    while (i <= text.length) {
      if (entities.length > entityIndex) {
        const e = entities[entityIndex];
        if (e.start !== i) {
          result.push(new Token(i, text.substring(i, e.start), result.length));
        }
        result.push(new Token(e.start, text.substring(e.start, e.end), result.length, e));
        i = e.end;
        entityIndex++;
      } else {
        if (i != text.length) {
          result.push(new Token(i, text.substring(i, text.length), result.length));
        }
        break;
      }
    }
    console.log(result);
    sentence._tokens = result;
  }

  getContrastYIQ(hexcolor) {
    if (!hexcolor) return '';
    hexcolor = hexcolor.replace('#', '');
    var r = parseInt(hexcolor.substr(0, 2), 16);
    var g = parseInt(hexcolor.substr(2, 2), 16);
    var b = parseInt(hexcolor.substr(4, 2), 16);
    var yiq = (r * 299 + g * 587 + b * 114) / 1000;
    return yiq >= 128 ? 'black' : 'white';
  }
}
