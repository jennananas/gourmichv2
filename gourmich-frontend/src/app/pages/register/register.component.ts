import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormGroup, Validators, FormControl, FormBuilder } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { tap } from 'rxjs';
import { matchPasswordsValidator } from '../../validators/matchPasswordsValidator';
import { uniqueFieldValidator } from '../../validators/uniqueFieldValidator';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { FloatLabelModule } from 'primeng/floatlabel';
import { PasswordModule } from 'primeng/password';

@Component({
  selector: 'app-register',
  imports: [RouterLink, ReactiveFormsModule, CommonModule, ButtonModule
    , InputTextModule, FloatLabelModule, PasswordModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;

  constructor(private router: Router, private formBuilder : FormBuilder, private authService: AuthService) {}

  get username() {
    return this.registerForm.get('username');
  }

  get email() {
    return this.registerForm.get('email');
  }
  get password() {
    return this.registerForm.get('password');
  }
  get confirmPassword() {     
    return this.registerForm.get('confirmPassword');
  }
  get passwordMatch() {
    return this.registerForm.get('password')?.value === this.registerForm.get('confirmPassword')?.value;
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.registerForm.get(fieldName);
    return !!field?.invalid && (!!field?.dirty || !!field?.touched);
  }

  ngOnInit(): void {
    this.registerForm = this.formBuilder.group({
      username: new FormControl('',{
        validators: [
        Validators.required, 
        Validators.minLength(4),
        Validators.maxLength(20),
        Validators.pattern('^[a-zA-Z0-9]+$')
        ],
      asyncValidators: [uniqueFieldValidator(this.authService.checkUsername.bind(this.authService), 'usernameTaken')],
      updateOn:'blur'}),
      password: new FormControl('', {
        validators: [
          Validators.required,
          Validators.minLength(8),
          Validators.maxLength(20),
          Validators.pattern('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d])[A-Za-z\\d\\W_]{8,}$')
        ], 
        updateOn: 'blur'}),
        confirmPassword: new FormControl('', {validators:[
          Validators.required
        ], updateOn: 'blur'}),
        // Custom validator to check if password and confirmPassword match}),
        email: new FormControl('', { validators: [
          Validators.required, 
          Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)
        ],
        asyncValidators: [uniqueFieldValidator(this.authService.checkEmail.bind(this.authService), 'emailTaken')],
        updateOn: 'blur'}),
      },
      {
        validators: matchPasswordsValidator('password', 'confirmPassword'),
        updateOn: 'blur'
      }
    );
      }


  // Async validator to check if the username is already taken

  // Async validator to check if the email is already taken

  onSubmit(): void {
    if (this.registerForm.invalid) {
      // Marque tous les champs comme "touchés" pour afficher les erreurs
      this.registerForm.markAllAsTouched();
      console.log('Form is invalid');
      console.log(this.registerForm.errors);
      return;
    }

    const formData = this.registerForm.value;
    console.log('Form Data:', formData);
    console.log('Form submitted');
    
    this.authService.register(formData).pipe(
      tap({
        next: (response) => {
          console.log('Registration successful', response);
          this.router.navigateByUrl('/login');
        },
        error: (error) => {
          console.error('Registration failed', error);
        }
      })
    ).subscribe();
  }
}