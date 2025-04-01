import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { LinechartComponent } from '../linechart/linechart.component';
import { DataService } from '../data.service';
import { TableformComponent } from '../tableform/tableform.component';
import { index } from 'd3';
import { Location } from '@angular/common';

@Component({
  selector: 'app-interface-det',
  standalone: true,
  imports: [CommonModule, FormsModule,HttpClientModule,LinechartComponent,TableformComponent],
  templateUrl: './interface-det.component.html',
  styleUrls: ['./interface-det.component.css']
})



export class InterfaceDetComponent implements OnInit {

  constructor(private route: ActivatedRoute,private http:HttpClient, private dataService:DataService,private location: Location) {}

  Id: string = "";
  duration: any[] = [
    { label: "1 hour", value: "1h" },
    { label: "6 hours", value: "6h" },
    { label: "12 hours", value: "12h" },
    { label: "24 hours", value: "1d" },
    { label: "1 week", value: "1w" },
    { label: "1 month", value: "30d" }
  ];
  type:any[] = ["Tables", "Charts"]
  data : any[]=[];
  general : any ={};
  status : any  = {};
  selectedItem: string = "";
  selectedType: string = "Tables";
  ip = history.state.ip;
  index = history.state.index;

  
  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      this.Id = params['id']; 
      console.log("Received ID:", this.Id);
    }); 
    
    const receivedTime = history.state.time;
    const matchedDuration = this.duration.find(item => item.label === receivedTime);
    console.log("You touched")
    console.log(receivedTime)
    console.log(index)
    //console.log(matchedDuration.value);
    
    if (matchedDuration) {
      this.selectedItem = matchedDuration.value;
    }
    this.fetchData(this.Id,this.selectedItem,this.index,this.ip);
  }

  onSelectChange(event: Event) {
    const selectedValue = (event.target as HTMLSelectElement).value;
    this.fetchData(this.Id,selectedValue,this.index,this.ip);
    console.log('Selected Duration:', this.selectedItem);
  }
  onTypeChange(event: Event) {
    const selectedValue = (event.target as HTMLSelectElement).value;
    console.log('Selected Duration:', this.selectedType);
  }
  
    
    fetchData(id: string, receivedTime: string,index:number, ip:string) {
      const URL = `http://localhost:9090/SNMP_TASK/getdata?time=${receivedTime}&id=${id}&index=${index}&ip=${ip}`;
      
      this.http.get< any >(URL).subscribe
        (response => {
          console.log('Full API Response:', response);
    
          // Extracting details
          this.general = response.general ? response.general[0] : null;
          this.data = response.data ? response.data[0].data:null;
          this.status = response.status ? response.status[0] : null;
          this.dataService.setInterfaceData(this.data);
          this.dataService.setInterval(this.selectedItem);
          this.dataService.setIP(ip);
          console.log("General Data:", this.general);
          console.log("Interface Data:", this.data);
          console.log("Status Data:", this.status);
        },
        (err) => {
          console.log("Error Occurred", err);
        }
      );
    }

    goback(){
      this.dataService.setIP(this.ip)
      this.dataService.setInterval(this.selectedItem)
      window.history.back();
    }
}
