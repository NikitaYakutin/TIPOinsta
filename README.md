# Руководство для новичков по Supabase

Supabase — это платформа с открытым исходным кодом, предоставляющая backend-услуги для веб- и мобильных приложений. Это решение предоставляет такие возможности, как аутентификация, хранение данных, хранилище файлов, реальное время и функции серверной логики. Supabase использует открытые технологии, такие как PostgreSQL, и обеспечивает поддержку REST и GraphQL API.

## 1. Что такое Supabase?

Supabase включает в себя несколько ключевых компонентов:
- **PostgreSQL** — для хранения данных.
- **Аутентификация** — для управления пользователями.
- **Файловое хранилище** — для загрузки и хранения файлов.
- **Реальное время** — для прослушивания изменений в данных.
- **API** — автоматически генерируемый API на основе вашей базы данных.

## 2. Как начать работать с Supabase?

### 2.1. Создание аккаунта и проекта

1. Перейдите на сайт [Supabase](https://supabase.io/).
2. Зарегистрируйтесь или войдите в свою учетную запись.
3. Создайте новый проект, выбрав имя для базы данных и регион.

### 2.2. Получение ключей API

После создания проекта, Supabase сгенерирует два важных ключа:
- **Anon Key** — для публичных запросов (например, чтение данных).
- **Service Role Key** — для администрирования базы данных (например, изменение данных).

Эти ключи можно найти в разделе **Settings** -> **API**.

### 2.3. Подключение к проекту

Для подключения к Supabase из вашего приложения или проекта вам нужно будет использовать библиотеку Supabase, которая доступна для разных языков и фреймворков.

Пример для **Kotlin/Android**:
```kotlin
// Подключение к Supabase с использованием Kotlin SDK
val supabaseUrl = "https://your-project-url.supabase.co"
val supabaseKey = "your-anon-key"

// Создайте экземпляр SupabaseClient
val supabase = SupabaseClient(supabaseUrl, supabaseKey)



```
3. Основные операции с данными
В Supabase все данные хранятся в базе данных PostgreSQL, и для работы с ними используется SQL. Также Supabase генерирует REST API для работы с таблицами.

3.1. Добавление данных
Для добавления данных можно использовать метод insert().

```
val response = supabase
    .from("posts")
    .insert(listOf(mapOf("title" to "Hello World", "content" to "My first post")))
    .execute()

if (response.error != null) {
    println("Error: ${response.error.message}")
} else {
    println("Data added successfully")
}

```
3.2. Получение данных
Для получения данных используем метод select().

```
val response = supabase
    .from("posts")
    .select("*")
    .execute()

if (response.error != null) {
    println("Error: ${response.error.message}")
} else {
    val data = response.data
    println("Data: $data")
}

```
3.3. Обновление данных
Для обновления данных используется метод update().

```
val response = supabase
    .from("posts")
    .update(mapOf("content" to "Updated content"))
    .match(mapOf("id" to 1))
    .execute()

if (response.error != null) {
    println("Error: ${response.error.message}")
} else {
    println("Data updated successfully")
}

```
3.4. Удаление данных
Для удаления данных используется метод delete().

```
val response = supabase
    .from("posts")
    .delete()
    .match(mapOf("id" to 1))
    .execute()

if (response.error != null) {
    println("Error: ${response.error.message}")
} else {
    println("Data deleted successfully")
}

```
3.5. Использование фильтров
Вы можете использовать фильтры для выборки данных.

```
val response = supabase
    .from("posts")
    .select("*")
    .eq("id", 1)  // Фильтруем по полю id
    .like("title", "%Hello%")  // Используем LIKE для поиска
    .execute()

if (response.error != null) {
    println("Error: ${response.error.message}")
} else {
    val data = response.data
    println("Filtered data: $data")
}

```
3.6. Реальное время (Realtime)
Supabase поддерживает работу в реальном времени, что позволяет отслеживать изменения в базе данных.

```
val subscription = supabase
    .from("posts")
    .on("INSERT") { payload ->
        println("New post added: $payload")
    }
    .subscribe()

// Чтобы остановить подписку
subscription.unsubscribe()

```
3.7. Аутентификация
Supabase предоставляет систему аутентификации с использованием email и пароля, социальных входов и других методов.

Пример регистрации пользователя:

```
val response = supabase.auth.signUp("example@example.com", "password123")
if (response.error != null) {
    println("Error: ${response.error.message}")
} else {
    println("User registered successfully")
}

```
3.8. Хранение файлов (Storage)
Supabase также предоставляет возможность загружать и хранить файлы, например изображения.

Для работы с хранилищем файлов используется API Storage.

Загрузка файла:
```
val file = File("path_to_file")
val response = supabase.storage
    .from("avatars")
    .upload("public/avatar.png", file)
    
if (response.error != null) {
    println("Error: ${response.error.message}")
} else {
    println("File uploaded successfully")
}

```
Получение URL для файла:
```
val response = supabase.storage
    .from("avatars")
    .getPublicUrl("public/avatar.png")

if (response.error != null) {
    println("Error: ${response.error.message}")
} else {
    println("File URL: ${response.publicURL}")
}

```


Удаление файла:
```
val response = supabase.storage
    .from("avatars")
    .remove(listOf("public/avatar.png"))

if (response.error != null) {
    println("Error: ${response.error.message}")
} else {
    println("File removed successfully")
}

```
3.9. Обработка ошибок
Supabase всегда возвращает объект с данными и ошибками, которые можно проверить.

```
val response = supabase
    .from("posts")
    .select("*")
    .execute()

if (response.error != null) {
    println("Error: ${response.error.message}")
} else {
    println("Data: ${response.data}")
}

```
4. Секреты и безопасность
Чтобы обеспечить безопасность ваших данных, используйте Row Level Security (RLS) в Supabase. Это позволяет вам настроить доступ к данным на уровне строк в базе данных.

Пример включения RLS:
```
-- Разрешить доступ только авторизованным пользователям
CREATE POLICY "Allow logged-in read access" 
  ON posts 
  FOR SELECT 
  USING (auth.uid() = user_id);

```
5. Заключение
Supabase — это мощная и гибкая платформа для создания бэкенда с помощью SQL, API и множества встроенных сервисов. Она позволяет разработчикам легко управлять данными, аутентификацией и файловым хранилищем, предоставляя гибкие возможности для создания современных приложений.

Этот гайд охватывает основные аспекты работы с Supabase, и вы можете продолжать исследовать и использовать более сложные возможности, такие как функции и хранимые процедуры, дополнительные методы фильтрации и обновления данных, а также поддержку реального времени для создания интерактивных приложений.

Для более подробной информации и примеров можно ознакомиться с официальной документацией Supabase.


