package com.gokcank.curalis.core.di;

import android.app.Application;
import com.gokcank.curalis.data.local.CuralisDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideCuralisDatabaseFactory implements Factory<CuralisDatabase> {
  private final Provider<Application> appProvider;

  public DatabaseModule_ProvideCuralisDatabaseFactory(Provider<Application> appProvider) {
    this.appProvider = appProvider;
  }

  @Override
  public CuralisDatabase get() {
    return provideCuralisDatabase(appProvider.get());
  }

  public static DatabaseModule_ProvideCuralisDatabaseFactory create(
      Provider<Application> appProvider) {
    return new DatabaseModule_ProvideCuralisDatabaseFactory(appProvider);
  }

  public static CuralisDatabase provideCuralisDatabase(Application app) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideCuralisDatabase(app));
  }
}
