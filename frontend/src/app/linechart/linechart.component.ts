import { Component, ElementRef, AfterViewInit, ViewChild,OnInit } from '@angular/core';
import * as d3 from 'd3';
import { DataService } from '../data.service';

interface TrafficData {
  time_slot: Date;
  avg_in_traffic: number;
}

@Component({
  selector: 'app-linechart',
  templateUrl: './linechart.component.html',
  styleUrls: ['./linechart.component.css']
})
export class LinechartComponent implements OnInit,AfterViewInit  {
  @ViewChild('chartContainer', { static: true }) chartContainer!: ElementRef;
  @ViewChild('outChartContainer', { static: true }) outChartContainer!: ElementRef;
  @ViewChild('inoutChartContainer', { static: true }) inoutChartContainer!: ElementRef;
  @ViewChild('inErrChartContainer', { static: true }) inErrChartContainer!: ElementRef;
  @ViewChild('outErrChartContainer', { static: true }) outErrChartContainer!: ElementRef;
  @ViewChild('inOutErrChartContainer', { static: true }) inOutErrChartContainer!: ElementRef;
  @ViewChild('inDiscContainer', { static: true }) inDiscChartContainer!: ElementRef;
  @ViewChild('outDiscChartContainer', { static: true }) outDiscChartContainer!: ElementRef;

  private interfaceData: any[] = [];

  constructor(private dataService: DataService) {}
  data:any[]=[];
  interval:string="";
  ngOnInit(): void {
    this.dataService.currentInterfaceData.subscribe(data => {
      this.interfaceData = data;
      this.updateData(); 
    });
  
    this.dataService.currentInterval.subscribe(data => {
      this.interval = data;
      console.log(this.interval);
      this.updateData();

    });
  }
  updateData(): void {
    if (!this.interfaceData) return; // Prevent errors if data is null
  
    this.data = this.interfaceData.map(d => ({
      date: d.time_slot ? new Date(d.time_slot) : (d.date ? new Date(d.date) : null),
      in_value: d.avg_in_traffic / 1000,
      out_value: d.avg_out_traffic / 1000,
      in_error: d.count_in_error,
      out_error: d.count_out_error,
      in_discard: d.count_in_discard
    }));
    this.destroyChart();
    this.createChartForView();
  }

    ngAfterViewInit(): void {
      this.destroyChart();
      this.createChartForView();
    }
    destroyChart():void{
      d3.select(this.chartContainer.nativeElement).selectAll("*").remove();
      d3.select(this.outChartContainer.nativeElement).selectAll("*").remove();
      d3.select(this.inErrChartContainer.nativeElement).selectAll("*").remove();
      d3.select(this.outErrChartContainer.nativeElement).selectAll("*").remove();
      d3.select(this.inoutChartContainer.nativeElement).selectAll("*").remove();
      d3.select(this.inOutErrChartContainer.nativeElement).selectAll("*").remove();

    }

    createChartForView():void{
      this.createChart('date','in_value',"Avg In Traffic",this.chartContainer);
      this.createChart('date','out_value',"Avg Out Traffic",this.outChartContainer);
      this.createChart('date','in_error',"In Errors",this.inErrChartContainer);
      this.createChart('date','out_error',"Out Errors",this.outErrChartContainer);
      this.createDualLineChart('date','in_value','out_value',"In Traffic","Out Traffic",this.inoutChartContainer)
      this.createDualLineChart('date','in_error','out_error',"In Errors","Out Errors",this.inOutErrChartContainer)
    }


