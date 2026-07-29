import { useEffect, type ReactNode } from 'react';
import { createPortal } from 'react-dom';

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

  return createPortal(
    <div className="fixed inset-0 grid bg-heading/45 p-4 sm:place-items-center">
      <div className="absolute inset-0" role="presentation" onClick={onClose} aria-hidden="true" />
      <section
        className="relative max-h-[calc(100svh-32px)] w-full max-w-3xl overflow-y-auto rounded-lg border border-border bg-surface p-4 shadow-panel sm:p-6"
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
      >
        <div className="mb-5">
          <h1 id={titleId} className="m-0 text-3xl">
            {title}
          </h1>
        </div>

        {children}
      </section>
    </div>,
    document.body
  );
}

export default Modal;
