package com.gokcank.curalis.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.gokcank.curalis.data.local.entity.MedicationDaysEntity;
import com.gokcank.curalis.data.local.entity.MedicationEntity;
import com.gokcank.curalis.data.local.entity.MedicationTimeEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MedicationDao_Impl implements MedicationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MedicationEntity> __insertionAdapterOfMedicationEntity;

  private final EntityInsertionAdapter<MedicationDaysEntity> __insertionAdapterOfMedicationDaysEntity;

  private final EntityInsertionAdapter<MedicationTimeEntity> __insertionAdapterOfMedicationTimeEntity;

  private final EntityDeletionOrUpdateAdapter<MedicationEntity> __deletionAdapterOfMedicationEntity;

  private final EntityDeletionOrUpdateAdapter<MedicationEntity> __updateAdapterOfMedicationEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMedicationDays;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMedicationTimes;

  public MedicationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMedicationEntity = new EntityInsertionAdapter<MedicationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `medications` (`id`,`name`,`barcode`,`activeIngredient`,`form`,`dosage`,`unit`,`notes`,`frequencyType`,`intervalDays`,`startDate`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getBarcode() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getBarcode());
        }
        if (entity.getActiveIngredient() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getActiveIngredient());
        }
        if (entity.getForm() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getForm());
        }
        if (entity.getDosage() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDosage());
        }
        if (entity.getUnit() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getUnit());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getNotes());
        }
        statement.bindString(9, entity.getFrequencyType());
        if (entity.getIntervalDays() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getIntervalDays());
        }
        statement.bindLong(11, entity.getStartDate());
      }
    };
    this.__insertionAdapterOfMedicationDaysEntity = new EntityInsertionAdapter<MedicationDaysEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `medication_days` (`medicationId`,`dayOfWeek`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationDaysEntity entity) {
        statement.bindString(1, entity.getMedicationId());
        statement.bindLong(2, entity.getDayOfWeek());
      }
    };
    this.__insertionAdapterOfMedicationTimeEntity = new EntityInsertionAdapter<MedicationTimeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `medication_times` (`id`,`medicationId`,`hour`,`minute`,`dose`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationTimeEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getMedicationId());
        statement.bindLong(3, entity.getHour());
        statement.bindLong(4, entity.getMinute());
        if (entity.getDose() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getDose());
        }
      }
    };
    this.__deletionAdapterOfMedicationEntity = new EntityDeletionOrUpdateAdapter<MedicationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `medications` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfMedicationEntity = new EntityDeletionOrUpdateAdapter<MedicationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `medications` SET `id` = ?,`name` = ?,`barcode` = ?,`activeIngredient` = ?,`form` = ?,`dosage` = ?,`unit` = ?,`notes` = ?,`frequencyType` = ?,`intervalDays` = ?,`startDate` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MedicationEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getBarcode() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getBarcode());
        }
        if (entity.getActiveIngredient() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getActiveIngredient());
        }
        if (entity.getForm() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getForm());
        }
        if (entity.getDosage() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDosage());
        }
        if (entity.getUnit() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getUnit());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getNotes());
        }
        statement.bindString(9, entity.getFrequencyType());
        if (entity.getIntervalDays() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getIntervalDays());
        }
        statement.bindLong(11, entity.getStartDate());
        statement.bindString(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteMedicationDays = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM medication_days WHERE medicationId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMedicationTimes = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM medication_times WHERE medicationId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMedication(final MedicationEntity medication,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMedicationEntity.insert(medication);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMedicationDays(final List<MedicationDaysEntity> days,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMedicationDaysEntity.insert(days);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMedicationTimes(final List<MedicationTimeEntity> times,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMedicationTimeEntity.insert(times);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMedication(final MedicationEntity medication,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMedicationEntity.handle(medication);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMedication(final MedicationEntity medication,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMedicationEntity.handle(medication);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMedicationDays(final String medicationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMedicationDays.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, medicationId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteMedicationDays.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMedicationTimes(final String medicationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMedicationTimes.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, medicationId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteMedicationTimes.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MedicationEntity>> getAllMedications() {
    final String _sql = "SELECT * FROM medications ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"medications"}, new Callable<List<MedicationEntity>>() {
      @Override
      @NonNull
      public List<MedicationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfActiveIngredient = CursorUtil.getColumnIndexOrThrow(_cursor, "activeIngredient");
          final int _cursorIndexOfForm = CursorUtil.getColumnIndexOrThrow(_cursor, "form");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfFrequencyType = CursorUtil.getColumnIndexOrThrow(_cursor, "frequencyType");
          final int _cursorIndexOfIntervalDays = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalDays");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final List<MedicationEntity> _result = new ArrayList<MedicationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpBarcode;
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _tmpBarcode = null;
            } else {
              _tmpBarcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final String _tmpActiveIngredient;
            if (_cursor.isNull(_cursorIndexOfActiveIngredient)) {
              _tmpActiveIngredient = null;
            } else {
              _tmpActiveIngredient = _cursor.getString(_cursorIndexOfActiveIngredient);
            }
            final String _tmpForm;
            if (_cursor.isNull(_cursorIndexOfForm)) {
              _tmpForm = null;
            } else {
              _tmpForm = _cursor.getString(_cursorIndexOfForm);
            }
            final String _tmpDosage;
            if (_cursor.isNull(_cursorIndexOfDosage)) {
              _tmpDosage = null;
            } else {
              _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            }
            final String _tmpUnit;
            if (_cursor.isNull(_cursorIndexOfUnit)) {
              _tmpUnit = null;
            } else {
              _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpFrequencyType;
            _tmpFrequencyType = _cursor.getString(_cursorIndexOfFrequencyType);
            final Integer _tmpIntervalDays;
            if (_cursor.isNull(_cursorIndexOfIntervalDays)) {
              _tmpIntervalDays = null;
            } else {
              _tmpIntervalDays = _cursor.getInt(_cursorIndexOfIntervalDays);
            }
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            _item = new MedicationEntity(_tmpId,_tmpName,_tmpBarcode,_tmpActiveIngredient,_tmpForm,_tmpDosage,_tmpUnit,_tmpNotes,_tmpFrequencyType,_tmpIntervalDays,_tmpStartDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<MedicationEntity> getMedicationById(final String id) {
    final String _sql = "SELECT * FROM medications WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"medications"}, new Callable<MedicationEntity>() {
      @Override
      @Nullable
      public MedicationEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfActiveIngredient = CursorUtil.getColumnIndexOrThrow(_cursor, "activeIngredient");
          final int _cursorIndexOfForm = CursorUtil.getColumnIndexOrThrow(_cursor, "form");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfFrequencyType = CursorUtil.getColumnIndexOrThrow(_cursor, "frequencyType");
          final int _cursorIndexOfIntervalDays = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalDays");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final MedicationEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpBarcode;
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _tmpBarcode = null;
            } else {
              _tmpBarcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final String _tmpActiveIngredient;
            if (_cursor.isNull(_cursorIndexOfActiveIngredient)) {
              _tmpActiveIngredient = null;
            } else {
              _tmpActiveIngredient = _cursor.getString(_cursorIndexOfActiveIngredient);
            }
            final String _tmpForm;
            if (_cursor.isNull(_cursorIndexOfForm)) {
              _tmpForm = null;
            } else {
              _tmpForm = _cursor.getString(_cursorIndexOfForm);
            }
            final String _tmpDosage;
            if (_cursor.isNull(_cursorIndexOfDosage)) {
              _tmpDosage = null;
            } else {
              _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            }
            final String _tmpUnit;
            if (_cursor.isNull(_cursorIndexOfUnit)) {
              _tmpUnit = null;
            } else {
              _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpFrequencyType;
            _tmpFrequencyType = _cursor.getString(_cursorIndexOfFrequencyType);
            final Integer _tmpIntervalDays;
            if (_cursor.isNull(_cursorIndexOfIntervalDays)) {
              _tmpIntervalDays = null;
            } else {
              _tmpIntervalDays = _cursor.getInt(_cursorIndexOfIntervalDays);
            }
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            _result = new MedicationEntity(_tmpId,_tmpName,_tmpBarcode,_tmpActiveIngredient,_tmpForm,_tmpDosage,_tmpUnit,_tmpNotes,_tmpFrequencyType,_tmpIntervalDays,_tmpStartDate);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MedicationEntity>> searchMedications(final String query) {
    final String _sql = "SELECT * FROM medications WHERE name LIKE '%' || ? || '%' OR barcode LIKE '%' || ? || '%' OR activeIngredient LIKE '%' || ? || '%' ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"medications"}, new Callable<List<MedicationEntity>>() {
      @Override
      @NonNull
      public List<MedicationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBarcode = CursorUtil.getColumnIndexOrThrow(_cursor, "barcode");
          final int _cursorIndexOfActiveIngredient = CursorUtil.getColumnIndexOrThrow(_cursor, "activeIngredient");
          final int _cursorIndexOfForm = CursorUtil.getColumnIndexOrThrow(_cursor, "form");
          final int _cursorIndexOfDosage = CursorUtil.getColumnIndexOrThrow(_cursor, "dosage");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfFrequencyType = CursorUtil.getColumnIndexOrThrow(_cursor, "frequencyType");
          final int _cursorIndexOfIntervalDays = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalDays");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final List<MedicationEntity> _result = new ArrayList<MedicationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpBarcode;
            if (_cursor.isNull(_cursorIndexOfBarcode)) {
              _tmpBarcode = null;
            } else {
              _tmpBarcode = _cursor.getString(_cursorIndexOfBarcode);
            }
            final String _tmpActiveIngredient;
            if (_cursor.isNull(_cursorIndexOfActiveIngredient)) {
              _tmpActiveIngredient = null;
            } else {
              _tmpActiveIngredient = _cursor.getString(_cursorIndexOfActiveIngredient);
            }
            final String _tmpForm;
            if (_cursor.isNull(_cursorIndexOfForm)) {
              _tmpForm = null;
            } else {
              _tmpForm = _cursor.getString(_cursorIndexOfForm);
            }
            final String _tmpDosage;
            if (_cursor.isNull(_cursorIndexOfDosage)) {
              _tmpDosage = null;
            } else {
              _tmpDosage = _cursor.getString(_cursorIndexOfDosage);
            }
            final String _tmpUnit;
            if (_cursor.isNull(_cursorIndexOfUnit)) {
              _tmpUnit = null;
            } else {
              _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpFrequencyType;
            _tmpFrequencyType = _cursor.getString(_cursorIndexOfFrequencyType);
            final Integer _tmpIntervalDays;
            if (_cursor.isNull(_cursorIndexOfIntervalDays)) {
              _tmpIntervalDays = null;
            } else {
              _tmpIntervalDays = _cursor.getInt(_cursorIndexOfIntervalDays);
            }
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            _item = new MedicationEntity(_tmpId,_tmpName,_tmpBarcode,_tmpActiveIngredient,_tmpForm,_tmpDosage,_tmpUnit,_tmpNotes,_tmpFrequencyType,_tmpIntervalDays,_tmpStartDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MedicationDaysEntity>> getMedicationDays(final String medicationId) {
    final String _sql = "SELECT * FROM medication_days WHERE medicationId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, medicationId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"medication_days"}, new Callable<List<MedicationDaysEntity>>() {
      @Override
      @NonNull
      public List<MedicationDaysEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMedicationId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationId");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final List<MedicationDaysEntity> _result = new ArrayList<MedicationDaysEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationDaysEntity _item;
            final String _tmpMedicationId;
            _tmpMedicationId = _cursor.getString(_cursorIndexOfMedicationId);
            final int _tmpDayOfWeek;
            _tmpDayOfWeek = _cursor.getInt(_cursorIndexOfDayOfWeek);
            _item = new MedicationDaysEntity(_tmpMedicationId,_tmpDayOfWeek);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MedicationTimeEntity>> getMedicationTimes(final String medicationId) {
    final String _sql = "SELECT * FROM medication_times WHERE medicationId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, medicationId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"medication_times"}, new Callable<List<MedicationTimeEntity>>() {
      @Override
      @NonNull
      public List<MedicationTimeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicationId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicationId");
          final int _cursorIndexOfHour = CursorUtil.getColumnIndexOrThrow(_cursor, "hour");
          final int _cursorIndexOfMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "minute");
          final int _cursorIndexOfDose = CursorUtil.getColumnIndexOrThrow(_cursor, "dose");
          final List<MedicationTimeEntity> _result = new ArrayList<MedicationTimeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MedicationTimeEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpMedicationId;
            _tmpMedicationId = _cursor.getString(_cursorIndexOfMedicationId);
            final int _tmpHour;
            _tmpHour = _cursor.getInt(_cursorIndexOfHour);
            final int _tmpMinute;
            _tmpMinute = _cursor.getInt(_cursorIndexOfMinute);
            final String _tmpDose;
            if (_cursor.isNull(_cursorIndexOfDose)) {
              _tmpDose = null;
            } else {
              _tmpDose = _cursor.getString(_cursorIndexOfDose);
            }
            _item = new MedicationTimeEntity(_tmpId,_tmpMedicationId,_tmpHour,_tmpMinute,_tmpDose);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
