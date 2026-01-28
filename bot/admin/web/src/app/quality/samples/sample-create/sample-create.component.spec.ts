import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SampleCreateComponent } from './sample-create.component';

describe('SampleCreateComponent', () => {
  let component: SampleCreateComponent;
  let fixture: ComponentFixture<SampleCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SampleCreateComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SampleCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
