import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModelQualityTabsComponent } from './model-quality-tabs.component';

describe('ModelQualityTabsComponent', () => {
  let component: ModelQualityTabsComponent;
  let fixture: ComponentFixture<ModelQualityTabsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ModelQualityTabsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModelQualityTabsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
