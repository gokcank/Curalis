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
public final class SearchMedicationsUseCase_Factory implements Factory<SearchMedicationsUseCase> {
  private final Provider<MedicationRepository> repositoryProvider;

  public SearchMedicationsUseCase_Factory(Provider<MedicationRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SearchMedicationsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static SearchMedicationsUseCase_Factory create(
      Provider<MedicationRepository> repositoryProvider) {
    return new SearchMedicationsUseCase_Factory(repositoryProvider);
  }

  public static SearchMedicationsUseCase newInstance(MedicationRepository repository) {
    return new SearchMedicationsUseCase(repository);
  }
}
