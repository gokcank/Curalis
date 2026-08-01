package com.gokcank.curalis.core.di;

import com.gokcank.curalis.data.local.CuralisDatabase;
import com.gokcank.curalis.data.local.dao.MedicationDao;
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
public final class DatabaseModule_ProvideMedicationDaoFactory implements Factory<MedicationDao> {
  private final Provider<CuralisDatabase> dbProvider;

  public DatabaseModule_ProvideMedicationDaoFactory(Provider<CuralisDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MedicationDao get() {
    return provideMedicationDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideMedicationDaoFactory create(
      Provider<CuralisDatabase> dbProvider) {
    return new DatabaseModule_ProvideMedicationDaoFactory(dbProvider);
  }

  public static MedicationDao provideMedicationDao(CuralisDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMedicationDao(db));
  }
}
