import type { Job } from '../../types/job';
import JobCard from './JobCard';

type JobsGridProps = {
  jobs: Job[];
};

function JobsGrid({ jobs }: JobsGridProps) {
  return (
    <div className="grid grid-cols-[repeat(auto-fit,minmax(400px,600px))] justify-center gap-4">
      {jobs.map((job) => (
        <JobCard key={job.id} job={job} />
      ))}
    </div>
  );
}

export default JobsGrid;
