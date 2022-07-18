import { Component, Input, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { NbThemeService } from '@nebular/theme';
import { Subscription } from 'rxjs';

@Component({
  selector: 'tock-json-previewer',
  templateUrl: './json-previewer.component.html',
  styleUrls: ['./json-previewer.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class JsonPreviewerComponent implements OnInit, OnDestroy {
  @Input() jsonData: string | object;

  subscription: Subscription = new Subscription();

  spacing: number = 2;

  constructor(private themeService: NbThemeService) {}

  ngOnInit(): void {
    this.subscription = this.themeService.onThemeChange().subscribe((theme: any) => {
      this.jsonData = this.syntaxHighlight(this.jsonFormat(this.jsonData), theme.name);
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private jsonFormat(jsonData: string | object): string {
    return JSON.stringify(jsonData, undefined, this.spacing);
  }

  private syntaxHighlight(jsonData: string, theme: string): string {
    const json = jsonData.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

    return json.replace(
      /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?|[{}\[\]]?)/g,
      (match) => {
        let _class = 'number';

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
        }

        return `<span class="${theme}-${_class}">${match}</span>`;
      }
    );
  }
}
