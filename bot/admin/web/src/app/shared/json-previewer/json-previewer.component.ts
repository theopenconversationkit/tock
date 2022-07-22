import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewEncapsulation
} from '@angular/core';
import { NbDialogRef, NbThemeService } from '@nebular/theme';
import { Subscription } from 'rxjs';

@Component({
  selector: 'tock-json-previewer',
  templateUrl: './json-previewer.component.html',
  styleUrls: ['./json-previewer.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class JsonPreviewerComponent implements OnInit, OnChanges, OnDestroy {
  @Input() jsonData!: string | object;
  @Input() title: string = 'JSON Preview';
  @Input() jsonPreviewerRef: NbDialogRef<JsonPreviewerComponent>;

  @Output() onClose: EventEmitter<boolean> = new EventEmitter<boolean>();

  data: string;
  subscription: Subscription = new Subscription();
  spacing: number = 2;
  theme: string = 'default';

  constructor(private themeService: NbThemeService) {}

  ngOnInit(): void {
    this.subscription = this.themeService.onThemeChange().subscribe((theme: any) => {
      this.theme = theme.name;
    });

    this.data = this.syntaxHighlight(this.jsonFormat(this.jsonData));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.jsonData.currentValue) {
      this.data = this.syntaxHighlight(this.jsonFormat(this.jsonData));
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private jsonFormat(jsonData: string | object): string {
    return JSON.stringify(jsonData, undefined, this.spacing);
  }

  private syntaxHighlight(jsonData: string): string {
    const json = jsonData.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

    return json.replace(
      /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?|[{}\[\]]?)/g,
      (match) => {
        let _class: string | undefined;

        if (/^"/.test(match)) {
          if (/:$/.test(match)) {
            _class = 'key';
          } else {
            _class = 'string';
          }
        } else if (/true|false/.test(match)) {
          _class = 'boolean';
        } else if (/null/.test(match)) {
          _class = 'null';
        } else if (/[{}\[\]]/.test(match)) {
          _class = 'delimiter';
        } else if (/\d/.test(match)) {
          _class = 'number';
        } else {
          return match;
        }

        return `<span ${_class && `class="${_class}"`}>${match}</span>`;
      }
    );
  }

  close(): void {
    if (this.jsonPreviewerRef) {
      this.jsonPreviewerRef.close();
    }

    this.onClose.emit(true);
  }
}
