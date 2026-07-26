import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { jobCategories } from '../../../types/job';
import Button from '../../ui/Button';
import Input from '../../ui/Input';
import Select from '../../ui/Select';
import {
  createJobSearchSchema,
  defaultJobSearchValues,
  jobSortOptions,
  type JobSearchFormValues,
} from './jobSearchFormModel';

type JobSearchPanelProps = {
  isSearching: boolean;
  onSearch: (values: JobSearchFormValues) => void;
};

function JobSearchPanel({ isSearching, onSearch }: JobSearchPanelProps) {
  const { t } = useTranslation();
  const {
    clearErrors,
    formState: { errors },
    handleSubmit,
    register,
    setValue,
  } = useForm<JobSearchFormValues>({
    defaultValues: defaultJobSearchValues,
    resolver: zodResolver(createJobSearchSchema(t)),
  });

  function handleReset() {
    setValue('q', defaultJobSearchValues.q);
    setValue('category', defaultJobSearchValues.category);
    setValue('minHourlyRate', defaultJobSearchValues.minHourlyRate);
    setValue('maxHourlyRate', defaultJobSearchValues.maxHourlyRate);
    setValue('sort', defaultJobSearchValues.sort);
    clearErrors();
    onSearch(defaultJobSearchValues);
  }

  return (
    <section className="border-b border-border pb-6" aria-label={t('jobs.search.title')}>
      <form className="grid gap-4" noValidate onSubmit={handleSubmit(onSearch)}>
        <div className="grid items-end gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-[minmax(240px,2fr)_minmax(180px,1fr)_minmax(170px,1fr)_minmax(170px,1fr)_minmax(190px,1.15fr)]">
          <Input
            id="job-search-query"
            label={t('jobs.search.fields.query')}
            maxLength={100}
            placeholder={t('jobs.search.fields.query_placeholder')}
            type="search"
            error={errors.q?.message}
            {...register('q')}
          />

          <Select
            id="job-search-category"
            label={t('jobs.search.fields.category')}
            error={errors.category?.message}
            {...register('category')}
          >
            <option value="">{t('jobs.search.fields.all_categories')}</option>
            {jobCategories.map((category) => (
              <option key={category} value={category}>
                {t(`jobs.categories.${category}`)}
              </option>
            ))}
          </Select>

          <Input
            id="job-search-min-hourly-rate"
            label={t('jobs.search.fields.min_hourly_rate')}
            inputMode="decimal"
            min="0"
            step="1"
            type="number"
            error={errors.minHourlyRate?.message}
            {...register('minHourlyRate')}
          />

          <Input
            id="job-search-max-hourly-rate"
            label={t('jobs.search.fields.max_hourly_rate')}
            inputMode="decimal"
            min="0"
            step="1"
            type="number"
            error={errors.maxHourlyRate?.message}
            {...register('maxHourlyRate')}
          />

          <Select
            id="job-search-sort"
            label={t('jobs.search.fields.sort')}
            error={errors.sort?.message}
            {...register('sort')}
          >
            {jobSortOptions.map((sort) => (
              <option key={sort} value={sort}>
                {t(`jobs.search.sort.${sort.replace(',', '_')}`)}
              </option>
            ))}
          </Select>
        </div>

        <div className="flex flex-wrap justify-end gap-3">
          <button
            className="rounded-lg border border-border bg-surface px-4.5 py-3 font-bold text-heading transition hover:border-primary focus:outline-none focus:ring-2 focus:ring-primary-soft"
            disabled={isSearching}
            type="button"
            onClick={handleReset}
          >
            {t('jobs.search.actions.reset')}
          </button>
          <Button className="!w-auto" isLoading={isSearching} type="submit">
            {isSearching ? t('jobs.search.actions.searching') : t('jobs.search.actions.search')}
          </Button>
        </div>
      </form>
    </section>
  );
}

export default JobSearchPanel;
