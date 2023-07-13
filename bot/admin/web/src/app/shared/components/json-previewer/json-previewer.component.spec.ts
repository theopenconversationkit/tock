import { SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbCardModule, NbDialogRef, NbIconModule, NbSelectModule, NbToastrModule } from '@nebular/theme';

import { JsonPreviewerComponent } from './json-previewer.component';
import { TestingModule } from '../../../../testing';

describe('JsonPreviewerComponent', () => {
  let component: JsonPreviewerComponent;
  let fixture: ComponentFixture<JsonPreviewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestingModule, NbCardModule, NbIconModule, NbSelectModule, NbToastrModule.forRoot({})],
      declarations: [JsonPreviewerComponent],
      providers: [
        {
          provide: NbDialogRef,
          useValue: {}
        }
      ]
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
      const stringReplaced = component['replaceCharactersByHtmlCode'](s);

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

  describe('test template', () => {
    const mockJson = {
      id: 'root',
      type: 'parallel',
      states: {
        Global: {
          id: 'Global'
        }
      },
      initial: 'Global',
      on: {}
    };

    it('should render as many index row as existing row in the json', () => {
      component.jsonData = mockJson;
      component.ngOnChanges({
        jsonData: new SimpleChange({}, component.jsonData, true)
      });
      fixture.detectChanges();
      const linesNumberContainer: HTMLElement = fixture.debugElement.query(By.css('.lines-number')).nativeElement;

      expect(linesNumberContainer.children.length).toBe(11);
      Array.from(linesNumberContainer.children).forEach((child, i) => {
        expect(child.textContent.trim()).toBe((i + 1).toString());
      });
    });

    it('should render as many lines with syntax highlighting as existing lines in the json', () => {
      component.jsonData = mockJson;
      component.ngOnChanges({
        jsonData: new SimpleChange({}, component.jsonData, true)
      });
      fixture.detectChanges();
      const linesContainer: HTMLElement = fixture.debugElement.query(By.css('.lines-container')).nativeElement;
      const expectedResult = [
        '<span class="delimiter">{</span>',
        '<span class="space">&nbsp;&nbsp;</span><span class="key">"id":</span><span class="space">&nbsp;</span><span class="string">"root"</span><span class="comma">,</span>',
        '<span class="space">&nbsp;&nbsp;</span><span class="key">"type":</span><span class="space">&nbsp;</span><span class="string">"parallel"</span><span class="comma">,</span>',
        '<span class="space">&nbsp;&nbsp;</span><span class="key">"states":</span><span class="space">&nbsp;</span><span class="delimiter">{</span>',
        '<span class="space">&nbsp;&nbsp;&nbsp;&nbsp;</span><span class="key">"Global":</span><span class="space">&nbsp;</span><span class="delimiter">{</span>',
        '<span class="space">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span><span class="key">"id":</span><span class="space">&nbsp;</span><span class="string">"Global"</span>',
        '<span class="space">&nbsp;&nbsp;&nbsp;&nbsp;</span><span class="delimiter">}</span>',
        '<span class="space">&nbsp;&nbsp;</span><span class="delimiter">}</span><span class="comma">,</span>',
        '<span class="space">&nbsp;&nbsp;</span><span class="key">"initial":</span><span class="space">&nbsp;</span><span class="string">"Global"</span><span class="comma">,</span>',
        '<span class="space">&nbsp;&nbsp;</span><span class="key">"on":</span><span class="space">&nbsp;</span><span class="delimiter">{</span><span class="delimiter">}</span>',
        '<span class="delimiter">}</span>'
      ];

      expect(linesContainer.children.length).toBe(expectedResult.length);
      Array.from(linesContainer.children).forEach((child, i) => {
        const line = Array.from(child.firstElementChild.children)
          .map((e) => e.outerHTML)
          .join('');

        expect(line).toBe(expectedResult[i]);
      });
    });
  });
});
