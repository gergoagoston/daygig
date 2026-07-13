import { useTranslation } from 'react-i18next';

function JobsPage() {
  const { t } = useTranslation();

  return (
    <main className="app-page">
      <section>
        <p className="mb-2 font-bold text-primary">DayGig</p>
        <h1>{t('jobs.title')}</h1>
        <p className="max-w-2xl text-body">{t('jobs.placeholder')}</p>
      </section>
    </main>
  );
}

export default JobsPage;
