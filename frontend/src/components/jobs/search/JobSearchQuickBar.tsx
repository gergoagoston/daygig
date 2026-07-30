import { useFormContext, useWatch } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import Button from '../../ui/Button';
import Input from '../../ui/Input';
import { defaultJobSearchValues, type JobSearchFormValues } from './jobSearchFormModel';

type JobSearchQuickBarProps = {
  isFiltersOpen: boolean;
  isSearching: boolean;
  onToggleFilters: () => void;
};

function JobSearchQuickBar({
  isFiltersOpen,
  isSearching,
  onToggleFilters,
}: JobSearchQuickBarProps) {
  const { t } = useTranslation();
  const {
    control,
    formState: { errors },
    register,
  } = useFormContext<JobSearchFormValues>();
  const [category, minHourlyRate, maxHourlyRate, sort, latitude, longitude] = useWatch({
    control,
    name: ['category', 'minHourlyRate', 'maxHourlyRate', 'sort', 'latitude', 'longitude'],
  });
  const activeFilterCount =
    Number(category !== defaultJobSearchValues.category) +
    Number(minHourlyRate !== defaultJobSearchValues.minHourlyRate) +
    Number(maxHourlyRate !== defaultJobSearchValues.maxHourlyRate) +
    Number(sort !== defaultJobSearchValues.sort) +
    Number(latitude !== '' && longitude !== '');

  return (
    <div className="grid items-end gap-3 sm:grid-cols-[minmax(0,1fr)_auto]">
      <Input
        id="job-search-query"
        label={t('jobs.search.fields.query')}
        maxLength={100}
        placeholder={t('jobs.search.fields.query_placeholder')}
        type="search"
        error={errors.q?.message}
        {...register('q')}
      />

      <div className="grid grid-cols-2 gap-3 sm:flex">
        <button
          className="inline-flex items-center justify-center whitespace-nowrap rounded-lg border border-border bg-surface px-4 py-3 font-bold text-heading transition hover:border-primary focus:outline-none focus:ring-2 focus:ring-primary-soft"
          type="button"
          aria-controls="job-search-filters"
          aria-expanded={isFiltersOpen}
          onClick={onToggleFilters}
        >
          {activeFilterCount > 0
            ? t('jobs.search.actions.filters_active', { count: activeFilterCount })
            : t('jobs.search.actions.filters')}
        </button>
        <Button className="!w-full sm:!w-auto" isLoading={isSearching} type="submit">
          {isSearching ? t('jobs.search.actions.searching') : t('jobs.search.actions.search')}
        </Button>
      </div>
    </div>
  );
}

export default JobSearchQuickBar;
