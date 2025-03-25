import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InterfaceDetComponent } from './interface-det.component';

describe('InterfaceDetComponent', () => {
  let component: InterfaceDetComponent;
  let fixture: ComponentFixture<InterfaceDetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InterfaceDetComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InterfaceDetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
