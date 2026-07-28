import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { z } from 'zod';

import { updateCurrentUserBio } from '../api/usersApi';
import AvatarEditor from '../components/profile/AvatarEditor';
import ReviewsPlaceholder from '../components/profile/ReviewsPlaceholder';
import Button from '../components/ui/Button';
import Textarea from '../components/ui/Textarea';
import useAuth from '../hooks/useAuth';

type ProfileFormValues = {
  bio: string;
};

function ProfileSettingsPage() {
  const { t } = useTranslation();
  const { updateUser, user } = useAuth();
  const profileSchema = z.object({
    bio: z.string().max(1000, t('profile.bio.errors.too_long')),
  });
  const {
    formState: { errors, isDirty },
    handleSubmit,
    register,
    reset,
  } = useForm<ProfileFormValues>({
    defaultValues: {
      bio: user?.bio ?? '',
    },
    resolver: zodResolver(profileSchema),
  });

  const bioMutation = useMutation({
    mutationFn: (values: ProfileFormValues) =>
      updateCurrentUserBio({
        bio: values.bio.trim() || null,
      }),
    onSuccess: (updatedUser) => {
      updateUser((currentUser) => ({
        ...currentUser,
        bio: updatedUser.bio,
      }));
      reset({ bio: updatedUser.bio ?? '' });
    },
  });

  if (!user) {
    return null;
  }

  const bioField = register('bio');
  const fullName = `${user.firstName} ${user.lastName}`.trim();

  return (
    <main className="app-page">
      <div className="mx-auto w-full max-w-5xl">
        <div>
          <section className="grid gap-8 py-8 md:grid-cols-[240px_minmax(0,1fr)]">
            <AvatarEditor user={user} onUserUpdated={updateUser} />

            <div className="min-w-0">
              <h2 className="text-2xl font-bold text-heading">{fullName}</h2>
              <dl className="mt-5 grid gap-4 text-sm sm:grid-cols-2">
                <div>
                  <dt className="font-bold text-heading">{t('profile.fields.email')}</dt>
                  <dd className="mt-1 break-words text-body">{user.email}</dd>
                </div>
                <div>
                  <dt className="font-bold text-heading">{t('profile.fields.phone')}</dt>
                  <dd className="mt-1 text-body">
                    {user.phoneNumber || t('profile.fields.not_provided')}
                  </dd>
                </div>
              </dl>

              <p className="mt-5 text-sm text-body">{t('profile.settings.private_details')}</p>
            </div>
          </section>

          <section className="py-7">
            <form
              className="max-w-3xl"
              onSubmit={(event) => void handleSubmit((values) => bioMutation.mutate(values))(event)}
            >
              <Textarea
                id="profile-bio"
                label={t('profile.bio.label')}
                maxLength={1000}
                rows={7}
                placeholder={t('profile.bio.placeholder')}
                error={errors.bio?.message}
                {...bioField}
                onChange={(event) => {
                  bioMutation.reset();
                  void bioField.onChange(event);
                }}
              />

              <div className="mt-2 flex items-center justify-between gap-4 text-sm text-body">
                <span>{t('profile.bio.help')}</span>
                <span>{t('profile.bio.limit')}</span>
              </div>

              <div className="mt-5 flex flex-wrap items-center gap-4">
                <div className="w-fit">
                  <Button type="submit" disabled={!isDirty} isLoading={bioMutation.isPending}>
                    {bioMutation.isPending ? t('profile.bio.saving') : t('profile.bio.save')}
                  </Button>
                </div>

                {bioMutation.isSuccess ? (
                  <p className="text-sm font-semibold text-heading" role="status">
                    {t('profile.bio.saved')}
                  </p>
                ) : null}

                {bioMutation.isError ? (
                  <p className="text-sm font-semibold text-danger" role="alert">
                    {t('profile.bio.errors.generic')}
                  </p>
                ) : null}
              </div>
            </form>
          </section>

          <ReviewsPlaceholder />
        </div>
      </div>
    </main>
  );
}

export default ProfileSettingsPage;
