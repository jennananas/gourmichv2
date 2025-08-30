export function getDifficultyLabel(difficulty: number): string {
  switch (difficulty) {
    case 1:
      return 'Easy';
    case 4:
    case 5:
      return 'Hard';
    default:
      return 'Medium';
  }
}