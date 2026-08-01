package com.gokcank.curalis.presentation.medication.list;

import com.gokcank.curalis.domain.usecase.DeleteMedicationUseCase;
import com.gokcank.curalis.domain.usecase.GetMedicationsUseCase;
import com.gokcank.curalis.domain.usecase.SearchMedicationsUseCase;
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
public final class MedicationListViewModel_Factory implements Factory<MedicationListViewModel> {
  private final Provider<GetMedicationsUseCase> getMedicationsUseCaseProvider;

  private final Provider<SearchMedicationsUseCase> searchMedicationsUseCaseProvider;

  private final Provider<DeleteMedicationUseCase> deleteMedicationUseCaseProvider;

  public MedicationListViewModel_Factory(
      Provider<GetMedicationsUseCase> getMedicationsUseCaseProvider,
      Provider<SearchMedicationsUseCase> searchMedicationsUseCaseProvider,
      Provider<DeleteMedicationUseCase> deleteMedicationUseCaseProvider) {
    this.getMedicationsUseCaseProvider = getMedicationsUseCaseProvider;
    this.searchMedicationsUseCaseProvider = searchMedicationsUseCaseProvider;
    this.deleteMedicationUseCaseProvider = deleteMedicationUseCaseProvider;
  }

  @Override
  public MedicationListViewModel get() {
    return newInstance(getMedicationsUseCaseProvider.get(), searchMedicationsUseCaseProvider.get(), deleteMedicationUseCaseProvider.get());
  }

  public static MedicationListViewModel_Factory create(
      Provider<GetMedicationsUseCase> getMedicationsUseCaseProvider,
      Provider<SearchMedicationsUseCase> searchMedicationsUseCaseProvider,
      Provider<DeleteMedicationUseCase> deleteMedicationUseCaseProvider) {
    return new MedicationListViewModel_Factory(getMedicationsUseCaseProvider, searchMedicationsUseCaseProvider, deleteMedicationUseCaseProvider);
  }

  public static MedicationListViewModel newInstance(GetMedicationsUseCase getMedicationsUseCase,
      SearchMedicationsUseCase searchMedicationsUseCase,
      DeleteMedicationUseCase deleteMedicationUseCase) {
    return new MedicationListViewModel(getMedicationsUseCase, searchMedicationsUseCase, deleteMedicationUseCase);
  }
}
