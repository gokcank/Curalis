package com.gokcank.curalis.domain.usecase;

import com.gokcank.curalis.domain.repository.MedicationRepository;
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
public final class AddMedicationUseCase_Factory implements Factory<AddMedicationUseCase> {
  private final Provider<MedicationRepository> repositoryProvider;

  public AddMedicationUseCase_Factory(Provider<MedicationRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AddMedicationUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static AddMedicationUseCase_Factory create(
      Provider<MedicationRepository> repositoryProvider) {
    return new AddMedicationUseCase_Factory(repositoryProvider);
  }

  public static AddMedicationUseCase newInstance(MedicationRepository repository) {
    return new AddMedicationUseCase(repository);
  }
}
