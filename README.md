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
// Подключение через Supabase Kotlin SDK
val supabaseUrl = "https://your-project-url.supabase.co"
val supabaseKey = "your-anon-key"
val supabase = SupabaseClient(supabaseUrl, supabaseKey)
Пример для JavaScript:

javascript
Копировать код
// Установка библиотеки
npm install @supabase/supabase-js

// Подключение к Supabase
import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://your-project-url.supabase.co'
const supabaseKey = 'your-anon-key'
const supabase = createClient(supabaseUrl, supabaseKey)
3. Основные операции с данными
В Supabase все данные хранятся в базе данных PostgreSQL, и для работы с ними используется SQL. Также Supabase генерирует REST API для работы с таблицами.

3.1. Добавление данных
Для добавления данных можно использовать метод insert().

javascript
Копировать код
const { data, error } = await supabase
  .from('posts') // Указываем название таблицы
  .insert([
    { title: 'Hello World', content: 'My first post' }
  ])
3.2. Получение данных
Для получения данных используем метод select().

javascript
Копировать код
const { data, error } = await supabase
  .from('posts') // Указываем название таблицы
  .select('*')   // Выбираем все столбцы
3.3. Обновление данных
Для обновления данных используется метод update().

javascript
Копировать код
const { data, error } = await supabase
  .from('posts')
  .update({ content: 'Updated content' })
  .match({ id: 1 }) // Указываем запись по id, которую нужно обновить
3.4. Удаление данных
Для удаления данных используется метод delete().

javascript
Копировать код
const { data, error } = await supabase
  .from('posts')
  .delete()
  .match({ id: 1 }) // Указываем, какую запись нужно удалить
3.5. Использование фильтров
Вы можете использовать фильтры для выборки данных.

javascript
Копировать код
const { data, error } = await supabase
  .from('posts')
  .select('*')
  .eq('id', 1)  // Фильтруем по полю id
  .like('title', '%Hello%') // Используем LIKE для поиска
3.6. Реальное время (Realtime)
Supabase поддерживает работу в реальном времени, что позволяет отслеживать изменения в базе данных.

javascript
Копировать код
const subscription = supabase
  .from('posts')
  .on('INSERT', payload => {
    console.log('New post added!', payload)
  })
  .subscribe()
3.7. Аутентификация
Supabase предоставляет систему аутентификации с использованием email и пароля, социальных входов и других методов.

Пример регистрации пользователя:

javascript
Копировать код
const { user, error } = await supabase.auth.signUp({
  email: 'example@example.com',
  password: 'password123'
})
Пример входа:

javascript
Копировать код
const { user, error } = await supabase.auth.signIn({
  email: 'example@example.com',
  password: 'password123'
})
3.8. Хранение файлов (Storage)
Supabase также предоставляет возможность загружать и хранить файлы, например изображения.

Для работы с хранилищем файлов используется API Storage.

Загрузка файла:
javascript
Копировать код
const { data, error } = await supabase.storage
  .from('avatars') // Указываем бакет
  .upload('public/avatar.png', file) // Загружаем файл
Получение URL для файла:
javascript
Копировать код
const { publicURL, error } = await supabase.storage
  .from('avatars') // Указываем бакет
  .getPublicUrl('public/avatar.png') // Получаем публичный URL файла
Получение списка файлов:
javascript
Копировать код
const { data, error } = await supabase.storage
  .from('avatars') // Указываем бакет
  .list() // Получаем список файлов
Удаление файла:
javascript
Копировать код
const { data, error } = await supabase.storage
  .from('avatars') // Указываем бакет
  .remove(['public/avatar.png']) // Удаляем файл
3.9. Обработка ошибок
Supabase всегда возвращает объект с данными и ошибками, которые можно проверить.

javascript
Копировать код
const { data, error } = await supabase
  .from('posts')
  .select('*')

if (error) {
  console.error('Error:', error)
} else {
  console.log('Data:', data)
}
4. Секреты и безопасность
Чтобы обеспечить безопасность ваших данных, используйте Row Level Security (RLS) в Supabase. Это позволяет вам настроить доступ к данным на уровне строк в базе данных.

Пример включения RLS:
sql
Копировать код
-- Разрешить доступ только для авторизованных пользователей
create policy "Allow logged-in read access" on posts
  for select using (auth.uid() = user_id);
5. Заключение
Supabase — это мощная и гибкая платформа для создания бэкенда с помощью SQL, API и множества встроенных сервисов. Она позволяет разработчикам легко управлять данными, аутентификацией и файловым хранилищем, предоставляя гибкие возможности для создания современных приложений.

Этот гайд охватывает основные аспекты работы с Supabase, и вы можете продолжать исследовать и использовать более сложные возможности, такие как функции и хранимые процедуры, дополнительные методы фильтрации и обновления данных, а также поддержку реального времени для создания интерактивных приложений.

Для более подробной информации и примеров можно ознакомиться с официальной документацией Supabase.


