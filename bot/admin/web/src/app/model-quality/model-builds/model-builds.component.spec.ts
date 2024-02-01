import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModelBuildsComponent } from './model-builds.component';

describe('ModelBuildsComponent', () => {
  let component: ModelBuildsComponent;
  let fixture: ComponentFixture<ModelBuildsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ModelBuildsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModelBuildsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
