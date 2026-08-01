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
public final class GetMedicationsUseCase_Factory implements Factory<GetMedicationsUseCase> {
  private final Provider<MedicationRepository> repositoryProvider;

  public GetMedicationsUseCase_Factory(Provider<MedicationRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetMedicationsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetMedicationsUseCase_Factory create(
      Provider<MedicationRepository> repositoryProvider) {
    return new GetMedicationsUseCase_Factory(repositoryProvider);
  }

  public static GetMedicationsUseCase newInstance(MedicationRepository repository) {
    return new GetMedicationsUseCase(repository);
  }
}
