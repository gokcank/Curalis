package com.gokcank.curalis;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.gokcank.curalis.core.di.AppModule_ProvideGsonFactory;
import com.gokcank.curalis.core.di.DatabaseModule_ProvideAppointmentDaoFactory;
import com.gokcank.curalis.core.di.DatabaseModule_ProvideCuralisDatabaseFactory;
import com.gokcank.curalis.core.di.DatabaseModule_ProvideDoctorDaoFactory;
import com.gokcank.curalis.core.di.DatabaseModule_ProvideDrugDaoFactory;
import com.gokcank.curalis.core.di.DatabaseModule_ProvideMedicationDaoFactory;
import com.gokcank.curalis.core.di.DatabaseModule_ProvideMedicationDictionaryDatabaseFactory;
import com.gokcank.curalis.core.di.DatabaseModule_ProvideReminderDaoFactory;
import com.gokcank.curalis.core.di.DatabaseModule_ProvideVitalDaoFactory;
import com.gokcank.curalis.core.notification.AlarmScheduler;
import com.gokcank.curalis.core.notification.NotificationHelper;
import com.gokcank.curalis.core.notification.ReminderActionReceiver;
import com.gokcank.curalis.core.notification.ReminderActionReceiver_MembersInjector;
import com.gokcank.curalis.core.notification.ReminderReceiver;
import com.gokcank.curalis.core.notification.ReminderReceiver_MembersInjector;
import com.gokcank.curalis.data.backup.LocalBackupManager;
import com.gokcank.curalis.data.local.CuralisDatabase;
import com.gokcank.curalis.data.local.MedicationDictionaryDatabase;
import com.gokcank.curalis.data.local.dao.AppointmentDao;
import com.gokcank.curalis.data.local.dao.DoctorDao;
import com.gokcank.curalis.data.local.dao.DrugDao;
import com.gokcank.curalis.data.local.dao.MedicationDao;
import com.gokcank.curalis.data.local.dao.ReminderDao;
import com.gokcank.curalis.data.local.dao.VitalDao;
import com.gokcank.curalis.data.provider.ProviderManager;
import com.gokcank.curalis.data.provider.local.LocalMedicationProvider;
import com.gokcank.curalis.data.repository.AppointmentRepositoryImpl;
import com.gokcank.curalis.data.repository.DoctorRepositoryImpl;
import com.gokcank.curalis.data.repository.VitalRepositoryImpl;
import com.gokcank.curalis.data.repository_impl.MedicationRepositoryImpl;
import com.gokcank.curalis.data.repository_impl.ReminderRepositoryImpl;
import com.gokcank.curalis.domain.repository.BackupManager;
import com.gokcank.curalis.domain.repository.MedicationRepository;
import com.gokcank.curalis.domain.repository.ReminderRepository;
import com.gokcank.curalis.domain.usecase.AcknowledgeReminderUseCase;
import com.gokcank.curalis.domain.usecase.AddAppointmentUseCase;
import com.gokcank.curalis.domain.usecase.AddDoctorUseCase;
import com.gokcank.curalis.domain.usecase.AddMedicationUseCase;
import com.gokcank.curalis.domain.usecase.AddVitalUseCase;
import com.gokcank.curalis.domain.usecase.AppointmentUseCases;
import com.gokcank.curalis.domain.usecase.DeleteAppointmentUseCase;
import com.gokcank.curalis.domain.usecase.DeleteDoctorUseCase;
import com.gokcank.curalis.domain.usecase.DeleteMedicationUseCase;
import com.gokcank.curalis.domain.usecase.DeleteVitalUseCase;
import com.gokcank.curalis.domain.usecase.DoctorUseCases;
import com.gokcank.curalis.domain.usecase.GetAppointmentByIdUseCase;
import com.gokcank.curalis.domain.usecase.GetAppointmentsUseCase;
import com.gokcank.curalis.domain.usecase.GetDoctorByIdUseCase;
import com.gokcank.curalis.domain.usecase.GetDoctorsUseCase;
import com.gokcank.curalis.domain.usecase.GetMedicationByIdUseCase;
import com.gokcank.curalis.domain.usecase.GetMedicationsUseCase;
import com.gokcank.curalis.domain.usecase.GetUpcomingAppointmentsUseCase;
import com.gokcank.curalis.domain.usecase.GetVitalByIdUseCase;
import com.gokcank.curalis.domain.usecase.GetVitalsByTypeUseCase;
import com.gokcank.curalis.domain.usecase.GetVitalsUseCase;
import com.gokcank.curalis.domain.usecase.ScheduleReminderUseCase;
import com.gokcank.curalis.domain.usecase.SearchMedicationsUseCase;
import com.gokcank.curalis.domain.usecase.SearchRemoteMedicationsUseCase;
import com.gokcank.curalis.domain.usecase.UpdateAppointmentUseCase;
import com.gokcank.curalis.domain.usecase.UpdateDoctorUseCase;
import com.gokcank.curalis.domain.usecase.UpdateMedicationUseCase;
import com.gokcank.curalis.domain.usecase.ValidateMedicationUseCase;
import com.gokcank.curalis.domain.usecase.VitalUseCases;
import com.gokcank.curalis.presentation.appointment.AddEditAppointmentViewModel;
import com.gokcank.curalis.presentation.appointment.AddEditAppointmentViewModel_HiltModules;
import com.gokcank.curalis.presentation.appointment.AppointmentListViewModel;
import com.gokcank.curalis.presentation.appointment.AppointmentListViewModel_HiltModules;
import com.gokcank.curalis.presentation.backup.BackupViewModel;
import com.gokcank.curalis.presentation.backup.BackupViewModel_HiltModules;
import com.gokcank.curalis.presentation.doctor.AddEditDoctorViewModel;
import com.gokcank.curalis.presentation.doctor.AddEditDoctorViewModel_HiltModules;
import com.gokcank.curalis.presentation.doctor.DoctorListViewModel;
import com.gokcank.curalis.presentation.doctor.DoctorListViewModel_HiltModules;
import com.gokcank.curalis.presentation.home.HomeViewModel;
import com.gokcank.curalis.presentation.home.HomeViewModel_HiltModules;
import com.gokcank.curalis.presentation.main.MainActivity;
import com.gokcank.curalis.presentation.medication.add_edit.AddEditMedicationViewModel;
import com.gokcank.curalis.presentation.medication.add_edit.AddEditMedicationViewModel_HiltModules;
import com.gokcank.curalis.presentation.medication.list.MedicationListViewModel;
import com.gokcank.curalis.presentation.medication.list.MedicationListViewModel_HiltModules;
import com.gokcank.curalis.presentation.vital.AddEditVitalViewModel;
import com.gokcank.curalis.presentation.vital.AddEditVitalViewModel_HiltModules;
import com.gokcank.curalis.presentation.vital.VitalListViewModel;
import com.gokcank.curalis.presentation.vital.VitalListViewModel_HiltModules;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideApplicationFactory;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerCuralisApp_HiltComponents_SingletonC {
  private DaggerCuralisApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public CuralisApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements CuralisApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public CuralisApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements CuralisApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public CuralisApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements CuralisApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public CuralisApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements CuralisApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CuralisApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements CuralisApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CuralisApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements CuralisApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public CuralisApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements CuralisApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public CuralisApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends CuralisApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends CuralisApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends CuralisApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends CuralisApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>builderWithExpectedSize(10).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_appointment_AddEditAppointmentViewModel, AddEditAppointmentViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_doctor_AddEditDoctorViewModel, AddEditDoctorViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_medication_add_edit_AddEditMedicationViewModel, AddEditMedicationViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_vital_AddEditVitalViewModel, AddEditVitalViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_appointment_AppointmentListViewModel, AppointmentListViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_backup_BackupViewModel, BackupViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_doctor_DoctorListViewModel, DoctorListViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_home_HomeViewModel, HomeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_medication_list_MedicationListViewModel, MedicationListViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_vital_VitalListViewModel, VitalListViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_gokcank_curalis_presentation_medication_add_edit_AddEditMedicationViewModel = "com.gokcank.curalis.presentation.medication.add_edit.AddEditMedicationViewModel";

      static String com_gokcank_curalis_presentation_doctor_AddEditDoctorViewModel = "com.gokcank.curalis.presentation.doctor.AddEditDoctorViewModel";

      static String com_gokcank_curalis_presentation_backup_BackupViewModel = "com.gokcank.curalis.presentation.backup.BackupViewModel";

      static String com_gokcank_curalis_presentation_doctor_DoctorListViewModel = "com.gokcank.curalis.presentation.doctor.DoctorListViewModel";

      static String com_gokcank_curalis_presentation_vital_AddEditVitalViewModel = "com.gokcank.curalis.presentation.vital.AddEditVitalViewModel";

      static String com_gokcank_curalis_presentation_appointment_AppointmentListViewModel = "com.gokcank.curalis.presentation.appointment.AppointmentListViewModel";

      static String com_gokcank_curalis_presentation_home_HomeViewModel = "com.gokcank.curalis.presentation.home.HomeViewModel";

      static String com_gokcank_curalis_presentation_vital_VitalListViewModel = "com.gokcank.curalis.presentation.vital.VitalListViewModel";

      static String com_gokcank_curalis_presentation_appointment_AddEditAppointmentViewModel = "com.gokcank.curalis.presentation.appointment.AddEditAppointmentViewModel";

      static String com_gokcank_curalis_presentation_medication_list_MedicationListViewModel = "com.gokcank.curalis.presentation.medication.list.MedicationListViewModel";

      @KeepFieldType
      AddEditMedicationViewModel com_gokcank_curalis_presentation_medication_add_edit_AddEditMedicationViewModel2;

      @KeepFieldType
      AddEditDoctorViewModel com_gokcank_curalis_presentation_doctor_AddEditDoctorViewModel2;

      @KeepFieldType
      BackupViewModel com_gokcank_curalis_presentation_backup_BackupViewModel2;

      @KeepFieldType
      DoctorListViewModel com_gokcank_curalis_presentation_doctor_DoctorListViewModel2;

      @KeepFieldType
      AddEditVitalViewModel com_gokcank_curalis_presentation_vital_AddEditVitalViewModel2;

      @KeepFieldType
      AppointmentListViewModel com_gokcank_curalis_presentation_appointment_AppointmentListViewModel2;

      @KeepFieldType
      HomeViewModel com_gokcank_curalis_presentation_home_HomeViewModel2;

      @KeepFieldType
      VitalListViewModel com_gokcank_curalis_presentation_vital_VitalListViewModel2;

      @KeepFieldType
      AddEditAppointmentViewModel com_gokcank_curalis_presentation_appointment_AddEditAppointmentViewModel2;

      @KeepFieldType
      MedicationListViewModel com_gokcank_curalis_presentation_medication_list_MedicationListViewModel2;
    }
  }

  private static final class ViewModelCImpl extends CuralisApp_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AddEditAppointmentViewModel> addEditAppointmentViewModelProvider;

    private Provider<AddEditDoctorViewModel> addEditDoctorViewModelProvider;

    private Provider<AddEditMedicationViewModel> addEditMedicationViewModelProvider;

    private Provider<AddEditVitalViewModel> addEditVitalViewModelProvider;

    private Provider<AppointmentListViewModel> appointmentListViewModelProvider;

    private Provider<BackupViewModel> backupViewModelProvider;

    private Provider<DoctorListViewModel> doctorListViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<MedicationListViewModel> medicationListViewModelProvider;

    private Provider<VitalListViewModel> vitalListViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private GetAppointmentsUseCase getAppointmentsUseCase() {
      return new GetAppointmentsUseCase(singletonCImpl.appointmentRepositoryImplProvider.get());
    }

    private GetAppointmentByIdUseCase getAppointmentByIdUseCase() {
      return new GetAppointmentByIdUseCase(singletonCImpl.appointmentRepositoryImplProvider.get());
    }

    private GetUpcomingAppointmentsUseCase getUpcomingAppointmentsUseCase() {
      return new GetUpcomingAppointmentsUseCase(singletonCImpl.appointmentRepositoryImplProvider.get());
    }

    private AddAppointmentUseCase addAppointmentUseCase() {
      return new AddAppointmentUseCase(singletonCImpl.appointmentRepositoryImplProvider.get());
    }

    private UpdateAppointmentUseCase updateAppointmentUseCase() {
      return new UpdateAppointmentUseCase(singletonCImpl.appointmentRepositoryImplProvider.get());
    }

    private DeleteAppointmentUseCase deleteAppointmentUseCase() {
      return new DeleteAppointmentUseCase(singletonCImpl.appointmentRepositoryImplProvider.get());
    }

    private AppointmentUseCases appointmentUseCases() {
      return new AppointmentUseCases(getAppointmentsUseCase(), getAppointmentByIdUseCase(), getUpcomingAppointmentsUseCase(), addAppointmentUseCase(), updateAppointmentUseCase(), deleteAppointmentUseCase());
    }

    private GetDoctorsUseCase getDoctorsUseCase() {
      return new GetDoctorsUseCase(singletonCImpl.doctorRepositoryImplProvider.get());
    }

    private GetDoctorByIdUseCase getDoctorByIdUseCase() {
      return new GetDoctorByIdUseCase(singletonCImpl.doctorRepositoryImplProvider.get());
    }

    private AddDoctorUseCase addDoctorUseCase() {
      return new AddDoctorUseCase(singletonCImpl.doctorRepositoryImplProvider.get());
    }

    private DeleteDoctorUseCase deleteDoctorUseCase() {
      return new DeleteDoctorUseCase(singletonCImpl.doctorRepositoryImplProvider.get());
    }

    private UpdateDoctorUseCase updateDoctorUseCase() {
      return new UpdateDoctorUseCase(singletonCImpl.doctorRepositoryImplProvider.get());
    }

    private DoctorUseCases doctorUseCases() {
      return new DoctorUseCases(getDoctorsUseCase(), getDoctorByIdUseCase(), addDoctorUseCase(), deleteDoctorUseCase(), updateDoctorUseCase());
    }

    private GetMedicationByIdUseCase getMedicationByIdUseCase() {
      return new GetMedicationByIdUseCase(singletonCImpl.bindMedicationRepositoryProvider.get());
    }

    private AddMedicationUseCase addMedicationUseCase() {
      return new AddMedicationUseCase(singletonCImpl.bindMedicationRepositoryProvider.get());
    }

    private UpdateMedicationUseCase updateMedicationUseCase() {
      return new UpdateMedicationUseCase(singletonCImpl.bindMedicationRepositoryProvider.get());
    }

    private SearchRemoteMedicationsUseCase searchRemoteMedicationsUseCase() {
      return new SearchRemoteMedicationsUseCase(singletonCImpl.providerManagerProvider.get());
    }

    private GetVitalsUseCase getVitalsUseCase() {
      return new GetVitalsUseCase(singletonCImpl.vitalRepositoryImplProvider.get());
    }

    private GetVitalsByTypeUseCase getVitalsByTypeUseCase() {
      return new GetVitalsByTypeUseCase(singletonCImpl.vitalRepositoryImplProvider.get());
    }

    private GetVitalByIdUseCase getVitalByIdUseCase() {
      return new GetVitalByIdUseCase(singletonCImpl.vitalRepositoryImplProvider.get());
    }

    private AddVitalUseCase addVitalUseCase() {
      return new AddVitalUseCase(singletonCImpl.vitalRepositoryImplProvider.get());
    }

    private DeleteVitalUseCase deleteVitalUseCase() {
      return new DeleteVitalUseCase(singletonCImpl.vitalRepositoryImplProvider.get());
    }

    private VitalUseCases vitalUseCases() {
      return new VitalUseCases(getVitalsUseCase(), getVitalsByTypeUseCase(), getVitalByIdUseCase(), addVitalUseCase(), deleteVitalUseCase());
    }

    private GetMedicationsUseCase getMedicationsUseCase() {
      return new GetMedicationsUseCase(singletonCImpl.bindMedicationRepositoryProvider.get());
    }

    private SearchMedicationsUseCase searchMedicationsUseCase() {
      return new SearchMedicationsUseCase(singletonCImpl.bindMedicationRepositoryProvider.get());
    }

    private DeleteMedicationUseCase deleteMedicationUseCase() {
      return new DeleteMedicationUseCase(singletonCImpl.bindMedicationRepositoryProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.addEditAppointmentViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.addEditDoctorViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.addEditMedicationViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.addEditVitalViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.appointmentListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.backupViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.doctorListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.medicationListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.vitalListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(10).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_appointment_AddEditAppointmentViewModel, ((Provider) addEditAppointmentViewModelProvider)).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_doctor_AddEditDoctorViewModel, ((Provider) addEditDoctorViewModelProvider)).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_medication_add_edit_AddEditMedicationViewModel, ((Provider) addEditMedicationViewModelProvider)).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_vital_AddEditVitalViewModel, ((Provider) addEditVitalViewModelProvider)).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_appointment_AppointmentListViewModel, ((Provider) appointmentListViewModelProvider)).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_backup_BackupViewModel, ((Provider) backupViewModelProvider)).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_doctor_DoctorListViewModel, ((Provider) doctorListViewModelProvider)).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_home_HomeViewModel, ((Provider) homeViewModelProvider)).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_medication_list_MedicationListViewModel, ((Provider) medicationListViewModelProvider)).put(LazyClassKeyProvider.com_gokcank_curalis_presentation_vital_VitalListViewModel, ((Provider) vitalListViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_gokcank_curalis_presentation_medication_list_MedicationListViewModel = "com.gokcank.curalis.presentation.medication.list.MedicationListViewModel";

      static String com_gokcank_curalis_presentation_appointment_AddEditAppointmentViewModel = "com.gokcank.curalis.presentation.appointment.AddEditAppointmentViewModel";

      static String com_gokcank_curalis_presentation_doctor_AddEditDoctorViewModel = "com.gokcank.curalis.presentation.doctor.AddEditDoctorViewModel";

      static String com_gokcank_curalis_presentation_vital_VitalListViewModel = "com.gokcank.curalis.presentation.vital.VitalListViewModel";

      static String com_gokcank_curalis_presentation_appointment_AppointmentListViewModel = "com.gokcank.curalis.presentation.appointment.AppointmentListViewModel";

      static String com_gokcank_curalis_presentation_backup_BackupViewModel = "com.gokcank.curalis.presentation.backup.BackupViewModel";

      static String com_gokcank_curalis_presentation_medication_add_edit_AddEditMedicationViewModel = "com.gokcank.curalis.presentation.medication.add_edit.AddEditMedicationViewModel";

      static String com_gokcank_curalis_presentation_vital_AddEditVitalViewModel = "com.gokcank.curalis.presentation.vital.AddEditVitalViewModel";

      static String com_gokcank_curalis_presentation_doctor_DoctorListViewModel = "com.gokcank.curalis.presentation.doctor.DoctorListViewModel";

      static String com_gokcank_curalis_presentation_home_HomeViewModel = "com.gokcank.curalis.presentation.home.HomeViewModel";

      @KeepFieldType
      MedicationListViewModel com_gokcank_curalis_presentation_medication_list_MedicationListViewModel2;

      @KeepFieldType
      AddEditAppointmentViewModel com_gokcank_curalis_presentation_appointment_AddEditAppointmentViewModel2;

      @KeepFieldType
      AddEditDoctorViewModel com_gokcank_curalis_presentation_doctor_AddEditDoctorViewModel2;

      @KeepFieldType
      VitalListViewModel com_gokcank_curalis_presentation_vital_VitalListViewModel2;

      @KeepFieldType
      AppointmentListViewModel com_gokcank_curalis_presentation_appointment_AppointmentListViewModel2;

      @KeepFieldType
      BackupViewModel com_gokcank_curalis_presentation_backup_BackupViewModel2;

      @KeepFieldType
      AddEditMedicationViewModel com_gokcank_curalis_presentation_medication_add_edit_AddEditMedicationViewModel2;

      @KeepFieldType
      AddEditVitalViewModel com_gokcank_curalis_presentation_vital_AddEditVitalViewModel2;

      @KeepFieldType
      DoctorListViewModel com_gokcank_curalis_presentation_doctor_DoctorListViewModel2;

      @KeepFieldType
      HomeViewModel com_gokcank_curalis_presentation_home_HomeViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.gokcank.curalis.presentation.appointment.AddEditAppointmentViewModel 
          return (T) new AddEditAppointmentViewModel(viewModelCImpl.appointmentUseCases(), viewModelCImpl.savedStateHandle);

          case 1: // com.gokcank.curalis.presentation.doctor.AddEditDoctorViewModel 
          return (T) new AddEditDoctorViewModel(viewModelCImpl.doctorUseCases(), viewModelCImpl.savedStateHandle);

          case 2: // com.gokcank.curalis.presentation.medication.add_edit.AddEditMedicationViewModel 
          return (T) new AddEditMedicationViewModel(viewModelCImpl.getMedicationByIdUseCase(), viewModelCImpl.addMedicationUseCase(), viewModelCImpl.updateMedicationUseCase(), new ValidateMedicationUseCase(), singletonCImpl.scheduleReminderUseCase(), viewModelCImpl.searchRemoteMedicationsUseCase(), singletonCImpl.alarmScheduler(), viewModelCImpl.savedStateHandle);

          case 3: // com.gokcank.curalis.presentation.vital.AddEditVitalViewModel 
          return (T) new AddEditVitalViewModel(viewModelCImpl.vitalUseCases());

          case 4: // com.gokcank.curalis.presentation.appointment.AppointmentListViewModel 
          return (T) new AppointmentListViewModel(viewModelCImpl.appointmentUseCases());

          case 5: // com.gokcank.curalis.presentation.backup.BackupViewModel 
          return (T) new BackupViewModel(singletonCImpl.bindBackupManagerProvider.get());

          case 6: // com.gokcank.curalis.presentation.doctor.DoctorListViewModel 
          return (T) new DoctorListViewModel(viewModelCImpl.doctorUseCases());

          case 7: // com.gokcank.curalis.presentation.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.bindReminderRepositoryProvider.get(), singletonCImpl.appointmentRepositoryImplProvider.get(), singletonCImpl.vitalRepositoryImplProvider.get(), singletonCImpl.bindMedicationRepositoryProvider.get());

          case 8: // com.gokcank.curalis.presentation.medication.list.MedicationListViewModel 
          return (T) new MedicationListViewModel(viewModelCImpl.getMedicationsUseCase(), viewModelCImpl.searchMedicationsUseCase(), viewModelCImpl.deleteMedicationUseCase());

          case 9: // com.gokcank.curalis.presentation.vital.VitalListViewModel 
          return (T) new VitalListViewModel(viewModelCImpl.vitalUseCases());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends CuralisApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends CuralisApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends CuralisApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<CuralisDatabase> provideCuralisDatabaseProvider;

    private Provider<ReminderDao> provideReminderDaoProvider;

    private Provider<ReminderRepositoryImpl> reminderRepositoryImplProvider;

    private Provider<ReminderRepository> bindReminderRepositoryProvider;

    private Provider<AppointmentDao> provideAppointmentDaoProvider;

    private Provider<AppointmentRepositoryImpl> appointmentRepositoryImplProvider;

    private Provider<DoctorDao> provideDoctorDaoProvider;

    private Provider<DoctorRepositoryImpl> doctorRepositoryImplProvider;

    private Provider<MedicationDao> provideMedicationDaoProvider;

    private Provider<MedicationRepositoryImpl> medicationRepositoryImplProvider;

    private Provider<MedicationRepository> bindMedicationRepositoryProvider;

    private Provider<MedicationDictionaryDatabase> provideMedicationDictionaryDatabaseProvider;

    private Provider<DrugDao> provideDrugDaoProvider;

    private Provider<LocalMedicationProvider> localMedicationProvider;

    private Provider<ProviderManager> providerManagerProvider;

    private Provider<VitalDao> provideVitalDaoProvider;

    private Provider<VitalRepositoryImpl> vitalRepositoryImplProvider;

    private Provider<Gson> provideGsonProvider;

    private Provider<LocalBackupManager> localBackupManagerProvider;

    private Provider<BackupManager> bindBackupManagerProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private AcknowledgeReminderUseCase acknowledgeReminderUseCase() {
      return new AcknowledgeReminderUseCase(bindReminderRepositoryProvider.get());
    }

    private ScheduleReminderUseCase scheduleReminderUseCase() {
      return new ScheduleReminderUseCase(bindReminderRepositoryProvider.get());
    }

    private AlarmScheduler alarmScheduler() {
      return new AlarmScheduler(ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule));
    }

    private NotificationHelper notificationHelper() {
      return new NotificationHelper(ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule));
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideCuralisDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<CuralisDatabase>(singletonCImpl, 2));
      this.provideReminderDaoProvider = DoubleCheck.provider(new SwitchingProvider<ReminderDao>(singletonCImpl, 1));
      this.reminderRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 0);
      this.bindReminderRepositoryProvider = DoubleCheck.provider((Provider) reminderRepositoryImplProvider);
      this.provideAppointmentDaoProvider = DoubleCheck.provider(new SwitchingProvider<AppointmentDao>(singletonCImpl, 4));
      this.appointmentRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AppointmentRepositoryImpl>(singletonCImpl, 3));
      this.provideDoctorDaoProvider = DoubleCheck.provider(new SwitchingProvider<DoctorDao>(singletonCImpl, 6));
      this.doctorRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<DoctorRepositoryImpl>(singletonCImpl, 5));
      this.provideMedicationDaoProvider = DoubleCheck.provider(new SwitchingProvider<MedicationDao>(singletonCImpl, 8));
      this.medicationRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 7);
      this.bindMedicationRepositoryProvider = DoubleCheck.provider((Provider) medicationRepositoryImplProvider);
      this.provideMedicationDictionaryDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<MedicationDictionaryDatabase>(singletonCImpl, 12));
      this.provideDrugDaoProvider = DoubleCheck.provider(new SwitchingProvider<DrugDao>(singletonCImpl, 11));
      this.localMedicationProvider = DoubleCheck.provider(new SwitchingProvider<LocalMedicationProvider>(singletonCImpl, 10));
      this.providerManagerProvider = DoubleCheck.provider(new SwitchingProvider<ProviderManager>(singletonCImpl, 9));
      this.provideVitalDaoProvider = DoubleCheck.provider(new SwitchingProvider<VitalDao>(singletonCImpl, 14));
      this.vitalRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<VitalRepositoryImpl>(singletonCImpl, 13));
      this.provideGsonProvider = DoubleCheck.provider(new SwitchingProvider<Gson>(singletonCImpl, 16));
      this.localBackupManagerProvider = new SwitchingProvider<>(singletonCImpl, 15);
      this.bindBackupManagerProvider = DoubleCheck.provider((Provider) localBackupManagerProvider);
    }

    @Override
    public void injectCuralisApp(CuralisApp curalisApp) {
    }

    @Override
    public void injectReminderActionReceiver(ReminderActionReceiver reminderActionReceiver) {
      injectReminderActionReceiver2(reminderActionReceiver);
    }

    @Override
    public void injectReminderReceiver(ReminderReceiver reminderReceiver) {
      injectReminderReceiver2(reminderReceiver);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private ReminderActionReceiver injectReminderActionReceiver2(ReminderActionReceiver instance) {
      ReminderActionReceiver_MembersInjector.injectAcknowledgeReminderUseCase(instance, acknowledgeReminderUseCase());
      ReminderActionReceiver_MembersInjector.injectScheduleReminderUseCase(instance, scheduleReminderUseCase());
      ReminderActionReceiver_MembersInjector.injectAlarmScheduler(instance, alarmScheduler());
      ReminderActionReceiver_MembersInjector.injectNotificationHelper(instance, notificationHelper());
      return instance;
    }

    @CanIgnoreReturnValue
    private ReminderReceiver injectReminderReceiver2(ReminderReceiver instance) {
      ReminderReceiver_MembersInjector.injectNotificationHelper(instance, notificationHelper());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.gokcank.curalis.data.repository_impl.ReminderRepositoryImpl 
          return (T) new ReminderRepositoryImpl(singletonCImpl.provideReminderDaoProvider.get());

          case 1: // com.gokcank.curalis.data.local.dao.ReminderDao 
          return (T) DatabaseModule_ProvideReminderDaoFactory.provideReminderDao(singletonCImpl.provideCuralisDatabaseProvider.get());

          case 2: // com.gokcank.curalis.data.local.CuralisDatabase 
          return (T) DatabaseModule_ProvideCuralisDatabaseFactory.provideCuralisDatabase(ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 3: // com.gokcank.curalis.data.repository.AppointmentRepositoryImpl 
          return (T) new AppointmentRepositoryImpl(singletonCImpl.provideAppointmentDaoProvider.get());

          case 4: // com.gokcank.curalis.data.local.dao.AppointmentDao 
          return (T) DatabaseModule_ProvideAppointmentDaoFactory.provideAppointmentDao(singletonCImpl.provideCuralisDatabaseProvider.get());

          case 5: // com.gokcank.curalis.data.repository.DoctorRepositoryImpl 
          return (T) new DoctorRepositoryImpl(singletonCImpl.provideDoctorDaoProvider.get());

          case 6: // com.gokcank.curalis.data.local.dao.DoctorDao 
          return (T) DatabaseModule_ProvideDoctorDaoFactory.provideDoctorDao(singletonCImpl.provideCuralisDatabaseProvider.get());

          case 7: // com.gokcank.curalis.data.repository_impl.MedicationRepositoryImpl 
          return (T) new MedicationRepositoryImpl(singletonCImpl.provideMedicationDaoProvider.get());

          case 8: // com.gokcank.curalis.data.local.dao.MedicationDao 
          return (T) DatabaseModule_ProvideMedicationDaoFactory.provideMedicationDao(singletonCImpl.provideCuralisDatabaseProvider.get());

          case 9: // com.gokcank.curalis.data.provider.ProviderManager 
          return (T) new ProviderManager(singletonCImpl.localMedicationProvider.get());

          case 10: // com.gokcank.curalis.data.provider.local.LocalMedicationProvider 
          return (T) new LocalMedicationProvider(singletonCImpl.provideDrugDaoProvider.get());

          case 11: // com.gokcank.curalis.data.local.dao.DrugDao 
          return (T) DatabaseModule_ProvideDrugDaoFactory.provideDrugDao(singletonCImpl.provideMedicationDictionaryDatabaseProvider.get());

          case 12: // com.gokcank.curalis.data.local.MedicationDictionaryDatabase 
          return (T) DatabaseModule_ProvideMedicationDictionaryDatabaseFactory.provideMedicationDictionaryDatabase(ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 13: // com.gokcank.curalis.data.repository.VitalRepositoryImpl 
          return (T) new VitalRepositoryImpl(singletonCImpl.provideVitalDaoProvider.get());

          case 14: // com.gokcank.curalis.data.local.dao.VitalDao 
          return (T) DatabaseModule_ProvideVitalDaoFactory.provideVitalDao(singletonCImpl.provideCuralisDatabaseProvider.get());

          case 15: // com.gokcank.curalis.data.backup.LocalBackupManager 
          return (T) new LocalBackupManager(singletonCImpl.provideCuralisDatabaseProvider.get(), singletonCImpl.provideGsonProvider.get());

          case 16: // com.google.gson.Gson 
          return (T) AppModule_ProvideGsonFactory.provideGson();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
