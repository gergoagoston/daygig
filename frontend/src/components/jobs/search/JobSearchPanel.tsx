import { useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { FormProvider, useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import JobSearchFilters from './JobSearchFilters';
import {
  createJobSearchSchema,
  defaultJobSearchValues,
  type JobSearchFormValues,
} from './jobSearchFormModel';
import JobSearchQuickBar from './JobSearchQuickBar';

type JobSearchPanelProps = {
  isSearching: boolean;
  onSearch: (values: JobSearchFormValues) => void;
};

function JobSearchPanel({ isSearching, onSearch }: JobSearchPanelProps) {
  const { t } = useTranslation();
  const [isFiltersOpen, setIsFiltersOpen] = useState(false);
  const [filtersResetKey, setFiltersResetKey] = useState(0);
  const form = useForm<JobSearchFormValues>({
    defaultValues: defaultJobSearchValues,
    resolver: zodResolver(createJobSearchSchema(t)),
  });

  function handleReset() {
    form.reset(defaultJobSearchValues);
    setFiltersResetKey((currentKey) => currentKey + 1);
    onSearch(defaultJobSearchValues);
  }

  function handleSearch(values: JobSearchFormValues) {
    onSearch(values);
    setIsFiltersOpen(false);
  }

  return (
    <section className="pb-6" aria-label={t('jobs.search.title')}>
      <FormProvider {...form}>
        <form
          className="grid gap-4"
          noValidate
          onSubmit={form.handleSubmit(handleSearch, () => setIsFiltersOpen(true))}
        >
          <JobSearchQuickBar
            isFiltersOpen={isFiltersOpen}
            isSearching={isSearching}
            onToggleFilters={() => setIsFiltersOpen((isOpen) => !isOpen)}
          />
          <JobSearchFilters
            key={filtersResetKey}
            isOpen={isFiltersOpen}
            isSearching={isSearching}
            onReset={handleReset}
          />
        </form>
      </FormProvider>
    </section>
  );
}

export default JobSearchPanel;
