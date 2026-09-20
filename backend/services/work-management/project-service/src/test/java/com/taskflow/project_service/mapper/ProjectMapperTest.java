package com.taskflow.project_service.mapper;

import com.taskflow.project_service.config.ModelMapperConfig;
import com.taskflow.project_service.domain.Project;
import com.taskflow.project_service.domain.ProjectMember;
import com.taskflow.project_service.domain.enums.ProjectRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectMapperTest {

    private final ProjectMapper mapper = new ProjectMapper(new ModelMapperConfig().modelMapper());

    private final Project project = Project.builder()
            .id("project-1")
            .name("TaskFlow")
            .ownerId("user-1")
            .build();

    @Test
    void mapsProjectSummaryForListRequests() {
        var response = mapper.toSummaryResponse(project, 3);

        assertThat(response.getId()).isEqualTo("project-1");
        assertThat(response.getName()).isEqualTo("TaskFlow");
        assertThat(response.getOwnerId()).isEqualTo("user-1");
        assertThat(response.getStatus()).isEqualTo(project.getStatus());
        assertThat(response.getMemberCount()).isEqualTo(3);
    }

    @Test
    void mapsProjectDetails() {
        var response = mapper.toResponse(project);

        assertThat(response.getId()).isEqualTo("project-1");
        assertThat(response.getName()).isEqualTo("TaskFlow");
        assertThat(response.getOwnerId()).isEqualTo("user-1");
    }

    @Test
    void mapsProjectMembers() {
        var member = ProjectMember.builder()
                .id("member-1")
                .projectId("project-1")
                .userId("user-1")
                .role(ProjectRole.OWNER)
                .build();

        var response = mapper.toMemberResponse(member);

        assertThat(response.getId()).isEqualTo("member-1");
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getRole()).isEqualTo(ProjectRole.OWNER);
    }
}
