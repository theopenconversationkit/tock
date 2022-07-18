import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JsonPreviewerComponent } from './json-previewer.component';

describe('JsonPreviewerComponent', () => {
  let component: JsonPreviewerComponent;
  let fixture: ComponentFixture<JsonPreviewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JsonPreviewerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JsonPreviewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
