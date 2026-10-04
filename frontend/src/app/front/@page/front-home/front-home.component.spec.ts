import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FrontHomeComponent } from './front-home.component';

describe('FrontHomeComponent', () => {
  let component: FrontHomeComponent;
  let fixture: ComponentFixture<FrontHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FrontHomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FrontHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
