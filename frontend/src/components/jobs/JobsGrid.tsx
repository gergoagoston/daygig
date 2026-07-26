import type { Job } from '../../types/job';
import JobCard from './JobCard';

type JobsGridProps = {
  jobs: Job[];
};

function JobsGrid({ jobs }: JobsGridProps) {
  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3 min-[1900px]:grid-cols-4">
      {jobs.map((job) => (
        <JobCard key={job.id} job={job} />
      ))}
    </div>
  );
}

export default JobsGrid;
