import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';

import { getUserProfile } from '../api/usersApi';
import ReviewsPlaceholder from '../components/profile/ReviewsPlaceholder';
import UserAvatar from '../components/profile/UserAvatar';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import { formatDate } from '../utils/formatDate';

function UserProfilePage() {
  const { i18n, t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const hasUserId = Boolean(id);
  const {
    data: profile,
    isError,
    isLoading,
  } = useQuery({
    queryKey: ['user-profile', id],
    queryFn: () => getUserProfile(id ?? ''),
    enabled: hasUserId,
  });
  const language = i18n.resolvedLanguage ?? i18n.language;

  return (
    <main className="app-page">
      <div className="mx-auto w-full max-w-4xl">
        {isLoading ? <LoadingState message={t('profile.public.loading')} /> : null}

        {(!hasUserId || isError) && !isLoading ? (
          <ErrorState message={t('profile.public.errors.load_failed')} />
        ) : null}

        {profile && !isLoading && !isError ? (
          <>
            <header className="flex flex-col items-center gap-5 py-8 text-center sm:flex-row sm:text-left">
              <UserAvatar
                avatarUrl={profile.avatarUrl}
                firstName={profile.firstName}
                lastName={profile.lastName}
                size="large"
              />

              <div className="min-w-0">
                <h1 className="m-0 break-words leading-tight">
                  {profile.firstName} {profile.lastName}
                </h1>
                <p className="mt-2 text-body">
                  {t('profile.public.member_since', {
                    date: formatDate(profile.createdAt, language),
                  })}
                </p>
              </div>
            </header>

            <section className="py-7">
              <h2 className="text-xl font-bold text-heading">{t('profile.bio.label')}</h2>
              <p className="mt-3 max-w-3xl whitespace-pre-line leading-relaxed text-body">
                {profile.bio || t('profile.bio.empty')}
              </p>
            </section>

            <ReviewsPlaceholder />
          </>
        ) : null}
      </div>
    </main>
  );
}

export default UserProfilePage;
