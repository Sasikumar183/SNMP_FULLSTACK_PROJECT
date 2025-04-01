import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DataService {
  private interfaceDataSource = new BehaviorSubject<any>(null);
  private intervalSource = new BehaviorSubject<any>(null);
  private ipSource = new BehaviorSubject<any>(null);
  
  currentIp = this.ipSource.asObservable();
  currentInterfaceData = this.interfaceDataSource.asObservable();
  currentInterval = this.intervalSource.asObservable();
  
  private refreshDataSubject = new BehaviorSubject<boolean>(false);
  
  setInterfaceData(data: any) {
    this.interfaceDataSource.next(data);
  }
  setInterval(data: any){
    this.intervalSource.next(data);
  }
  setIP(data:any){
    this.ipSource.next(data);
  }
}
