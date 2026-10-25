// 
package  com.taskflow.project_service.domain.enums;


/**
 * Project-scoped permissions: OWNER/ADMIN manage members and MANAGER also edits project metadata.
 * MEMBER/GUEST retain the view permissions currently derived by ProjectServiceImpl.
 * These roles differ from JWT platform ROLE_ADMIN/ROLE_USER; never authorize project changes
 * by matching only a similarly named platform authority.
 */
public enum ProjectRole {

    OWNER,

    ADMIN,

    MANAGER,

    MEMBER,

    // VIEWER,

    GUEST
}
