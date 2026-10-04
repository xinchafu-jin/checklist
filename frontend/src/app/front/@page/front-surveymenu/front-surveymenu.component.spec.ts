import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FrontSurveymenuComponent } from './front-surveymenu.component';

describe('FrontSurveymenuComponent', () => {
  let component: FrontSurveymenuComponent;
  let fixture: ComponentFixture<FrontSurveymenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FrontSurveymenuComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FrontSurveymenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
