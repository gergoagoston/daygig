import { useTranslation } from 'react-i18next';

import type { JobCategory } from '../../types/job';

type JobDetailsHeaderProps = {
  category: JobCategory;
  title: string;
};

function JobDetailsHeader({ category, title }: JobDetailsHeaderProps) {
  const { t } = useTranslation();

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-wrap items-center gap-3">
        <span className="rounded-full bg-primary-soft px-3 py-1 text-sm font-bold text-primary">
          {t(`jobs.categories.${category}`)}
        </span>
      </div>

      <h1 className="!m-0 max-w-3xl !text-4xl font-bold leading-tight text-heading sm:!text-5xl">
        {title}
      </h1>
    </div>
  );
}

export default JobDetailsHeader;
