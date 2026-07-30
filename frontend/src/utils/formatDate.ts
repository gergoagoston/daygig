export function formatDate(value: string, language: string) {
  return new Intl.DateTimeFormat(language, {
    dateStyle: 'medium',
  }).format(new Date(value));
}
