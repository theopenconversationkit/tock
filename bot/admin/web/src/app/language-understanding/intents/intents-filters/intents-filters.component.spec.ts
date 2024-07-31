import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IntentsFiltersComponent } from './intents-filters.component';

describe('IntentsFiltersComponent', () => {
  let component: IntentsFiltersComponent;
  let fixture: ComponentFixture<IntentsFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ IntentsFiltersComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IntentsFiltersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
