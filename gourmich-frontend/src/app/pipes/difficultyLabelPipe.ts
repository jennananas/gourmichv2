import { Pipe, PipeTransform } from '@angular/core';
import { getDifficultyLabel } from '../utils/format';

@Pipe({
  name: 'difficultyLabel',
  standalone: true,
})
export class DifficultyLabelPipe implements PipeTransform {
  transform(value: number): string {
    return getDifficultyLabel(value);
  }
}