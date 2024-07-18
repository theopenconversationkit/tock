import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VectorDbSettingsComponent } from './vector-db-settings.component';
describe('VectorDbSettingsComponent', () => {
  let component: VectorDbSettingsComponent;
  let fixture: ComponentFixture<VectorDbSettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VectorDbSettingsComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(VectorDbSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
