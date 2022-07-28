import { DOCUMENT } from '@angular/common';
import {
  Component,
  EventEmitter,
  inject,
  Inject,
  InjectionToken,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewEncapsulation
} from '@angular/core';
import { NbDialogRef, NbThemeService, NbToastrService } from '@nebular/theme';
import { Subscription } from 'rxjs';

const NAVIGATOR = new InjectionToken<Navigator>('An abstraction over navigator object', {
  factory: () => inject(DOCUMENT).defaultView.window.navigator
});

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

  data: string[];
  subscription: Subscription = new Subscription();
  spacing: number = 2;
  theme: string = 'default';

  constructor(
    @Inject(NAVIGATOR) private navigatorRef: Navigator,
    private themeService: NbThemeService,
    private toastrService: NbToastrService
  ) {}

  ngOnInit(): void {
    this.subscription = this.themeService.onThemeChange().subscribe((theme: any) => {
      this.theme = theme.name;
    });
    this.data = this.lineBreak(this.jsonData);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.jsonData.currentValue) {
      this.data = this.lineBreak(this.jsonData);
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  syntaxHighlight(jsonData: string): string {
    const json = this.replaceCharactersByHtmlCode(jsonData);

    return json.replace(
      /(&nbsp;)+|,|("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?|[{}\[\]]?)/g,
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
        } else if (/&nbsp;/.test(match)) {
          _class = 'space';
        } else if (/,/.test(match)) {
          _class = 'comma';
        } else {
          return match;
        }

        return `<span ${_class && `class="${_class}"`}>${match}</span>`;
      }
    );
  }

  private replaceCharactersByHtmlCode(string: string): string {
    return string
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/\s/g, '&nbsp;');
  }

  private jsonFormat(jsonData: string | object): string {
    if (typeof jsonData === 'string') {
      return JSON.stringify(JSON.parse(jsonData), undefined, this.spacing);
    }
    return JSON.stringify(jsonData, undefined, this.spacing);
  }

  lineBreak(jsonData: string | object): string[] {
    jsonData = this.jsonFormat(jsonData);

    const lineBreak = /\r\n?|\n/g;
    return jsonData.split(lineBreak);
  }

  copyToClipboard(): void {
    this.navigatorRef.clipboard
      .writeText(this.jsonFormat(this.jsonData))
      .then(() => {
        this.toastrService.info(`The json has been copied successfully`, 'Copied', {
          duration: 5000,
          status: 'info'
        });
      })
      .catch(() => {
        this.toastrService.danger(`An error has occurred`, 'Error', {
          duration: 5000,
          status: 'danger'
        });
      });
  }

  spacingChange(): void {
    this.data = this.lineBreak(this.jsonData);
  }

  close(): void {
    if (this.jsonPreviewerRef) {
      this.jsonPreviewerRef.close();
    }

    this.onClose.emit(true);
  }
}
