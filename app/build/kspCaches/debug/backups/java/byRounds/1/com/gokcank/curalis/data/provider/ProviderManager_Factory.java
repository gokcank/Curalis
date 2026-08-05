package com.gokcank.curalis.data.provider;

import com.gokcank.curalis.data.provider.local.LocalMedicationProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ProviderManager_Factory implements Factory<ProviderManager> {
  private final Provider<LocalMedicationProvider> localMedicationProvider;

  public ProviderManager_Factory(Provider<LocalMedicationProvider> localMedicationProvider) {
    this.localMedicationProvider = localMedicationProvider;
  }

  @Override
  public ProviderManager get() {
    return newInstance(localMedicationProvider.get());
  }

  public static ProviderManager_Factory create(
      Provider<LocalMedicationProvider> localMedicationProvider) {
    return new ProviderManager_Factory(localMedicationProvider);
  }

  public static ProviderManager newInstance(LocalMedicationProvider localMedicationProvider) {
    return new ProviderManager(localMedicationProvider);
  }
}
