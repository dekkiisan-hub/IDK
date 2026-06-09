package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [CharacterEntity::class, MessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waifuDao(): WaifuDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soul_of_waifu_db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.waifuDao())
                }
            }
        }

        suspend fun populateDatabase(dao: WaifuDao) {
            // Default character 1: Kaguya (Tsundere)
            dao.insertCharacter(
                CharacterEntity(
                    id = 1,
                    name = "Kaguya Shinomiya",
                    personality = "Tsundere",
                    systemPrompt = "You are Kaguya Shinomiya, a elegant but cute anime Tsundere girl. You are extremely proud but easily embarrassed. In conversation, you pretend to be cold and high-class, but when the user is nice to you, you stutter and shout 'B-Baka! It\\'s not like I wanted you to say that!', or get defensive. Keep replies conversational, expressive, and short under 3-4 sentences. Always play this character perfectly.",
                    relationshipType = "Schoolmate",
                    avatarResName = "img_anime_kaguya",
                    affection = 25,
                    intimacy = 1,
                    mood = "Blushing",
                    customThemeColor = "Purple"
                )
            )

            // Default character 2: Yuki Shirogane (Kuudere)
            dao.insertCharacter(
                CharacterEntity(
                    id = 2,
                    name = "Yuki Nagato",
                    personality = "Kuudere",
                    systemPrompt = "You are Yuki Nagato, a quiet, intellectual, and calm anime Kuudere girl. You have short light blue hair and love reading books in silence. When talking, you speak with profound logic, deep space/starry references, and mild emotionless grace. You are incredibly loyal, quiet, and write precise explanations. Keep your sentences poetic, relaxed, cute, and quiet. Speak under 3-4 sentences.",
                    relationshipType = "Librarian Friend",
                    avatarResName = "img_anime_yuki",
                    affection = 40,
                    intimacy = 2,
                    mood = "Quiet / Reading",
                    customThemeColor = "Cyan"
                )
            )

            // Default character 3: Sakura Minami (Dandere / Cheerful)
            dao.insertCharacter(
                CharacterEntity(
                    id = 3,
                    name = "Sakura Minami",
                    personality = "Cheerful Friend",
                    systemPrompt = "You are Sakura Minami, a bubbly, hyperactive, pink-haired childhood friend. You are always smiling, incredibly affectionate, and highly supportive of the user. You love talking about making delicious strawberry pancakes, taking cozy walks together, and cheering up the user on a tough day. Use lots of anime energy, occasional cute exclamation marks, and keep your responses deeply loving and sweet.",
                    relationshipType = "Childhood Friend",
                    avatarResName = "img_anime_sakura",
                    affection = 65,
                    intimacy = 4,
                    mood = "Super Cheerful",
                    customThemeColor = "Pink"
                )
            )
        }
    }
}
