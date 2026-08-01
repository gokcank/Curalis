package com.gokcank.curalis.data.provider.openfda;

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
public final class OpenFdaProvider_Factory implements Factory<OpenFdaProvider> {
  private final Provider<OpenFdaApi> apiProvider;

  public OpenFdaProvider_Factory(Provider<OpenFdaApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public OpenFdaProvider get() {
    return newInstance(apiProvider.get());
  }

  public static OpenFdaProvider_Factory create(Provider<OpenFdaApi> apiProvider) {
    return new OpenFdaProvider_Factory(apiProvider);
  }

  public static OpenFdaProvider newInstance(OpenFdaApi api) {
    return new OpenFdaProvider(api);
  }
}
