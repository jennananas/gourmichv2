import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'firstLetter',
  standalone: true // si tu utilises Angular standalone
})
export class FirstLetterPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) return '';
    return value.charAt(0).toUpperCase(); // ou juste charAt(0) si tu ne veux pas la mettre en maj
  }
}