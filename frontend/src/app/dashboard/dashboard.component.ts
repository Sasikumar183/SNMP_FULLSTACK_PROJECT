import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Observable, Subject, takeUntil } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Router } from '@angular/router';
import { DataService } from '../data.service';
declare var bootstrap: any;  // Import Bootstrap globally


@Component({
  selector: 'app-dashboard',
  imports: [FormsModule, CommonModule, HttpClientModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  
  BASEURL:string = "http://localhost:9090/SNMP_TASK/";
  // BASEURL:string = "http://localhost:9090/SNMP_FULL_Task/";

  GETURL:string = "getdata?time="
  GETIP:string  = "getip"
  IPURL:string = "&ip="
  RENAMEURL:string = "rename";
  DELETEURL:string = "delete?"
  duration: any[] = [
    "1 hour", "6 hours" , "12 hours",
    "24 hours", "1 week", "1 month" 
  ]

  ip:string[] = [];
  selectedIP:string = "localhost";

  constructor(private http:HttpClient, private router:Router, private dataService:DataService) {
    
  }
  prevIP:string = '';
  prevInt:string = '';
  GOBACK:boolean = false;
  selectedItem:string = "1h";
  selectedValue:string = "1 hour";


  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.fetchIP()
      .pipe(takeUntil(this.destroy$))
      .subscribe((response: any) => {
        this.ip = response?.data || [];  
  
        this.dataService.currentInterval
          .pipe(takeUntil(this.destroy$))
          .subscribe((data) => {
            this.prevInt = data;
          });
  
        this.dataService.currentIp
          .pipe(takeUntil(this.destroy$))
          .subscribe((data) => {
            this.prevIP = data;
          });
  
        if (this.prevIP!=null && this.prevInt!=null) {
          this.selectedIP = this.prevIP;
          this.selectedItem = this.prevInt;
          this.selectedValue = this.selectedItem === "1h" ? "1 hour" :
          this.selectedItem === "6h" ? "6 hours" :
          this.selectedItem === "12h" ? "12 hours" :
          this.selectedItem === "1d" ? "24 hours" :
          this.selectedItem === "1w" ? "1 week" :
          this.selectedItem === "30d" ? "1 month" :
          "";     
          this.fetchData(this.selectedItem);
        }
        else{
          this.fetchData(this.selectedItem)
        }
      });
  }
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  fetchInterfaceDetails(ip:string){
    this.http.get<{ data: any[] }>(this.BASEURL + "getinterface?ip="+ip).subscribe(
      (res)=>{
        this.data=res.data;
      },
      (err) =>{
        console.log("Error while getting");
        
      });
      console.error();
      

  }
  fetchIP(): Observable<{ data: any[] }> {
    return this.http.get<{ data: any[] }>(this.BASEURL + this.GETIP);
  }
  

  // fetchIP(){
    
  

  fetchData(interval: string) {
    this.http.get<{ data: any[] }>(this.BASEURL + this.GETURL + interval+ this.IPURL +this.selectedIP).subscribe(
      (res) => {
        this.data =  res.data.sort((a,b)=>a.index-b.index);
        if(this.data.length === 0){
          this.fetchInterfaceDetails(this.selectedIP);
        }
        console.log(this.data);
      },
      (err) => {
        console.log("Error Occurred", err);
      }
    );
  }
  

  data: any[] = [];
  selectedId  : number = -1;
  newInterfaceName: string = "";

  onSelectChange(event: Event) {
    this.selectedValue = (event.target as HTMLSelectElement).value;

    this.selectedItem =
    this.selectedValue === "1 hour" ? "1h" :
    this.selectedValue === "6 hours" ? "6h" :
    this.selectedValue === "12 hours" ? "12h" :
    this.selectedValue === "24 hours" ? "1d" :
    this.selectedValue === "1 week" ? "1w" :
    this.selectedValue === "1 month" ? "30d" :
    "";   
    this.fetchData(this.selectedItem);

  }

  onSelectChangeIP(event: Event) {
    this.selectedIP = (event.target as HTMLSelectElement).value;
    this.fetchData(this.selectedItem);
  }

  deleteInterface(item:number,index:number){
   
    if(confirm("Confirm to Delete the data of this interface?")){

      this.http.delete(`${this.BASEURL}${this.DELETEURL}id=${item}&index=${index}&ip=${this.selectedIP}`)
            .subscribe(
              (res) => {
                alert("Deleted successfully");
                this.fetchData(this.selectedItem)
              },
              (err) => {
                console.error("Error Occurred", err);
              }
            );
          }
  }

  openRenameModal(id: number, name: string) {
    this.selectedId = id;
    this.newInterfaceName = name;
  
    let modalElement = document.getElementById('renameModal');
    if (modalElement) {
      let modal = new bootstrap.Modal(modalElement);
      modal.show();
    }
  }
  


    renameInterface() {
      if (this.selectedId !== -1 && this.newInterfaceName.trim() !== "") {
        this.http.patch(`${this.BASEURL}${this.RENAMEURL}?id=${this.selectedId}&name=${this.newInterfaceName}`, {})
          .subscribe(
            (res) => {
              this.fetchData(this.selectedItem);
              alert("Updated successfully");
            },
            (err) => {
              console.error("Error Occurred", err);
            }
          );
      }
    }

    navigateToPage(id: number,index:number) {
      this.router.navigate(['/specific'], { queryParams: { id: id } ,state: { time: this.selectedValue,index:index,ip:this.selectedIP }});
      
    }
    

}
