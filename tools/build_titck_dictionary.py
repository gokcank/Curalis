#!/usr/bin/env python3
"""
TİTCK "Ruhsatlı Beşeri Tıbbi Ürünler Listesi" Excel dosyasından uygulamanın ilaç
sözlüğünü (app/src/main/assets/database/medications.db) üretir.

Liste: https://www.titck.gov.tr/dinamikmodul/85 (haftalık yayımlanır).

Kullanım:
    pip install openpyxl
    python3 tools/build_titck_dictionary.py <liste.xlsx> <sözlük-sürümü> [çıktı.db]

Sözlük sürümü, MedicationDictionaryDatabase.VERSION ile aynı olmalı ve her liste
güncellemesinde bir artırılmalıdır; Room ancak sürüm değişince kurulu uygulamalardaki
eski kopyayı yenisiyle değiştirir. Aynı güncellemede MedicationDictionaryDatabase.DATABASE_DATE
de listenin tarihine çekilir. MedicationDictionaryAssetTest iki sürümün eşleştiğini denetler.

Eleme kuralları:
  - Ruhsatı askıda olan ürünler (askı durumu 0 değilse),
  - barkodu 12-14 haneli bir sayı olmayan ya da adı boş olan satırlar,
  - barkodu başka bir satırla çakışan ürünlerin tümü (kaynak veri belirsiz).
Hücrelerdeki satır sonları ve bölünmez boşluklar normal boşluğa çevrilir.
"""
import collections
import os
import re
import sqlite3
import sys

import openpyxl

# Excel'in ilk sayfasındaki sütunlar (ilk iki satır başlık).
COL_BARCODE, COL_NAME, COL_INGREDIENT, COL_OWNER, COL_SUSPENSION = 1, 2, 3, 5, 11
DEFAULT_OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "database", "medications.db")


def clean(value):
    if value is None:
        return ""
    return str(value).replace("\r", " ").replace("\n", " ").replace("\xa0", " ").strip()


def read_products(path):
    sheet = openpyxl.load_workbook(path, read_only=True).worksheets[0]
    stats = collections.Counter()
    candidates = []
    for row in sheet.iter_rows(min_row=3, values_only=True):
        if row is None or all(v in (None, "") for v in row[:4]):
            continue
        stats["rows"] += 1
        barcode = row[COL_BARCODE]
        if isinstance(barcode, float) and barcode.is_integer():
            barcode = int(barcode)
        barcode = clean(barcode)
        name = clean(row[COL_NAME])
        if clean(row[COL_SUSPENSION]) not in ("0", ""):
            stats["suspended"] += 1
            continue
        if not re.fullmatch(r"\d{12,14}", barcode) or not name:
            stats["bad_barcode_or_name"] += 1
            continue
        candidates.append((barcode, name, clean(row[COL_INGREDIENT]), clean(row[COL_OWNER])))

    counts = collections.Counter(c[0] for c in candidates)
    products = [c for c in candidates if counts[c[0]] == 1]
    stats["shared_barcode"] = len(candidates) - len(products)
    stats["final"] = len(products)
    return products, stats


def write_database(products, out, version):
    if os.path.exists(out):
        os.remove(out)
    db = sqlite3.connect(out)
    # Şema DrugEntity ile birebir aynı olmalı (Room açılışta doğrular).
    db.execute(
        "CREATE TABLE `drug_entity` (`gtin` TEXT NOT NULL, `drugName` TEXT NOT NULL, "
        "`activeIngredient` TEXT NOT NULL, `manufacturer` TEXT NOT NULL, PRIMARY KEY(`gtin`))"
    )
    db.executemany("INSERT INTO drug_entity VALUES (?, ?, ?, ?)", products)
    db.commit()
    db.execute(f"PRAGMA user_version = {version}")
    db.execute("PRAGMA journal_mode = WAL")
    db.execute("VACUUM")
    db.close()


def main():
    if len(sys.argv) not in (3, 4):
        sys.exit(__doc__)
    source, version = sys.argv[1], int(sys.argv[2])
    out = os.path.normpath(sys.argv[3] if len(sys.argv) == 4 else DEFAULT_OUT)
    products, stats = read_products(source)
    write_database(products, out, version)
    print(f"{out}: {stats['final']} ürün (sözlük sürümü {version})")
    print(dict(stats))


if __name__ == "__main__":
    main()
