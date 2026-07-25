import { zodResolver } from '@hookform/resolvers/zod';
import { useForm, useWatch } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { jobCategories } from '../../../types/job';
import LocationPickerMap from '../../map/LocationPickerMap';
import Button from '../../ui/Button';
import Input from '../../ui/Input';
import Select from '../../ui/Select';
import Textarea from '../../ui/Textarea';
import {
  createJobSchema,
  defaultJobCreateValues,
  toJobCreatePayload,
  type JobCreateFormValues,
} from './jobCreateFormModel';
import useCreateJobSubmit from './useCreateJobSubmit';

type JobCreateFormProps = {
  onCancel: () => void;
};

function JobCreateForm({ onCancel }: JobCreateFormProps) {
  const { t } = useTranslation();
  const schema = createJobSchema(t);
  const {
    control,
    formState: { errors },
    handleSubmit,
    register,
    setError,
    setValue,
  } = useForm<JobCreateFormValues>({
    defaultValues: defaultJobCreateValues,
    resolver: zodResolver(schema),
  });
  const createJobMutation = useCreateJobSubmit({ onCreated: onCancel, setError });
  const [selectedLatitude, selectedLongitude] = useWatch({
    control,
    name: ['latitude', 'longitude'],
  });
  const selectedLocation =
    selectedLatitude && selectedLongitude
      ? {
          latitude: Number(selectedLatitude),
          longitude: Number(selectedLongitude),
        }
      : null;

  return (
    <form
      className="grid gap-5"
      noValidate
      onSubmit={handleSubmit((values) => createJobMutation.mutate(toJobCreatePayload(values)))}
    >
      <div className="grid gap-5 md:grid-cols-2">
        <Input
          id="create-job-title-input"
          label={t('jobs.create.fields.title')}
          type="text"
          maxLength={50}
          error={errors.title?.message}
          required
          {...register('title')}
        />

        <Input
          id="create-job-location-name"
          label={t('jobs.create.fields.location_name')}
          type="text"
          error={errors.locationName?.message}
          required
          {...register('locationName')}
        />
      </div>

      <Textarea
        id="create-job-description"
        label={t('jobs.create.fields.description')}
        maxLength={1000}
        error={errors.description?.message}
        required
        {...register('description')}
      />

      <div className="grid gap-5 md:grid-cols-2">
        <Select
          id="create-job-category"
          label={t('jobs.create.fields.category')}
          error={errors.category?.message}
          required
          {...register('category')}
        >
          <option value="">{t('jobs.create.fields.select_placeholder')}</option>
          {jobCategories.map((category) => (
            <option key={category} value={category}>
              {t(`jobs.categories.${category}`)}
            </option>
          ))}
        </Select>

        <Input
          id="create-job-hourly-rate"
          label={`${t('jobs.create.fields.hourly_rate')} (RON)`}
          inputMode="decimal"
          min="0"
          step="0.01"
          type="number"
          error={errors.hourlyRate?.message}
          required
          {...register('hourlyRate')}
        />
      </div>

      <div className="grid gap-2">
        <LocationPickerMap
          label={t('jobs.create.map.label')}
          selectedLocation={selectedLocation}
          onSelectLocation={(location) => {
            setValue('latitude', String(location.latitude), {
              shouldDirty: true,
              shouldValidate: true,
            });
            setValue('longitude', String(location.longitude), {
              shouldDirty: true,
              shouldValidate: true,
            });
          }}
        />
        <input type="hidden" {...register('latitude')} />
        <input type="hidden" {...register('longitude')} />
        {errors.latitude || errors.longitude ? (
          <p className="text-sm text-danger">
            {errors.latitude?.message ?? errors.longitude?.message}
          </p>
        ) : null}
      </div>

      {errors.form ? (
        <p className="rounded-lg bg-danger-soft px-3.5 py-3 font-semibold text-danger">
          {errors.form.message}
        </p>
      ) : null}

      <div className="flex flex-wrap justify-end gap-3">
        <button className="navbar-gomb" type="button" onClick={onCancel}>
          {t('jobs.create.cancel')}
        </button>
        <Button className="sm:w-auto" type="submit" isLoading={createJobMutation.isPending}>
          {createJobMutation.isPending ? t('jobs.create.submitting') : t('jobs.create.submit')}
        </Button>
      </div>
    </form>
  );
}

export default JobCreateForm;
