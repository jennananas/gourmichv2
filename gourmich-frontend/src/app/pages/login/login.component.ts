import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { FloatLabelModule } from "primeng/floatlabel"
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-login',
  imports: [PasswordModule,RouterLink, ReactiveFormsModule, CommonModule, FloatLabelModule, InputTextModule, ButtonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  value: string | undefined;
  loginForm!: FormGroup;
  errorMessage: string = "";

  constructor(private router: Router, private formBuilder: FormBuilder, private authService: AuthService) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  private initializeForm(): void {
    this.loginForm = this.formBuilder.group({
      username: new FormControl('', [Validators.required]),
      password: new FormControl('', [Validators.required]),
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      
      return;
    } 
    const {username, password} = this.loginForm.value;
    this.authService.login(username, password).subscribe({
      next: (response: { token: string }) => {
        this.authService.saveToken(response.token);
        this.authService.saveUserName(username);
        this.router.navigate(['/']);
      },
      error: (error: string) => {
        this.errorMessage = "Invalid credentials, please try again."
      }
    });
  }

  
}
