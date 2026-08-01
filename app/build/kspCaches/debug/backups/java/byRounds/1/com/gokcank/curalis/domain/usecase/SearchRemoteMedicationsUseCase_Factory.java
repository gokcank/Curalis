package com.gokcank.curalis.domain.usecase;

import com.gokcank.curalis.data.provider.ProviderManager;
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
public final class SearchRemoteMedicationsUseCase_Factory implements Factory<SearchRemoteMedicationsUseCase> {
  private final Provider<ProviderManager> providerManagerProvider;

  public SearchRemoteMedicationsUseCase_Factory(Provider<ProviderManager> providerManagerProvider) {
    this.providerManagerProvider = providerManagerProvider;
  }

  @Override
  public SearchRemoteMedicationsUseCase get() {
    return newInstance(providerManagerProvider.get());
  }

  public static SearchRemoteMedicationsUseCase_Factory create(
      Provider<ProviderManager> providerManagerProvider) {
    return new SearchRemoteMedicationsUseCase_Factory(providerManagerProvider);
  }

  public static SearchRemoteMedicationsUseCase newInstance(ProviderManager providerManager) {
    return new SearchRemoteMedicationsUseCase(providerManager);
  }
}
