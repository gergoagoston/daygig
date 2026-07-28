import { useTranslation } from 'react-i18next';

function ReviewsPlaceholder() {
  const { t } = useTranslation();

  return (
    <section className="py-7">
      <h2 className="text-xl font-bold text-heading">{t('profile.reviews.title')}</h2>
      <p className="mt-2 text-body">{t('profile.reviews.empty')}</p>
    </section>
  );
}

export default ReviewsPlaceholder;
