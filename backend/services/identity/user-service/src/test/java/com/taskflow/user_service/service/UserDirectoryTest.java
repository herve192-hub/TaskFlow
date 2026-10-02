package com.taskflow.user_service.service;

import com.taskflow.user_service.domain.User;
import com.taskflow.user_service.domain.enums.UserStatus;
import com.taskflow.user_service.dto.response.UserSummaryResponse;
import com.taskflow.user_service.exception.InvalidUserException;
import com.taskflow.user_service.mapper.UserMapper;
import com.taskflow.user_service.repository.UserRepository;
import com.taskflow.user_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserDirectoryTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final UserMapper mapper = mock(UserMapper.class);
    private final UserServiceImpl service = new UserServiceImpl(repository, mapper);

    @Test
    void emptySearchListsOnlyActiveProfilesWithPagination() {
        var pageable = PageRequest.of(1, 20);
        var user = new User();
        var summary = UserSummaryResponse.builder().authUserId("auth-user-1").build();
        when(repository.findByStatus(UserStatus.ACTIVE, pageable)).thenReturn(new PageImpl<>(List.of(user), pageable, 21));
        when(mapper.toSummary(user)).thenReturn(summary);

        var result = service.searchUsers("  ", pageable);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(21);
        assertThat(result.getContent()).containsExactly(summary);
        verify(repository, never()).findAll(any(PageRequest.class));
    }

    @Test
    void searchTreatsRegexCharactersInNamesAndEmailsLiterally() {
        var pageable = PageRequest.of(0, 20);
        String query = "jane+team@example.com";
        when(repository.searchActiveUsers(Pattern.quote(query), pageable)).thenReturn(new PageImpl<>(List.of()));

        service.searchUsers(" " + query + " ", pageable);

        verify(repository).searchActiveUsers(Pattern.quote(query), pageable);
    }

    @Test
    void lookupUsesCanonicalAuthIdsAndKeepsInactiveMembersVisible() {
        var user = new User();
        var summary = UserSummaryResponse.builder().authUserId("auth-user-1").status(UserStatus.INACTIVE).build();
        when(repository.findByAuthUserIdIn(List.of("auth-user-1"))).thenReturn(List.of(user));
        when(mapper.toSummary(user)).thenReturn(summary);

        var result = service.lookupUsers(List.of("auth-user-1", "auth-user-1"));

        assertThat(result).containsExactly(summary);
        verify(repository).findByAuthUserIdIn(List.of("auth-user-1"));
    }

    @Test
    void emptyLookupDoesNotQueryTheDirectory() {
        assertThat(service.lookupUsers(List.of())).isEmpty();
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsBlankIdsAndOversizedSearchesOrLookups() {
        assertThatThrownBy(() -> service.lookupUsers(List.of(" "))).isInstanceOf(InvalidUserException.class);
        assertThatThrownBy(() -> service.lookupUsers(Collections.nCopies(101, "auth-user-1")))
                .isInstanceOf(InvalidUserException.class);
        assertThatThrownBy(() -> service.searchUsers("a".repeat(101), PageRequest.of(0, 20)))
                .isInstanceOf(InvalidUserException.class);
        verifyNoInteractions(repository);
    }
}
