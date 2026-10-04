import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FrontDashboardComponent } from './front-dashboard.component';

describe('FrontDashboardComponent', () => {
  let component: FrontDashboardComponent;
  let fixture: ComponentFixture<FrontDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FrontDashboardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FrontDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
