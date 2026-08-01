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
public final class GetRemindersForMedicationUseCase_Factory implements Factory<GetRemindersForMedicationUseCase> {
  private final Provider<ReminderRepository> repositoryProvider;

  public GetRemindersForMedicationUseCase_Factory(Provider<ReminderRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetRemindersForMedicationUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetRemindersForMedicationUseCase_Factory create(
      Provider<ReminderRepository> repositoryProvider) {
    return new GetRemindersForMedicationUseCase_Factory(repositoryProvider);
  }

  public static GetRemindersForMedicationUseCase newInstance(ReminderRepository repository) {
    return new GetRemindersForMedicationUseCase(repository);
  }
}
