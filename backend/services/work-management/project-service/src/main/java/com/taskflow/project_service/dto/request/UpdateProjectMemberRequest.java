
package com.taskflow.project_service.dto.request;

import com.taskflow.project_service.domain.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Required role payload for updating a nested membership document.
 * The service checks project scope and caller management rights, and forbids demoting an owner
 * or promoting another membership to OWNER. A platform admin role is not this DTO's project role.
 */
@Data
public class UpdateProjectMemberRequest {

    @NotNull(message = "Project role is required")
    private ProjectRole role;
}
