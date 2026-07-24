import { useTranslation } from 'react-i18next';

import Modal from '../ui/Modal';
import JobCreateForm from './create/JobCreateForm';

type JobCreateModalProps = {
  isOpen: boolean;
  onClose: () => void;
};

function JobCreateModal({ isOpen, onClose }: JobCreateModalProps) {
  const { t } = useTranslation();

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={t('jobs.create.title')}
      titleId="create-job-title"
    >
      <JobCreateForm onCancel={onClose} />
    </Modal>
  );
}

export default JobCreateModal;
