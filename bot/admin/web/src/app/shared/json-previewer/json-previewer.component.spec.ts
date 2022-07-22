import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbCardModule, NbIconModule } from '@nebular/theme';

import { TestSharedModule } from '../test-shared.module';
import { JsonPreviewerComponent } from './json-previewer.component';

describe('JsonPreviewerComponent', () => {
  let component: JsonPreviewerComponent;
  let fixture: ComponentFixture<JsonPreviewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NbCardModule, NbIconModule, TestSharedModule],
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
});
