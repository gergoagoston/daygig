type ErrorStateProps = {
  message: string;
};

function ErrorState({ message }: ErrorStateProps) {
  return (
    <div className="rounded-lg border border-danger bg-surface p-8 text-danger shadow-panel">
      <p className="font-semibold">{message}</p>
    </div>
  );
}

export default ErrorState;
