import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FaqTabsComponent } from './faq-tabs.component';

describe('FaqTabsComponent', () => {
  let component: FaqTabsComponent;
  let fixture: ComponentFixture<FaqTabsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FaqTabsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FaqTabsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
