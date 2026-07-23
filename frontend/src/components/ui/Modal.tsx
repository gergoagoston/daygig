import { useEffect, type ReactNode } from 'react';

type ModalProps = {
  children: ReactNode;
  isOpen: boolean;
  onClose: () => void;
  title: string;
  titleId: string;
};

function Modal({ children, isOpen, onClose, title, titleId }: ModalProps) {
  useEffect(() => {
    if (!isOpen) {
      return undefined;
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 grid bg-heading/45 p-4 sm:place-items-center">
      <div className="absolute inset-0" role="presentation" onClick={onClose} aria-hidden="true" />
      <section
        className="relative max-h-[calc(100svh-32px)] w-full max-w-3xl overflow-y-auto rounded-lg border border-border bg-surface shadow-panel sm:p-6"
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
      >
        <div>
          <h1 id={titleId} className="pb-3">
            {title}
          </h1>
        </div>

        {children}
      </section>
    </div>
  );
}

export default Modal;
