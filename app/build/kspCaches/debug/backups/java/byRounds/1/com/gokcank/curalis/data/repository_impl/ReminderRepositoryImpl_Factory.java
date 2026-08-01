package com.gokcank.curalis.data.repository_impl;

import com.gokcank.curalis.data.local.dao.ReminderDao;
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
public final class ReminderRepositoryImpl_Factory implements Factory<ReminderRepositoryImpl> {
  private final Provider<ReminderDao> daoProvider;

  public ReminderRepositoryImpl_Factory(Provider<ReminderDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public ReminderRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static ReminderRepositoryImpl_Factory create(Provider<ReminderDao> daoProvider) {
    return new ReminderRepositoryImpl_Factory(daoProvider);
  }

  public static ReminderRepositoryImpl newInstance(ReminderDao dao) {
    return new ReminderRepositoryImpl(dao);
  }
}
