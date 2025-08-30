import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-forgot-pswd',
  imports: [RouterLink, ButtonModule],
  templateUrl: './forgot-pswd.component.html',
  styleUrl: './forgot-pswd.component.css'
})
export class ForgotPswdComponent {

}
