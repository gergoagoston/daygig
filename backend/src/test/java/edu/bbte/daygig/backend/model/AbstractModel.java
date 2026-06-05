package edu.bbte.daygig.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public abstract class AbstractModel {
    @Column(name = "uuid", unique = true, nullable = false, length = 36)
    private String uuid;

    @PrePersist
    public void prePersist() {
        getUuid();
    }

    public String getUuid() {
        if (uuid == null) {
            uuid = UUID
                .randomUUID()
                .toString();
        }
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractModel that = (AbstractModel) o;
        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }

}
