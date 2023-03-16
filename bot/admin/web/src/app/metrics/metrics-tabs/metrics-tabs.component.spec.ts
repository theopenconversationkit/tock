import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbRouteTabsetModule, NbTabsetModule } from '@nebular/theme';

import { TestSharedModule } from '../../shared/test-shared.module';
import { MetricsTabsComponent } from './metrics-tabs.component';

describe('MetricsTabsComponent', () => {
  let component: MetricsTabsComponent;
  let fixture: ComponentFixture<MetricsTabsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestSharedModule, NbTabsetModule, NbRouteTabsetModule],
      declarations: [MetricsTabsComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricsTabsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
