package com.taskflow.user_service.service;

import com.taskflow.user_service.domain.User;
import com.taskflow.user_service.domain.UserPreferences;
import com.taskflow.user_service.dto.request.UpdateAvatarRequest;
import com.taskflow.user_service.dto.request.UpdateUserRequest;
import com.taskflow.user_service.exception.InvalidUserException;
import com.taskflow.user_service.exception.UserNotFoundException;
import com.taskflow.user_service.mapper.UserMapper;
import com.taskflow.user_service.repository.UserRepository;
import com.taskflow.user_service.service.impl.UserServiceImpl;
import com.taskflow.user_service.util.AvatarImages;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;
import javax.imageio.ImageIO;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserProfileTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final UserMapper mapper;
    private final UserServiceImpl service;
    UserProfileTest() {
        var modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT).setSkipNullEnabled(true);
        mapper = new UserMapper(modelMapper);
        service = new UserServiceImpl(repository, mapper);
    }
    private User profile() {
        User user = User.builder().authUserId("signed-account").firstName("Alex").lastName("Morgan").email("alex@example.test").build();
        when(repository.findByAuthUserId("signed-account")).thenReturn(Optional.of(user));
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        return user;
    }
    private static String png(int width, int height) throws Exception {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", output);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
    }
    @Test void profileEditsPreserveTheCredentialIdentityAndPreferences() {
        User user = profile();
        var result = service.updateCurrentUser("signed-account", UpdateUserRequest.builder().firstName("Avery").bio("Building useful things.").phone("+1 555 0100").build());
        assertThat(result.getFullName()).isEqualTo("Avery Morgan");
        assertThat(result.getEmail()).isEqualTo("alex@example.test");
        assertThat(user.getAuthUserId()).isEqualTo("signed-account");
        assertThat(result.getPreferences().getTimeZone()).isEqualTo("UTC");
    }
    @Test void preferencesPersistAndLegacyProfilesReceiveDefaults() {
        User user = profile();
        user.setPreferences(null);
        assertThat(mapper.toResponse(user).getPreferences().getStartPage()).isEqualTo(UserPreferences.StartPage.DASHBOARD);
        var preferences = UserPreferences.builder().timeZone("America/Chicago").use24HourTime(true).build();
        assertThat(service.updatePreferences("signed-account", preferences).getPreferences().isUse24HourTime()).isTrue();
        assertThat(mapper.toSummary(user)).hasNoNullFieldsOrPropertiesExcept("id", "avatarUrl");
    }
    @Test void invalidPreferencesCannotSkipAccountLookupOrBePersisted() {
        User user = profile();
        UserPreferences original = user.getPreferences();
        UserPreferences[] invalid = {null,
                UserPreferences.builder().timeZone("not/a-time-zone").build(),
                UserPreferences.builder().timeZone(null).build(),
                UserPreferences.builder().timeZone("").build()};
        for (UserPreferences preferences : invalid) {
            assertThatThrownBy(() -> service.updatePreferences("signed-account", preferences))
                    .isInstanceOf(InvalidUserException.class)
                    .hasMessage("Choose a valid time zone");
        }
        verify(repository, times(invalid.length)).findByAuthUserId("signed-account");
        verify(repository, never()).save(any());
        assertThat(user.getPreferences()).isSameAs(original);
    }
    @Test void missingAccountsAreRejectedBeforePreferencesAreValidated() {
        when(repository.findByAuthUserId("missing-account")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updatePreferences("missing-account", null))
                .isInstanceOf(UserNotFoundException.class);
        verify(repository).findByAuthUserId("missing-account");
        verify(repository, never()).save(any());
    }
    @Test void missingAuthenticatedSubjectsAreRejectedBeforePreferencesAreValidated() {
        for (String authUserId : new String[]{null, "", " "}) {
            assertThatThrownBy(() -> service.updatePreferences(authUserId, null))
                    .isInstanceOf(InvalidUserException.class)
                    .hasMessage("Authenticated user ID is required");
        }
        verifyNoInteractions(repository);
    }
    @Test void acceptsRealSmallPngsAndAllowsPhotoRemoval() throws Exception {
        User user = profile();
        String image = png(256, 256);
        assertThat(service.updateAvatar("signed-account", new UpdateAvatarRequest(image)).getAvatarUrl()).isEqualTo(image);
        service.updateAvatar("signed-account", new UpdateAvatarRequest(null));
        assertThat(user.getAvatarUrl()).isNull();
    }
    @Test void rejectsExternalUrlsSvgMalformedAndOversizedImages() throws Exception {
        for (String value : new String[]{ "https://example.test/photo.png", "data:image/svg+xml;base64,PHN2Zy8+", "data:image/png;base64,not-base64", png(513, 1), "x".repeat(262145) }) {
            assertThatThrownBy(() -> AvatarImages.validate(value)).isInstanceOf(InvalidUserException.class);
        }
    }
}
