package com.gokcank.curalis.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.gokcank.curalis.data.local.entity.MedicationEntity;
import java.lang.Class;
import java.lang.Exception;
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

  private final EntityDeletionOrUpdateAdapter<MedicationEntity> __deletionAdapterOfMedicationEntity;

  private final EntityDeletionOrUpdateAdapter<MedicationEntity> __updateAdapterOfMedicationEntity;

  public MedicationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMedicationEntity = new EntityInsertionAdapter<MedicationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `medications` (`id`,`name`,`barcode`,`activeIngredient`,`form`,`dosage`,`unit`,`notes`) VALUES (?,?,?,?,?,?,?,?)";
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
        return "UPDATE OR ABORT `medications` SET `id` = ?,`name` = ?,`barcode` = ?,`activeIngredient` = ?,`form` = ?,`dosage` = ?,`unit` = ?,`notes` = ? WHERE `id` = ?";
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
        statement.bindString(9, entity.getId());
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
            _item = new MedicationEntity(_tmpId,_tmpName,_tmpBarcode,_tmpActiveIngredient,_tmpForm,_tmpDosage,_tmpUnit,_tmpNotes);
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
            _result = new MedicationEntity(_tmpId,_tmpName,_tmpBarcode,_tmpActiveIngredient,_tmpForm,_tmpDosage,_tmpUnit,_tmpNotes);
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
            _item = new MedicationEntity(_tmpId,_tmpName,_tmpBarcode,_tmpActiveIngredient,_tmpForm,_tmpDosage,_tmpUnit,_tmpNotes);
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
