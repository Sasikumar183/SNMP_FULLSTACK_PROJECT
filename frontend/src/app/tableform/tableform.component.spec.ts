import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TableformComponent } from './tableform.component';

describe('TableformComponent', () => {
  let component: TableformComponent;
  let fixture: ComponentFixture<TableformComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TableformComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TableformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
