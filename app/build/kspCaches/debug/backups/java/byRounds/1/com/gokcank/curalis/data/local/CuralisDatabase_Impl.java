package com.gokcank.curalis.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.gokcank.curalis.data.local.dao.AppointmentDao;
import com.gokcank.curalis.data.local.dao.AppointmentDao_Impl;
import com.gokcank.curalis.data.local.dao.BackupDao;
import com.gokcank.curalis.data.local.dao.BackupDao_Impl;
import com.gokcank.curalis.data.local.dao.DoctorDao;
import com.gokcank.curalis.data.local.dao.DoctorDao_Impl;
import com.gokcank.curalis.data.local.dao.MedicationDao;
import com.gokcank.curalis.data.local.dao.MedicationDao_Impl;
import com.gokcank.curalis.data.local.dao.ReminderDao;
import com.gokcank.curalis.data.local.dao.ReminderDao_Impl;
import com.gokcank.curalis.data.local.dao.VitalDao;
import com.gokcank.curalis.data.local.dao.VitalDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CuralisDatabase_Impl extends CuralisDatabase {
  private volatile MedicationDao _medicationDao;

  private volatile ReminderDao _reminderDao;

  private volatile DoctorDao _doctorDao;

  private volatile AppointmentDao _appointmentDao;

  private volatile VitalDao _vitalDao;

  private volatile BackupDao _backupDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `medications` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `barcode` TEXT, `activeIngredient` TEXT, `form` TEXT, `dosage` TEXT, `unit` TEXT, `notes` TEXT, `frequencyType` TEXT NOT NULL, `intervalDays` INTEGER, `startDate` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`id` TEXT NOT NULL, `medicationId` TEXT NOT NULL, `timeInMillis` INTEGER NOT NULL, `state` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`medicationId`) REFERENCES `medications`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_medicationId` ON `reminders` (`medicationId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `medication_days` (`medicationId` TEXT NOT NULL, `dayOfWeek` INTEGER NOT NULL, PRIMARY KEY(`medicationId`, `dayOfWeek`), FOREIGN KEY(`medicationId`) REFERENCES `medications`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_medication_days_medicationId` ON `medication_days` (`medicationId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `medication_times` (`id` TEXT NOT NULL, `medicationId` TEXT NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `dose` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`medicationId`) REFERENCES `medications`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_medication_times_medicationId` ON `medication_times` (`medicationId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `doctors` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `specialty` TEXT, `phoneNumber` TEXT, `email` TEXT, `hospitalName` TEXT, `notes` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `appointments` (`id` TEXT NOT NULL, `doctorId` TEXT, `title` TEXT NOT NULL, `timeInMillis` INTEGER NOT NULL, `location` TEXT, `notes` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`doctorId`) REFERENCES `doctors`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_appointments_doctorId` ON `appointments` (`doctorId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `vitals` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `value1` REAL NOT NULL, `value2` REAL, `unit` TEXT NOT NULL, `timeInMillis` INTEGER NOT NULL, `notes` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ded1adcb1d8a24afe41127593eea4d2c')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `medications`");
        db.execSQL("DROP TABLE IF EXISTS `reminders`");
        db.execSQL("DROP TABLE IF EXISTS `medication_days`");
        db.execSQL("DROP TABLE IF EXISTS `medication_times`");
        db.execSQL("DROP TABLE IF EXISTS `doctors`");
        db.execSQL("DROP TABLE IF EXISTS `appointments`");
        db.execSQL("DROP TABLE IF EXISTS `vitals`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMedications = new HashMap<String, TableInfo.Column>(11);
        _columnsMedications.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("barcode", new TableInfo.Column("barcode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("activeIngredient", new TableInfo.Column("activeIngredient", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("form", new TableInfo.Column("form", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("dosage", new TableInfo.Column("dosage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("unit", new TableInfo.Column("unit", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("frequencyType", new TableInfo.Column("frequencyType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("intervalDays", new TableInfo.Column("intervalDays", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("startDate", new TableInfo.Column("startDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMedications = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMedications = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMedications = new TableInfo("medications", _columnsMedications, _foreignKeysMedications, _indicesMedications);
        final TableInfo _existingMedications = TableInfo.read(db, "medications");
        if (!_infoMedications.equals(_existingMedications)) {
          return new RoomOpenHelper.ValidationResult(false, "medications(com.gokcank.curalis.data.local.entity.MedicationEntity).\n"
                  + " Expected:\n" + _infoMedications + "\n"
                  + " Found:\n" + _existingMedications);
        }
        final HashMap<String, TableInfo.Column> _columnsReminders = new HashMap<String, TableInfo.Column>(4);
        _columnsReminders.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("medicationId", new TableInfo.Column("medicationId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("timeInMillis", new TableInfo.Column("timeInMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("state", new TableInfo.Column("state", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReminders = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysReminders.add(new TableInfo.ForeignKey("medications", "CASCADE", "NO ACTION", Arrays.asList("medicationId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesReminders = new HashSet<TableInfo.Index>(1);
        _indicesReminders.add(new TableInfo.Index("index_reminders_medicationId", false, Arrays.asList("medicationId"), Arrays.asList("ASC")));
        final TableInfo _infoReminders = new TableInfo("reminders", _columnsReminders, _foreignKeysReminders, _indicesReminders);
        final TableInfo _existingReminders = TableInfo.read(db, "reminders");
        if (!_infoReminders.equals(_existingReminders)) {
          return new RoomOpenHelper.ValidationResult(false, "reminders(com.gokcank.curalis.data.local.entity.ReminderEntity).\n"
                  + " Expected:\n" + _infoReminders + "\n"
                  + " Found:\n" + _existingReminders);
        }
        final HashMap<String, TableInfo.Column> _columnsMedicationDays = new HashMap<String, TableInfo.Column>(2);
        _columnsMedicationDays.put("medicationId", new TableInfo.Column("medicationId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicationDays.put("dayOfWeek", new TableInfo.Column("dayOfWeek", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMedicationDays = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMedicationDays.add(new TableInfo.ForeignKey("medications", "CASCADE", "NO ACTION", Arrays.asList("medicationId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMedicationDays = new HashSet<TableInfo.Index>(1);
        _indicesMedicationDays.add(new TableInfo.Index("index_medication_days_medicationId", false, Arrays.asList("medicationId"), Arrays.asList("ASC")));
        final TableInfo _infoMedicationDays = new TableInfo("medication_days", _columnsMedicationDays, _foreignKeysMedicationDays, _indicesMedicationDays);
        final TableInfo _existingMedicationDays = TableInfo.read(db, "medication_days");
        if (!_infoMedicationDays.equals(_existingMedicationDays)) {
          return new RoomOpenHelper.ValidationResult(false, "medication_days(com.gokcank.curalis.data.local.entity.MedicationDaysEntity).\n"
                  + " Expected:\n" + _infoMedicationDays + "\n"
                  + " Found:\n" + _existingMedicationDays);
        }
        final HashMap<String, TableInfo.Column> _columnsMedicationTimes = new HashMap<String, TableInfo.Column>(5);
        _columnsMedicationTimes.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicationTimes.put("medicationId", new TableInfo.Column("medicationId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicationTimes.put("hour", new TableInfo.Column("hour", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicationTimes.put("minute", new TableInfo.Column("minute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicationTimes.put("dose", new TableInfo.Column("dose", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMedicationTimes = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMedicationTimes.add(new TableInfo.ForeignKey("medications", "CASCADE", "NO ACTION", Arrays.asList("medicationId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMedicationTimes = new HashSet<TableInfo.Index>(1);
        _indicesMedicationTimes.add(new TableInfo.Index("index_medication_times_medicationId", false, Arrays.asList("medicationId"), Arrays.asList("ASC")));
        final TableInfo _infoMedicationTimes = new TableInfo("medication_times", _columnsMedicationTimes, _foreignKeysMedicationTimes, _indicesMedicationTimes);
        final TableInfo _existingMedicationTimes = TableInfo.read(db, "medication_times");
        if (!_infoMedicationTimes.equals(_existingMedicationTimes)) {
          return new RoomOpenHelper.ValidationResult(false, "medication_times(com.gokcank.curalis.data.local.entity.MedicationTimeEntity).\n"
                  + " Expected:\n" + _infoMedicationTimes + "\n"
                  + " Found:\n" + _existingMedicationTimes);
        }
        final HashMap<String, TableInfo.Column> _columnsDoctors = new HashMap<String, TableInfo.Column>(7);
        _columnsDoctors.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDoctors.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDoctors.put("specialty", new TableInfo.Column("specialty", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDoctors.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDoctors.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDoctors.put("hospitalName", new TableInfo.Column("hospitalName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDoctors.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDoctors = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDoctors = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDoctors = new TableInfo("doctors", _columnsDoctors, _foreignKeysDoctors, _indicesDoctors);
        final TableInfo _existingDoctors = TableInfo.read(db, "doctors");
        if (!_infoDoctors.equals(_existingDoctors)) {
          return new RoomOpenHelper.ValidationResult(false, "doctors(com.gokcank.curalis.data.local.entity.DoctorEntity).\n"
                  + " Expected:\n" + _infoDoctors + "\n"
                  + " Found:\n" + _existingDoctors);
        }
        final HashMap<String, TableInfo.Column> _columnsAppointments = new HashMap<String, TableInfo.Column>(6);
        _columnsAppointments.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppointments.put("doctorId", new TableInfo.Column("doctorId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppointments.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppointments.put("timeInMillis", new TableInfo.Column("timeInMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppointments.put("location", new TableInfo.Column("location", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppointments.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAppointments = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysAppointments.add(new TableInfo.ForeignKey("doctors", "SET NULL", "NO ACTION", Arrays.asList("doctorId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesAppointments = new HashSet<TableInfo.Index>(1);
        _indicesAppointments.add(new TableInfo.Index("index_appointments_doctorId", false, Arrays.asList("doctorId"), Arrays.asList("ASC")));
        final TableInfo _infoAppointments = new TableInfo("appointments", _columnsAppointments, _foreignKeysAppointments, _indicesAppointments);
        final TableInfo _existingAppointments = TableInfo.read(db, "appointments");
        if (!_infoAppointments.equals(_existingAppointments)) {
          return new RoomOpenHelper.ValidationResult(false, "appointments(com.gokcank.curalis.data.local.entity.AppointmentEntity).\n"
                  + " Expected:\n" + _infoAppointments + "\n"
                  + " Found:\n" + _existingAppointments);
        }
        final HashMap<String, TableInfo.Column> _columnsVitals = new HashMap<String, TableInfo.Column>(7);
        _columnsVitals.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVitals.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVitals.put("value1", new TableInfo.Column("value1", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVitals.put("value2", new TableInfo.Column("value2", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVitals.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVitals.put("timeInMillis", new TableInfo.Column("timeInMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVitals.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVitals = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesVitals = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoVitals = new TableInfo("vitals", _columnsVitals, _foreignKeysVitals, _indicesVitals);
        final TableInfo _existingVitals = TableInfo.read(db, "vitals");
        if (!_infoVitals.equals(_existingVitals)) {
          return new RoomOpenHelper.ValidationResult(false, "vitals(com.gokcank.curalis.data.local.entity.VitalEntity).\n"
                  + " Expected:\n" + _infoVitals + "\n"
                  + " Found:\n" + _existingVitals);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "ded1adcb1d8a24afe41127593eea4d2c", "42d1a1980f240ea01ea9159be2ab8739");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "medications","reminders","medication_days","medication_times","doctors","appointments","vitals");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `medications`");
      _db.execSQL("DELETE FROM `reminders`");
      _db.execSQL("DELETE FROM `medication_days`");
      _db.execSQL("DELETE FROM `medication_times`");
      _db.execSQL("DELETE FROM `doctors`");
      _db.execSQL("DELETE FROM `appointments`");
      _db.execSQL("DELETE FROM `vitals`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MedicationDao.class, MedicationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReminderDao.class, ReminderDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DoctorDao.class, DoctorDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AppointmentDao.class, AppointmentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(VitalDao.class, VitalDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BackupDao.class, BackupDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MedicationDao getMedicationDao() {
    if (_medicationDao != null) {
      return _medicationDao;
    } else {
      synchronized(this) {
        if(_medicationDao == null) {
          _medicationDao = new MedicationDao_Impl(this);
        }
        return _medicationDao;
      }
    }
  }

  @Override
  public ReminderDao getReminderDao() {
    if (_reminderDao != null) {
      return _reminderDao;
    } else {
      synchronized(this) {
        if(_reminderDao == null) {
          _reminderDao = new ReminderDao_Impl(this);
        }
        return _reminderDao;
      }
    }
  }

  @Override
  public DoctorDao getDoctorDao() {
    if (_doctorDao != null) {
      return _doctorDao;
    } else {
      synchronized(this) {
        if(_doctorDao == null) {
          _doctorDao = new DoctorDao_Impl(this);
        }
        return _doctorDao;
      }
    }
  }

  @Override
  public AppointmentDao getAppointmentDao() {
    if (_appointmentDao != null) {
      return _appointmentDao;
    } else {
      synchronized(this) {
        if(_appointmentDao == null) {
          _appointmentDao = new AppointmentDao_Impl(this);
        }
        return _appointmentDao;
      }
    }
  }

  @Override
  public VitalDao getVitalDao() {
    if (_vitalDao != null) {
      return _vitalDao;
    } else {
      synchronized(this) {
        if(_vitalDao == null) {
          _vitalDao = new VitalDao_Impl(this);
        }
        return _vitalDao;
      }
    }
  }

  @Override
  public BackupDao getBackupDao() {
    if (_backupDao != null) {
      return _backupDao;
    } else {
      synchronized(this) {
        if(_backupDao == null) {
          _backupDao = new BackupDao_Impl(this);
        }
        return _backupDao;
      }
    }
  }
}
