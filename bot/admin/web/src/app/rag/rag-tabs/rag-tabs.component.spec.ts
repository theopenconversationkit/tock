import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StateService } from '../../core-nlp/state.service';

import { RagTabsComponent } from './rag-tabs.component';
import { TabLink } from '../../shared/utils';

let hasAdminRoleVariable = true;

describe('RagTabsComponent', () => {
  let component: RagTabsComponent;
  let fixture: ComponentFixture<RagTabsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RagTabsComponent],
      providers: [
        {
          provide: StateService,
          useValue: {
            hasRole: () => hasAdminRoleVariable
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RagTabsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should propose all rag tabs if user role is Admin', () => {
    hasAdminRoleVariable = true;

    fixture = TestBed.createComponent(RagTabsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.tabLinks).toEqual([
      new TabLink('sources', 'Rag sources', 'cloud-download-outline'),
      new TabLink('exclusions', 'Rag exclusions', 'alert-triangle-outline'),
      new TabLink('settings', 'Rag settings', 'settings-outline')
    ]);
  });

  it('should not propose all rag tabs if user role is not Admin', () => {
    hasAdminRoleVariable = false;

    fixture = TestBed.createComponent(RagTabsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.tabLinks).toEqual([
      new TabLink('exclusions', 'Rag exclusions', 'alert-triangle-outline'),
      new TabLink('settings', 'Rag settings', 'settings-outline')
    ]);
  });
});
