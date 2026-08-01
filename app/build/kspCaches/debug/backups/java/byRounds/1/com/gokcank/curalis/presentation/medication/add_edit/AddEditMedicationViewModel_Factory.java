package com.gokcank.curalis.presentation.medication.add_edit;

import androidx.lifecycle.SavedStateHandle;
import com.gokcank.curalis.domain.usecase.AddMedicationUseCase;
import com.gokcank.curalis.domain.usecase.GetMedicationByIdUseCase;
import com.gokcank.curalis.domain.usecase.UpdateMedicationUseCase;
import com.gokcank.curalis.domain.usecase.ValidateMedicationUseCase;
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
public final class AddEditMedicationViewModel_Factory implements Factory<AddEditMedicationViewModel> {
  private final Provider<GetMedicationByIdUseCase> getMedicationByIdUseCaseProvider;

  private final Provider<AddMedicationUseCase> addMedicationUseCaseProvider;

  private final Provider<UpdateMedicationUseCase> updateMedicationUseCaseProvider;

  private final Provider<ValidateMedicationUseCase> validateMedicationUseCaseProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public AddEditMedicationViewModel_Factory(
      Provider<GetMedicationByIdUseCase> getMedicationByIdUseCaseProvider,
      Provider<AddMedicationUseCase> addMedicationUseCaseProvider,
      Provider<UpdateMedicationUseCase> updateMedicationUseCaseProvider,
      Provider<ValidateMedicationUseCase> validateMedicationUseCaseProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.getMedicationByIdUseCaseProvider = getMedicationByIdUseCaseProvider;
    this.addMedicationUseCaseProvider = addMedicationUseCaseProvider;
    this.updateMedicationUseCaseProvider = updateMedicationUseCaseProvider;
    this.validateMedicationUseCaseProvider = validateMedicationUseCaseProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public AddEditMedicationViewModel get() {
    return newInstance(getMedicationByIdUseCaseProvider.get(), addMedicationUseCaseProvider.get(), updateMedicationUseCaseProvider.get(), validateMedicationUseCaseProvider.get(), savedStateHandleProvider.get());
  }

  public static AddEditMedicationViewModel_Factory create(
      Provider<GetMedicationByIdUseCase> getMedicationByIdUseCaseProvider,
      Provider<AddMedicationUseCase> addMedicationUseCaseProvider,
      Provider<UpdateMedicationUseCase> updateMedicationUseCaseProvider,
      Provider<ValidateMedicationUseCase> validateMedicationUseCaseProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new AddEditMedicationViewModel_Factory(getMedicationByIdUseCaseProvider, addMedicationUseCaseProvider, updateMedicationUseCaseProvider, validateMedicationUseCaseProvider, savedStateHandleProvider);
  }

  public static AddEditMedicationViewModel newInstance(
      GetMedicationByIdUseCase getMedicationByIdUseCase, AddMedicationUseCase addMedicationUseCase,
      UpdateMedicationUseCase updateMedicationUseCase,
      ValidateMedicationUseCase validateMedicationUseCase, SavedStateHandle savedStateHandle) {
    return new AddEditMedicationViewModel(getMedicationByIdUseCase, addMedicationUseCase, updateMedicationUseCase, validateMedicationUseCase, savedStateHandle);
  }
}