    private createChart(col1: string, col2: string, yLabel: string,chartContainer:any): void {
      const margin = { top: 50, right: 50, bottom: 60, left: 100 };
      const width = 800 - margin.left - margin.right;
      const height = 500 - margin.top - margin.bottom;
  
      // Convert time_slot to Date format
      const dataset = this.data.map(d => ({
          x: new Date(d[col1]), // X-axis (time)
          y: d[col2] // Y-axis (metric)
      }));

      let inter = this.interval;

      console.log("Collected Dataset "+ dataset[0].x)
      const svg = d3.select(chartContainer.nativeElement)
          .append("svg")
          .attr("width", width + margin.left + margin.right)
          .attr("height", height + margin.top + margin.bottom)
          .append("g")
          .attr("transform", `translate(${margin.left},${margin.top})`);
  
      // X Scale (Time)
      const x = d3.scaleTime()
          .domain(d3.extent(dataset, d => d.x) as [Date, Date])
          .range([0, width]);
  
      // Y Scale
      const y = d3.scaleLinear()
          .domain([0, d3.max(dataset, d => d.y)!])
          .range([height, 0]);
  
      // X-Axis
      //
      
      if(this.interval === "1h" || this.interval ==="6h" || this.interval ==="12h"|| this.interval ==="1d"){
        svg.append("g")
          .attr("transform", `translate(0,${height})`)
          .call(d3.axisBottom(x).tickFormat(d3.timeFormat("%H:%M") as any));
      }
      else{
        svg.append("g")
    .attr("transform", `translate(0,${height})`)
    .call(
        d3.axisBottom(x)
            .ticks(d3.timeDay.every(1)) // Ensures only one tick per day
            .tickFormat(d3.timeFormat("%d %b %y") as any)
    );

      }
  
      // Y-Axis
      svg.append("g").call(d3.axisLeft(y));
  
      // Line generator
      const line = d3.line<{ x: Date, y: number }>()
          .x(d => x(d.x))
          .y(d => y(d.y));
  
      // Append Line
      svg.append("path")
          .datum(dataset)
          .attr("fill", "none")
          .attr("stroke", "steelblue")
          .attr("stroke-width", 2)
          .attr("d", line as any);
  
      // Add data points
      svg.selectAll(".dot")
          .data(dataset)
          .enter().append("circle")
          .attr("class", "dot")
          .attr("cx", d => x(d.x))
          .attr("cy", d => y(d.y))
          .attr("r", 5)
          .attr("fill", "red")
          .on("mouseover", function (event, d) {
            let timeFormat;
            if (inter === "1h" || inter === "6h" || inter ==="12h" || inter==="1d") {
                timeFormat = d3.timeFormat("%H:%M"); // Show hours and minutes
            } else {
                timeFormat = d3.timeFormat("%d %b %y"); // Show date format
            }
        
            tooltip.style("visibility", "visible")
                .text(`Time: ${timeFormat(d.x)}, Value: ${d.y.toFixed(2)}`)
                .style("left", (event.pageX + 10) + "px")
                .style("top", (event.pageY - 20) + "px");
        })
          .on("mouseout", () => tooltip.style("visibility", "hidden"));
  
      // Tooltip
      const tooltip = d3.select("body").append("div")
          .style("position", "absolute")
          .style("background", "#fff")
          .style("padding", "5px 10px")
          .style("border", "1px solid #000")
          .style("border-radius", "5px")
          .style("visibility", "hidden");
  
      // X-axis Label
      svg.append("text")
          .attr("x", width / 2)
          .attr("y", height + margin.bottom - 10)
          .attr("text-anchor", "middle")
          .style("font-size", "16px")
          .text("Time");
  
      // Y-axis Label (Dynamic)
      svg.append("text")
          .attr("x", -height / 2)
          .attr("y", -50)
          .attr("text-anchor", "middle")
          .attr("transform", "rotate(-90)")
          .style("font-size", "16px")
          .text(yLabel);
  }
  
