// 
package  com.taskflow.project_service.domain.enums;

/**
 * Project visibility metadata serialized between the service and frontend.
 * Current ProjectServiceImpl still requires membership for reads; PUBLIC/TEAM/ORGANIZATION
 * labels do not implement anonymous discovery or organization-wide authorization.
 */
public enum ProjectVisibility {

    PUBLIC,

    TEAM,

    ORGANIZATION,

    PRIVATE
}
