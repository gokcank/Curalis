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
public final class UpdateMedicationUseCase_Factory implements Factory<UpdateMedicationUseCase> {
  private final Provider<MedicationRepository> repositoryProvider;

  public UpdateMedicationUseCase_Factory(Provider<MedicationRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public UpdateMedicationUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static UpdateMedicationUseCase_Factory create(
      Provider<MedicationRepository> repositoryProvider) {
    return new UpdateMedicationUseCase_Factory(repositoryProvider);
  }

  public static UpdateMedicationUseCase newInstance(MedicationRepository repository) {
    return new UpdateMedicationUseCase(repository);
  }
}
