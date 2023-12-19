import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LanguageUnderstandingTabsComponent } from './language-understanding-tabs.component';

describe('LanguageUnderstandingTabsComponent', () => {
  let component: LanguageUnderstandingTabsComponent;
  let fixture: ComponentFixture<LanguageUnderstandingTabsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LanguageUnderstandingTabsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LanguageUnderstandingTabsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
