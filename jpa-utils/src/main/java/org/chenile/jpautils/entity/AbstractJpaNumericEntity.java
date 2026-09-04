package org.chenile.jpautils.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import org.chenile.core.context.ContextContainer;
import org.chenile.core.context.HeaderUtils;

import java.util.Date;

/** Common audit fields for additive, non-workflow numeric JPA entities. */
@MappedSuperclass
public abstract class AbstractJpaNumericEntity<ID extends Number> {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    public Date createdTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    private Date lastModifiedTime;
    private String lastModifiedBy;
    public String tenant;
    public String createdBy;
    @Transient public boolean testEntity;
    @Version public long version;

    public abstract ID getId();

    public abstract void setId(ID id);

    public Date getCreatedTime() { return createdTime; }

    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }

    public Date getLastModifiedTime() { return lastModifiedTime; }

    public void setLastModifiedTime(Date lastModifiedTime) { this.lastModifiedTime = lastModifiedTime; }

    public String getLastModifiedBy() { return lastModifiedBy; }

    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public String getCreatedBy() { return createdBy; }

    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Long getVersion() { return version; }

    public void setVersion(Long version) { this.version = version; }

    @PrePersist
    @PreUpdate
    protected void populateAuditFields() {
        ContextContainer contextContainer = ContextContainer.CONTEXT_CONTAINER;
        if (createdTime == null) createdTime = new Date();
        lastModifiedTime = new Date();
        lastModifiedBy = contextContainer.get(HeaderUtils.AUTH_USER_KEY);
        createdBy = contextContainer.get(HeaderUtils.AUTH_USER_KEY);
        tenant = contextContainer.get(HeaderUtils.TENANT_ID_KEY);
        testEntity = contextContainer.isTestMode();
    }
}
