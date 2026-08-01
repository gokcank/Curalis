package com.gokcank.curalis.core.di;

import com.gokcank.curalis.data.provider.openfda.OpenFdaApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideOpenFdaApiFactory implements Factory<OpenFdaApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideOpenFdaApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public OpenFdaApi get() {
    return provideOpenFdaApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideOpenFdaApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideOpenFdaApiFactory(retrofitProvider);
  }

  public static OpenFdaApi provideOpenFdaApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideOpenFdaApi(retrofit));
  }
}
