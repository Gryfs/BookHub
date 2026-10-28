import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-access-denied',
  imports: [RouterLink],
  templateUrl: './access-denied.html',
  styleUrl: '../not-found/not-found.css',
})
export class AccessDenied {}
