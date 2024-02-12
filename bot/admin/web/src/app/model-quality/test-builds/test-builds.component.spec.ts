import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestBuildsComponent } from './test-builds.component';

describe('TestBuildsComponent', () => {
  let component: TestBuildsComponent;
  let fixture: ComponentFixture<TestBuildsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TestBuildsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TestBuildsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
