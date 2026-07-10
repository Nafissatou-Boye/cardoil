import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet />`
})
export class App implements OnInit {

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get(`${environment.apiUrl}/ping`, { responseType: 'text' }).subscribe({
      next: (res) => console.log('✅ API connectée :', res),
      error: (err) => console.error('❌ Erreur API :', err.message)
    });
  }
}