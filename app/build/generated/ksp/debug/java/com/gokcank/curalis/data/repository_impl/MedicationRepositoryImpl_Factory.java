package com.gokcank.curalis.data.repository_impl;

import com.gokcank.curalis.data.local.dao.MedicationDao;
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
public final class MedicationRepositoryImpl_Factory implements Factory<MedicationRepositoryImpl> {
  private final Provider<MedicationDao> daoProvider;

  public MedicationRepositoryImpl_Factory(Provider<MedicationDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public MedicationRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static MedicationRepositoryImpl_Factory create(Provider<MedicationDao> daoProvider) {
    return new MedicationRepositoryImpl_Factory(daoProvider);
  }

  public static MedicationRepositoryImpl newInstance(MedicationDao dao) {
    return new MedicationRepositoryImpl(dao);
  }
}
