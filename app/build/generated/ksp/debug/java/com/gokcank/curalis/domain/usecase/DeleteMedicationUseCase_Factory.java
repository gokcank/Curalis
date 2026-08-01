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
public final class DeleteMedicationUseCase_Factory implements Factory<DeleteMedicationUseCase> {
  private final Provider<MedicationRepository> repositoryProvider;

  public DeleteMedicationUseCase_Factory(Provider<MedicationRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeleteMedicationUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static DeleteMedicationUseCase_Factory create(
      Provider<MedicationRepository> repositoryProvider) {
    return new DeleteMedicationUseCase_Factory(repositoryProvider);
  }

  public static DeleteMedicationUseCase newInstance(MedicationRepository repository) {
    return new DeleteMedicationUseCase(repository);
  }
}
