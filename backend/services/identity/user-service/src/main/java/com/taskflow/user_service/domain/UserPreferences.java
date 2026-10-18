package com.taskflow.user_service.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Private, account-scoped display preferences; never included in directory summaries. */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserPreferences {
    public enum DateFormat { MONTH_DAY_YEAR, DAY_MONTH_YEAR, YEAR_MONTH_DAY }
    public enum StartPage { DASHBOARD, PROJECTS, TASKS, TEAMS }

    @NotBlank @Size(max = 64)
    @Builder.Default private String timeZone = "UTC";
    @NotNull @Builder.Default private DateFormat dateFormat = DateFormat.MONTH_DAY_YEAR;
    @NotNull @Builder.Default private StartPage startPage = StartPage.DASHBOARD;
    @Builder.Default private boolean use24HourTime = false;
}
