import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { InterfaceDetComponent } from './interface-det/interface-det.component';

export const routes: Routes = [
    {
        path:"",
        component:DashboardComponent
    },
    {
        path: "specific",
        component: InterfaceDetComponent
    }
];
