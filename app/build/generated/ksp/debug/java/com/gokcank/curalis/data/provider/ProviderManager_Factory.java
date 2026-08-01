package com.gokcank.curalis.data.provider;

import com.gokcank.curalis.data.provider.openfda.OpenFdaProvider;
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
  private final Provider<OpenFdaProvider> openFdaProvider;

  public ProviderManager_Factory(Provider<OpenFdaProvider> openFdaProvider) {
    this.openFdaProvider = openFdaProvider;
  }

  @Override
  public ProviderManager get() {
    return newInstance(openFdaProvider.get());
  }

  public static ProviderManager_Factory create(Provider<OpenFdaProvider> openFdaProvider) {
    return new ProviderManager_Factory(openFdaProvider);
  }

  public static ProviderManager newInstance(OpenFdaProvider openFdaProvider) {
    return new ProviderManager(openFdaProvider);
  }
}
