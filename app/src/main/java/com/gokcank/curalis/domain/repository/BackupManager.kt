package com.gokcank.curalis.domain.repository

/**
 * Interface defining the backup and restore operations for the application.
 */
interface BackupManager {
    /**
     * Exports the entire database state into a JSON string.
     * @return JSON string representing BackupData
     */
    suspend fun exportData(): String

    /**
     * Imports a JSON string representing BackupData and overwrites the local database.
     * @param jsonString The JSON string to parse and import
     * @return true if successful, false otherwise
     */
    suspend fun importData(jsonString: String): Boolean
}
