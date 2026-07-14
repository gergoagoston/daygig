import { useQuery } from '@tanstack/react-query';
import { useParams } from 'react-router-dom';

import { getJobById } from '../api/jobsApi';

function JobDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const hasJobId = Boolean(id);
  const {
    data: job,
    isError,
    isLoading,
  } = useQuery({
    queryKey: ['job', id],
    queryFn: () => getJobById(id ?? ''),
    enabled: hasJobId,
  });

  return (
    <main className="app-page">
      {isLoading ? <p>Loading...</p> : null}

      {(!hasJobId || isError) && !isLoading ? <p>Could not load job details.</p> : null}

      {job && !isLoading && !isError ? (
        <dl>
          <dt>ID</dt>
          <dd>{job.id}</dd>

          <dt>Title</dt>
          <dd>{job.title}</dd>

          <dt>Description</dt>
          <dd>{job.description}</dd>

          <dt>Category</dt>
          <dd>{job.category}</dd>

          <dt>Hourly rate</dt>
          <dd>
            {job.hourlyRate} {job.currency}
          </dd>

          <dt>Location</dt>
          <dd>{job.locationName}</dd>

          <dt>Status</dt>
          <dd>{job.status}</dd>

          <dt>Latitude</dt>
          <dd>{job.latitude}</dd>

          <dt>Longitude</dt>
          <dd>{job.longitude}</dd>

          <dt>Created at</dt>
          <dd>{job.createdAt}</dd>

          <dt>Updated at</dt>
          <dd>{job.updatedAt}</dd>
        </dl>
      ) : null}
    </main>
  );
}

export default JobDetailsPage;
