import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-header',
  imports: [RouterLink, CommonModule, ButtonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css', 
  
})
export class HeaderComponent {

   constructor(private authService: AuthService, private router: Router) {}
  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  getUsername(): string | null {
    return this.authService.getUsername();
  }

  logout(): void {  
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
