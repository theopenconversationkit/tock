import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbCardModule, NbIconModule, NbToastrModule } from '@nebular/theme';

import { TestSharedModule } from '../test-shared.module';
import { JsonPreviewerComponent } from './json-previewer.component';

describe('JsonPreviewerComponent', () => {
  let component: JsonPreviewerComponent;
  let fixture: ComponentFixture<JsonPreviewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NbCardModule, NbIconModule, NbToastrModule.forRoot({}), TestSharedModule],
      declarations: [JsonPreviewerComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JsonPreviewerComponent);
    component = fixture.componentInstance;
    component.jsonData = {};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should replace characters by html code', () => {
    const stringsToTest = [
      '',
      '&',
      '<',
      '>',
      ' ',
      '&&&',
      '<<<',
      '>>>',
      '   ',
      '&<> ',
      'abcdefghijklmnopqrstuvwxyz',
      'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
      '1234567890',
      'é"\'(-è_çà)=~#{[|`\\^@]}"$£êù%*µ?,.;/:§!âå€þýûîô¶ÂøÊ±æðÛÎÔ¹«»©®ß¬',
      '<script src="hack.js?go&go"></script>'
    ];
    const expectedResult = [
      '',
      '&amp;',
      '&lt;',
      '&gt;',
      '&nbsp;',
      '&amp;&amp;&amp;',
      '&lt;&lt;&lt;',
      '&gt;&gt;&gt;',
      '&nbsp;&nbsp;&nbsp;',
      '&amp;&lt;&gt;&nbsp;',
      'abcdefghijklmnopqrstuvwxyz',
      'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
      '1234567890',
      'é"\'(-è_çà)=~#{[|`\\^@]}"$£êù%*µ?,.;/:§!âå€þýûîô¶ÂøÊ±æðÛÎÔ¹«»©®ß¬',
      '&lt;script&nbsp;src="hack.js?go&amp;go"&gt;&lt;/script&gt;'
    ];

    stringsToTest.forEach((s, i) => {
      const stringReplaced = component['replaceCharactersByHtmlCode'](s); // access to private method

      expect(stringReplaced).toEqual(expectedResult[i]);
    });
  });

  it('should construct a row array from a string containing line breaks or carriage returns', () => {
    const stringsToTest = ['', 'test', 'a\nb\nc\nd', 'a\r\nb\r\nc\r\nd'];
    const expectedResult = [[''], ['test'], ['a', 'b', 'c', 'd'], ['a', 'b', 'c', 'd']];

    stringsToTest.forEach((s, i) => {
      const result = component.lineBreak(s);
      expect(result).toEqual(expectedResult[i]);
    });
  });

  it('should render a set of html nodes associated with a css class as a string from a character string representing elements of a json', () => {
    const stringsToTest = [
      '{',
      '}',
      ',',
      '"title": "oui"',
      '[{"id": 1}]',
      '{"id": "root","status": "DELETED", "isOnline": false, "description": null}'
    ];

    const expectedResult = [
      '<span class="delimiter">{</span>',
      '<span class="delimiter">}</span>',
      '<span class="comma">,</span>',
      '<span class="key">"title":</span><span class="space">&nbsp;</span><span class="string">"oui"</span>',
      '<span class="delimiter">[</span><span class="delimiter">{</span><span class="key">"id":</span><span class="space">&nbsp;</span><span class="number">1</span><span class="delimiter">}</span><span class="delimiter">]</span>',
      '<span class="delimiter">{</span><span class="key">"id":</span><span class="space">&nbsp;</span><span class="string">"root"</span><span class="comma">,</span><span class="key">"status":</span><span class="space">&nbsp;</span><span class="string">"DELETED"</span><span class="comma">,</span><span class="space">&nbsp;</span><span class="key">"isOnline":</span><span class="space">&nbsp;</span><span class="boolean">false</span><span class="comma">,</span><span class="space">&nbsp;</span><span class="key">"description":</span><span class="space">&nbsp;</span><span class="null">null</span><span class="delimiter">}</span>'
    ];

    stringsToTest.forEach((s, i) => {
      const result = component.syntaxHighlight(s);
      expect(result).toBe(expectedResult[i]);
    });
  });
});
