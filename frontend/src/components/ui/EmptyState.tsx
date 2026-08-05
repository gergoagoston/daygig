type EmptyStateProps = {
  title: string;
  description?: string;
};

function EmptyState({ title, description }: EmptyStateProps) {
  return (
    <div className="rounded-lg border border-border bg-surface p-8 shadow-panel">
      <h2 className="mb-2 text-2xl font-bold text-heading">{title}</h2>
      {description ? <p className="text-body">{description}</p> : null}
    </div>
  );
}

export default EmptyState;
