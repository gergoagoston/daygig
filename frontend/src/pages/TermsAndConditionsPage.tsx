import { useTranslation } from 'react-i18next';

const termsSectionKeys = [
  'platform_role',
  'not_a_party',
  'user_responsibility',
  'offline_incidents',
  'agreements_and_payments',
  'verification',
  'conduct',
  'moderation',
  'acceptance',
] as const;

function TermsAndConditionsPage() {
  const { t } = useTranslation();

  return (
    <main className="app-page">
      <article className="mx-auto flex w-full max-w-4xl flex-col gap-8">
        <div className="flex flex-col gap-4">
          <h1 className="m-0! max-w-3xl text-4xl! font-bold leading-tight text-heading sm:text-5xl!">
            {t('legal.terms.title')}
          </h1>
          <p className="m-0 max-w-3xl text-lg text-body">{t('legal.terms.subtitle')}</p>
        </div>

        <div className="flex flex-col gap-6">
          {termsSectionKeys.map((sectionKey) => (
            <section
              key={sectionKey}
              className="border-t border-border pt-6 first:border-t-0 first:pt-0"
            >
              <h2 className="m-0 mb-2 text-xl font-bold text-heading">
                {t(`legal.terms.sections.${sectionKey}.title`)}
              </h2>
              <p className="m-0 text-body">{t(`legal.terms.sections.${sectionKey}.body`)}</p>
            </section>
          ))}
        </div>
      </article>
    </main>
  );
}

export default TermsAndConditionsPage;
