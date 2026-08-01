package com.gokcank.curalis.domain.usecase;

import com.gokcank.curalis.domain.repository.ReminderRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class ScheduleReminderUseCase_Factory implements Factory<ScheduleReminderUseCase> {
  private final Provider<ReminderRepository> repositoryProvider;

  public ScheduleReminderUseCase_Factory(Provider<ReminderRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ScheduleReminderUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ScheduleReminderUseCase_Factory create(
      Provider<ReminderRepository> repositoryProvider) {
    return new ScheduleReminderUseCase_Factory(repositoryProvider);
  }

  public static ScheduleReminderUseCase newInstance(ReminderRepository repository) {
    return new ScheduleReminderUseCase(repository);
  }
}
