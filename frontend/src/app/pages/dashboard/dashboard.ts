import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {

}

// import { Component, inject,OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { RouterModule } from '@angular/router';
// import { HttpClient } from '@angular/common/http';

// @Component({
//   selector: 'app-dashboard',
//   imports: [CommonModule, RouterModule],
//   templateUrl: './dashboard.html',
//   styleUrl: './dashboard.css',
// })
// export class Dashboard implements OnInit{

//   private http = inject(HttpClient);

//   ngOnInit() {
//     console.log("Dashboard loaded");
//     this.http.get<any>('http://localhost:8080/api/test/secure')
//       .subscribe({
//         next: (res) => console.warn('SUCCESS:', res),
//         error: (err) => console.error('ERROR:', err)
//       });
//   }
// }