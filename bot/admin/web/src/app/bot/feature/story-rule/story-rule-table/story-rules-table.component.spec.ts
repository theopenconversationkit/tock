import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StoryRulesTableComponent } from './story-rules-table.component';

describe('StoryRuleTableComponent', () => {
  let component: StoryRulesTableComponent;
  let fixture: ComponentFixture<StoryRulesTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StoryRulesTableComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(StoryRulesTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
