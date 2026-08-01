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
public final class AcknowledgeReminderUseCase_Factory implements Factory<AcknowledgeReminderUseCase> {
  private final Provider<ReminderRepository> repositoryProvider;

  public AcknowledgeReminderUseCase_Factory(Provider<ReminderRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AcknowledgeReminderUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static AcknowledgeReminderUseCase_Factory create(
      Provider<ReminderRepository> repositoryProvider) {
    return new AcknowledgeReminderUseCase_Factory(repositoryProvider);
  }

  public static AcknowledgeReminderUseCase newInstance(ReminderRepository repository) {
    return new AcknowledgeReminderUseCase(repository);
  }
}
