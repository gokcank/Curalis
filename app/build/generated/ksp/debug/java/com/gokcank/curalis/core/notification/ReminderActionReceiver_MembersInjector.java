package com.gokcank.curalis.core.notification;

import com.gokcank.curalis.domain.usecase.AcknowledgeReminderUseCase;
import com.gokcank.curalis.domain.usecase.ScheduleReminderUseCase;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ReminderActionReceiver_MembersInjector implements MembersInjector<ReminderActionReceiver> {
  private final Provider<AcknowledgeReminderUseCase> acknowledgeReminderUseCaseProvider;

  private final Provider<ScheduleReminderUseCase> scheduleReminderUseCaseProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  public ReminderActionReceiver_MembersInjector(
      Provider<AcknowledgeReminderUseCase> acknowledgeReminderUseCaseProvider,
      Provider<ScheduleReminderUseCase> scheduleReminderUseCaseProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    this.acknowledgeReminderUseCaseProvider = acknowledgeReminderUseCaseProvider;
    this.scheduleReminderUseCaseProvider = scheduleReminderUseCaseProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public static MembersInjector<ReminderActionReceiver> create(
      Provider<AcknowledgeReminderUseCase> acknowledgeReminderUseCaseProvider,
      Provider<ScheduleReminderUseCase> scheduleReminderUseCaseProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    return new ReminderActionReceiver_MembersInjector(acknowledgeReminderUseCaseProvider, scheduleReminderUseCaseProvider, alarmSchedulerProvider, notificationHelperProvider);
  }

  @Override
  public void injectMembers(ReminderActionReceiver instance) {
    injectAcknowledgeReminderUseCase(instance, acknowledgeReminderUseCaseProvider.get());
    injectScheduleReminderUseCase(instance, scheduleReminderUseCaseProvider.get());
    injectAlarmScheduler(instance, alarmSchedulerProvider.get());
    injectNotificationHelper(instance, notificationHelperProvider.get());
  }

  @InjectedFieldSignature("com.gokcank.curalis.core.notification.ReminderActionReceiver.acknowledgeReminderUseCase")
  public static void injectAcknowledgeReminderUseCase(ReminderActionReceiver instance,
      AcknowledgeReminderUseCase acknowledgeReminderUseCase) {
    instance.acknowledgeReminderUseCase = acknowledgeReminderUseCase;
  }

  @InjectedFieldSignature("com.gokcank.curalis.core.notification.ReminderActionReceiver.scheduleReminderUseCase")
  public static void injectScheduleReminderUseCase(ReminderActionReceiver instance,
      ScheduleReminderUseCase scheduleReminderUseCase) {
    instance.scheduleReminderUseCase = scheduleReminderUseCase;
  }

  @InjectedFieldSignature("com.gokcank.curalis.core.notification.ReminderActionReceiver.alarmScheduler")
  public static void injectAlarmScheduler(ReminderActionReceiver instance,
      AlarmScheduler alarmScheduler) {
    instance.alarmScheduler = alarmScheduler;
  }

  @InjectedFieldSignature("com.gokcank.curalis.core.notification.ReminderActionReceiver.notificationHelper")
  public static void injectNotificationHelper(ReminderActionReceiver instance,
      NotificationHelper notificationHelper) {
    instance.notificationHelper = notificationHelper;
  }
}
