type LoadingStateProps = {
  message: string;
};

function LoadingState({ message }: LoadingStateProps) {
  return (
    <div className="rounded-lg border border-border bg-surface p-8 shadow-panel">
      <p className="font-semibold text-heading">{message}</p>
    </div>
  );
}

export default LoadingState;
