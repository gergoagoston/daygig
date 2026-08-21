import type { ReactNode, SubmitEventHandler } from 'react';

type AuthFormLayoutProps = {
  title: string;
  subtitle: string;
  onSubmit: SubmitEventHandler<HTMLFormElement>;
  children: ReactNode;
};

function AuthFormLayout({ title, subtitle, onSubmit, children }: AuthFormLayoutProps) {
  return (
    <main className="app-page-centered">
      <section className="grid w-full max-w-5xl items-center gap-8 md:grid-cols-[0.9fr_1.1fr]">
        <div className="hidden min-w-0 text-left md:block">
          <div className="mb-6 h-1.5 w-18 rounded-full bg-primary" />
          <h1 className="max-w-md leading-tight">{title}</h1>
          <p className="text-body">{subtitle}</p>
        </div>

        <form
          className="min-w-0 rounded-lg border border-border bg-surface p-8 text-left shadow-panel"
          onSubmit={onSubmit}
          noValidate
        >
          {children}
        </form>
      </section>
    </main>
  );
}

export default AuthFormLayout;
