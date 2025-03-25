import { Component } from '@angular/core';
import { DataService } from '../data.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-tableform',
  imports: [CommonModule,FormsModule],
  templateUrl: './tableform.component.html',
  styleUrl: './tableform.component.css'
})
export class TableformComponent {
  interfaceData: any[]=[];
  interval:string='';
  constructor(private dataService: DataService) {}

  ngOnInit() {
    this.dataService.currentInterfaceData.subscribe(data => {
      this.interfaceData = data;
      console.log("Received Interface Data in InterfaceDetComponent:", this.interfaceData);
    });
    this.dataService.currentInterval.subscribe(data=>{
      this.interval = data;
    })
  }
  

}