  private createDualLineChart(col1: string, col2: string, col3: string, yLabel1: string, yLabel2: string, chartContainer: any): void {
    const margin = { top: 50, right: 60, bottom: 60, left: 100 };
    const width = 800 - margin.left - margin.right;
    const height = 500 - margin.top - margin.bottom;

    // Convert data into Date format and extract values
    const dataset = this.data.map(d => ({
        x: new Date(d[col1]), // X-axis (time)
        y1: d[col2], // Y-axis (first metric)
        y2: d[col3]  // Y-axis (second metric)
    }));

    const svg = d3.select(chartContainer.nativeElement)
        .append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", `translate(${margin.left},${margin.top})`);

    // X Scale (Time)
    const x = d3.scaleTime()
        .domain(d3.extent(dataset, d => d.x) as [Date, Date])
        .range([0, width]);

    // Y Scale
    const y = d3.scaleLinear()
        .domain([0, d3.max(dataset, d => Math.max(d.y1, d.y2))!])
        .range([height, 0]);

    // X-Axis
    //
    if(this.interval === "1h" || this.interval ==="6h" || this.interval ==="12h"|| this.interval ==="1d"){
        svg.append("g")
        .attr("transform", `translate(0,${height})`)
        .call(d3.axisBottom(x).tickFormat(d3.timeFormat("%H:%M") as any));
    }
    else{
      svg.append("g")
      .attr("transform", `translate(0,${height})`)
      .call(
          d3.axisBottom(x)
              .ticks(d3.timeDay.every(1)) // Ensures only one tick per day
              .tickFormat(d3.timeFormat("%d %b %y") as any)
      );
  
    }
    // Y-Axis
    svg.append("g")
        .call(d3.axisLeft(y));

    // Line generator for first Y variable
    const line1 = d3.line<{ x: Date, y1: number }>()
        .x(d => x(d.x))
        .y(d => y(d.y1));

    // Line generator for second Y variable
    const line2 = d3.line<{ x: Date, y2: number }>()
        .x(d => x(d.x))
        .y(d => y(d.y2));

    // Append first line
    svg.append("path")
        .datum(dataset)
        .attr("fill", "none")
        .attr("stroke", "steelblue")
        .attr("stroke-width", 2)
        .attr("d", line1 as any);

    // Append second line
    svg.append("path")
        .datum(dataset)
        .attr("fill", "none")
        .attr("stroke", "red")
        .attr("stroke-width", 2)
        .attr("d", line2 as any);

    // Tooltip
    const tooltip = d3.select(chartContainer.nativeElement)
        .append("div")
        .style("position", "absolute")
        .style("visibility", "hidden")
        .style("background", "#fff")
        .style("padding", "8px")
        .style("border", "1px solid #ccc")
        .style("border-radius", "4px")
        .style("box-shadow", "0px 2px 6px rgba(0,0,0,0.1)")
        .style("font-size", "12px");

    // Add data points for first line
    svg.selectAll(".dot1")
        .data(dataset)
        .enter().append("circle")
        .attr("class", "dot1")
        .attr("cx", d => x(d.x))
        .attr("cy", d => y(d.y1))
        .attr("r", 5)
        .attr("fill", "blue")
        .on("mouseover", (event, d) => {
            tooltip.style("visibility", "visible")
                .text(`${yLabel1}: ${d.y1}`);
        })
        .on("mousemove", (event) => {
            tooltip.style("top", `${event.pageY - 10}px`)
                .style("left", `${event.pageX + 10}px`);
        })
        .on("mouseout", () => {
            tooltip.style("visibility", "hidden");
        });

    // Add data points for second line
    svg.selectAll(".dot2")
        .data(dataset)
        .enter().append("circle")
        .attr("class", "dot2")
        .attr("cx", d => x(d.x))
        .attr("cy", d => y(d.y2))
        .attr("r", 5)
        .attr("fill", "red")
        .on("mouseover", (event, d) => {
            tooltip.style("visibility", "visible")
                .text(`${yLabel2}: ${d.y2}`);
        })
        .on("mousemove", (event) => {
            tooltip.style("top", `${event.pageY - 10}px`)
                .style("left", `${event.pageX + 10}px`);
        })
        .on("mouseout", () => {
            tooltip.style("visibility", "hidden");
        });

    // X-axis Label
    svg.append("text")
        .attr("x", width / 2)
        .attr("y", height + margin.bottom - 10)
        .attr("text-anchor", "middle")
        .style("font-size", "16px")
        .text("Time");

    // Y-axis Label
    svg.append("text")
        .attr("x", -height / 2)
        .attr("y", -50)
        .attr("text-anchor", "middle")
        .attr("transform", "rotate(-90)")
        .style("font-size", "16px")
        .text(`${yLabel1} & ${yLabel2}`);

    // Legend
    const legend = svg.append("g")
        .attr("transform", `translate(${width - 150}, ${-10})`);

    legend.append("rect")
        .attr("x", 0)
        .attr("y", 0)
        .attr("width", 12)
        .attr("height", 12)
        .attr("fill", "steelblue");

    legend.append("text")
        .attr("x", 20)
        .attr("y", 10)
        .text(yLabel1)
        .style("font-size", "14px");

    legend.append("rect")
        .attr("x", 0)
        .attr("y", 20)
        .attr("width", 12)
        .attr("height", 12)
        .attr("fill", "red");

    legend.append("text")
        .attr("x", 20)
        .attr("y", 30)
        .text(yLabel2)
        .style("font-size", "14px");
}


}