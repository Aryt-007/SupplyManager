package com.example.supplymanager.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supplymanager.Models.Notes;
import com.example.supplymanager.NotesClickListener;
import com.example.supplymanager.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotesListAdapterMain extends RecyclerView.Adapter <NotesViewHolderMain>{

    Context context;
    private boolean isSelectionMode = false;
    public List <Notes> list;
    NotesClickListener listener;
    private List<Notes> selectedNotes = new ArrayList<>();

    public NotesListAdapterMain(List<Notes> list, NotesClickListener listener, Context context) {
        this.list = list;
        this.listener = listener;
        this.context = context;
    }

    /**
     * Этот метод динамически рисует мини-таблицу (превью списка покупок или товаров)
     * прямо внутри карточки заметки на главном экране.
     * Так как мы не знаем заранее, сколько будет строк, мы создаем элементы интерфейса
     * (TextView и LinearLayout) прямо здесь, программно (через код, а не через XML).
     */
    private void displayTableData(LinearLayout container, Notes note) {
        // Очищаем контейнер перед добавлением новых строк.
        // Это КРАЙНЕ ВАЖНО: так как RecyclerView переиспользует старые карточки при скролле,
        // если не очистить контейнер, новые строки добавятся под старыми, и список будет бесконечно расти.
        container.removeAllViews();

        // Получаем строки с данными из базы.
        // В базе они хранятся в виде одной длинной строки, разделенной символом "|" (например: "Молоко|Хлеб|Сыр")
        String textTitles = note.getText_title();
        String countTitles = note.getCount_title();

        // Если есть данные таблицы
        if (textTitles != null && !textTitles.isEmpty() && countTitles != null && !countTitles.isEmpty()) {
            // Разбиваем длинную строку на массив отдельных слов, используя разделитель "|"
            String[] textTitleArray = textTitles.split("\\|");
            String[] countTitleArray = countTitles.split("\\|");

            // Находим минимальную длину массива, чтобы избежать ошибки IndexOutOfBoundsException,
            // если вдруг названий оказалось больше, чем цифр количества (или наоборот)
            int maxLength = Math.min(textTitleArray.length, countTitleArray.length);

            // Ограничиваем количество строк для превью на карточке.
            // Мы хотим показать максимум 3 первые позиции, чтобы карточка не была слишком огромной.
            int maxDisplayRows = Math.min(maxLength, 3);

            // --- СОЗДАЕМ ШАПКУ ТАБЛИЦЫ ("Название" | "Кол-во") ---
            LinearLayout headerRow = new LinearLayout(context);
            headerRow.setOrientation(LinearLayout.HORIZONTAL); // Выстраиваем элементы по горизонтали
            LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, // Ширина во всю карточку
                    LinearLayout.LayoutParams.WRAP_CONTENT // Высота по содержимому
            );
            headerRow.setLayoutParams(headerParams);

            // Создаем ячейку "Название"
            TextView headerName = new TextView(context);
            headerName.setText("Название");
            headerName.setTextColor(Color.BLACK);
            headerName.setTextSize(14);
            headerName.setTypeface(null, android.graphics.Typeface.BOLD); // Делаем текст жирным
            headerName.setGravity(Gravity.START); // Выравниваем по левому краю
            headerName.setPadding(8, 4, 8, 4);
            // Задаем "вес" (weight) = 1f. Это значит, что эта колонка займет всю доступную ширину,
            // оставив второй колонке ровно столько, сколько ей нужно (0.5f).
            headerName.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            ));

            // Создаем ячейку "Кол-во"
            TextView headerCount = new TextView(context);
            headerCount.setText("Кол-во");
            headerCount.setTextColor(Color.BLACK);
            headerCount.setTextSize(14);
            headerCount.setTypeface(null, android.graphics.Typeface.BOLD);
            headerCount.setGravity(Gravity.CENTER); // Выравниваем по центру
            headerCount.setPadding(8, 4, 8, 4);
            // Колонка с количеством будет в два раза уже основной (вес 0.5f против 1f)
            headerCount.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.5f
            ));

            // Добавляем созданные текстовые поля в строку, а саму строку — в главный контейнер карточки
            headerRow.addView(headerName);
            headerRow.addView(headerCount);
            container.addView(headerRow);

            // --- ДОБАВЛЯЕМ СЕРУЮ ЛИНИЮ-РАЗДЕЛИТЕЛЬ ---
            View divider = new View(context);
            divider.setBackgroundColor(Color.LTGRAY);
            // Линия будет высотой ровно 1 пиксель (или 1 dp в зависимости от экрана)
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
            );
            divider.setLayoutParams(dividerParams);
            container.addView(divider);

            // --- ДОБАВЛЯЕМ СТРОКИ С ДАННЫМИ ---
            for (int i = 0; i < maxDisplayRows; i++) {
                // Создаем новую горизонтальную строку для каждой позиции
                LinearLayout dataRow = new LinearLayout(context);
                dataRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                dataRow.setLayoutParams(rowParams);

                // Текстовое поле для названия товара (берем из массива textTitleArray под индексом i)
                TextView nameTextView = new TextView(context);
                nameTextView.setText(textTitleArray[i]);
                nameTextView.setTextColor(Color.BLACK);
                nameTextView.setTextSize(16); // Делаем текст чуть крупнее шапки
                nameTextView.setGravity(Gravity.START);
                nameTextView.setPadding(8, 4, 8, 4);
                nameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                ));

                // Текстовое поле для количества (берем из массива countTitleArray)
                TextView countTextView = new TextView(context);
                countTextView.setText(countTitleArray[i]);
                countTextView.setTextColor(Color.BLACK);
                countTextView.setTextSize(16);
                countTextView.setGravity(Gravity.CENTER);
                countTextView.setPadding(8, 4, 8, 4);
                countTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.5f
                ));

                // Собираем строку и добавляем в контейнер
                dataRow.addView(nameTextView);
                dataRow.addView(countTextView);
                container.addView(dataRow);
            }

            // --- ПОКАЗЫВАЕМ ИНДИКАТОР СКРЫТЫХ ЭЛЕМЕНТОВ ---
            // Если реальное количество позиций в заметке больше, чем мы можем показать (больше 3-х),
            // рисуем внизу блеклую надпись, например: "Еще 2"
            if (maxLength > 3) {
                TextView moreIndicator = new TextView(context);
                moreIndicator.setText("Eщe " + (maxLength - 3));
                moreIndicator.setTextColor(Color.GRAY);
                moreIndicator.setTextSize(12);
                moreIndicator.setGravity(Gravity.CENTER);
                moreIndicator.setPadding(8, 2, 8, 2);
                moreIndicator.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                container.addView(moreIndicator);
            }

            // Поскольку мы добавляем элементы (TextView) программно "на лету", StaggeredGridLayoutManager
            // часто не успевает правильно высчитать новую высоту карточки. Из-за этого при скролле
            // текст может накладываться друг на друга, карточки могут обрезаться снизу или прыгать.
            // Метод .post() заставляет код внутри run() выполниться только ПОСЛЕ того,
            // как система полностью отрисует все добавленные элементы.
            container.post(new Runnable() {
                @Override
                public void run() {
                    // Измеряем содержимое
                    // Принудительно замеряем реальные габариты контейнера с учетом всех только что добавленных строк
                    container.measure(
                            View.MeasureSpec.makeMeasureSpec(container.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    );

                    // Устанавливаем точную высоту
                    // Берем получившуюся правильную высоту (getMeasuredHeight)
                    // и намертво прописываем её в параметры контейнера (LayoutParams).
                    // Это фиксирует размер карточки и предотвращает любые "прыжки" при переиспользовании.
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
                    if (params != null) {
                        params.height = container.getMeasuredHeight();
                        container.setLayoutParams(params);
                    }
                }
            });
        }
    }

    /**
     * Этот метод используется для работы функции поиска по заметкам.
     * Он вызывается каждый раз, когда пользователь вводит или удаляет букву в строке поиска.
     * Метод получает новый, уже обрезанный список заметок (например, только те, в которых есть введенное слово),
     * и заставляет адаптер отобразить именно их.
     */
    @SuppressLint("NotifyDataSetChanged") // Скрываем предупреждение системы о том, что обновлять весь список разом — не самый оптимальный подход
    public void filterList (List <Notes> filterAddList) {
        // Заменяем старый список заметок в адаптере (который отображался на экране до этого)
        // на новый, отфильтрованный список, который мы передали в метод.
        list = filterAddList;
        // Даем команду самому RecyclerView: "Внутренний список данных полностью изменился!
        // Сотри текущие карточки с экрана и перерисуй их заново на основе нового списка".
        notifyDataSetChanged();
    }

    /**
     * Этот метод — обязательная базовая функция любого адаптера (RecyclerView).
     * Он сообщает системе (Android), сколько всего элементов (карточек)
     * нужно подготовить и отрисовать на экране в данный момент.
     */
    @Override
    public int getItemCount() {
        // Возвращает текущее количество элементов в списке (list).
        // Если вы, например, ввели текст в поиск (вызвали метод filterList выше),
        // список list заменяется на укороченный. Система автоматически вызывает getItemCount(),
        // видит новое, меньшее количество элементов, и рисует ровно столько карточек, сколько нашлось.
        return list.size();
    }

    /**
     * Этот метод работает в тесной связке с командой setHasStableIds(true),
     * которую мы включали при первоначальной настройке списка (в методе updateRecycle).
     * Он критически важен для плавности интерфейса и отключения "дерганий" при выделении карточек.
     */
    @Override
    public long getItemId(int position) {
        // По умолчанию Android отличает карточки друг от друга просто по их порядковому номеру (0, 1, 2...).
        // Проблема в том, что если удалить нулевой элемент, все остальные сдвинутся, их номера изменятся,
        // и системе придется перерисовывать весь экран заново (что вызывает мерцание).

        // Переопределяя этот метод, мы говорим системе: "Не смотри на временную позицию!
        // У каждой заметки уже есть свой железобетонный, уникальный ID прямиком из базы данных Room".
        //
        // Возвращая этот ID, мы позволяем RecyclerView четко отслеживать жизненный цикл каждой конкретной
        // карточки. Теперь при появлении галочки или удалении заметки система обновит только один нужный элемент,
        // не трогая остальные.
        return list.get(position).getID();
    }


    /**
     * Этот метод — "сердце" главного экрана. Он берет пустую карточку (holder) и заполняет её
     * данными конкретной заметки. Здесь же прописана вся сложная логика того, как карточка
     * должна выглядеть в обычном режиме и в режиме выделения (множественного выбора).
     */
    @Override
    public void onBindViewHolder(@NonNull NotesViewHolderMain holder, int position) {
        // Получаем текущую заметку из списка
        Notes notes = list.get(position);
        // Вставляем заголовок и активируем "бегущую строку", если текст не помещается
        holder.textView_title.setSelected(true);
        holder.textView_title.setText(list.get(position).getTitle());

        // Вызываем метод, который мы разбирали ранее: он программно нарисует
        // мини-таблицу со списком покупок внутри контейнера textView_notes
        displayTableData(holder.textView_notes, list.get(position));

        // Устанавливаем дату и тоже включаем для нее "бегущую строку"
        holder.textView_date.setSelected(false);
        holder.textView_date.setText(list.get(position).getDate());

        // Проверяем, закреплена ли заметка (для обычного режима)
        if (list.get(position).isPinned()) {
            holder.imageView_pin.setImageResource(R.drawable.pinned);
        } else {
            holder.imageView_pin.setImageResource(0);
        }

        // --- ХИТРОСТЬ ДЛЯ ПРЕДОТВРАЩЕНИЯ ПУСТЫХ ПРОСТРАНСТВ В СЕТКЕ ---
        // Так как мы генерируем содержимое карточки динамически, StaggeredGrid иногда сходит с ума
        // и оставляет огромные дыры между карточками. Здесь мы принудительно заставляем контейнер
        // измерить свое содержимое (measure) и жестко фиксируем его высоту (LayoutParams).
        holder.textView_notes.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.textView_notes.getLayoutParams();
        if (params != null) {
            params.height = holder.textView_notes.getMeasuredHeight();
            holder.textView_notes.setLayoutParams(params);
        }

        // --- ФОН КАРТОЧКИ ---
        holder.notes_container.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.white, null));

        // --- ОБРАБОТКА ОБЫЧНОГО КЛИКА ПО КАРТОЧКЕ ---
        holder.notes_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Если мы сейчас находимся в режиме выделения нескольких заметок...
                if (isSelectionMode) {
                    // МГНОВЕННО меняем картинку прямо во View!
                    // Мы не просим адаптер перерисовать всю карточку, мы просто прячем одну
                    // иконку и показываем другую. Это делает выделение идеально плавным.
                    if (selectedNotes.contains(notes)) {
                        // Если карточка уже была выделена — снимаем выделение
                        selectedNotes.remove(notes); // Удаляем из списка
                        holder.check_mark_on.setVisibility(View.GONE); // Прячем зеленую галочку
                        holder.check_mark_off.setVisibility(View.VISIBLE); // Показываем пустой кружок
                    } else {
                        // Если не была выделена — выделяем
                        selectedNotes.add(notes); // Добавляем в список
                        holder.check_mark_on.setVisibility(View.VISIBLE); // Показываем зеленую галочку
                        holder.check_mark_off.setVisibility(View.GONE); // Прячем пустой кружок
                    }
                }
                // Передаем сигнал в MainActivity (вдруг список стал пустым и надо выйти)
                // В любом случае передаем сигнал в MainActivity.
                // Если это обычный режим, откроется редактор.
                // Если режим выделения, MainActivity проверит, не стал ли список выделенных пустым, чтобы выйти из режима.
                listener.onClick(notes);
            }
        });

        // Обработка долгого нажатия
        holder.notes_container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Сообщаем MainActivity, что на карточку нажали долго.
                // Именно там запустится режим выделения (startSelectionMode).
                listener.onLongClick(list.get(holder.getAdapterPosition()), holder.notes_container);
                // Возвращаем true! Это означает: "Мы полностью обработали долгое нажатие,
                // не нужно после него случайно вызывать обычный клик".
                return true;
            }
        });

        // Логика отображения иконок в зависимости от режима
        if (isSelectionMode) {
            // Прячем иконку закрепления в режиме выделения
            holder.imageView_pin.setVisibility(View.GONE);

            // Если заметка есть в списке выделенных — ставим зеленую галочку (on)
            if (selectedNotes.contains(notes)) {
                holder.check_mark_on.setVisibility(View.VISIBLE);
                holder.check_mark_off.setVisibility(View.GONE);
            } else {
                // Если нет — ставим пустой кружок (off)
                holder.check_mark_on.setVisibility(View.GONE);
                holder.check_mark_off.setVisibility(View.VISIBLE);
            }
        } else {
            // Обычный режим
            holder.check_mark_on.setVisibility(View.GONE);
            holder.check_mark_off.setVisibility(View.GONE);

            // Показываем пин только если заметка закреплена
            if (notes.isPinned()) {
                holder.imageView_pin.setVisibility(View.VISIBLE);
            } else {
                holder.imageView_pin.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Этот метод — обязательная базовая часть адаптера главного списка (RecyclerView).
     * Он отвечает за создание новых "пустых" карточек-оболочек для заметок на главном экране.
     * Система вызывает его только тогда, когда ей нужна новая карточка, а готовых
     * (переиспользуемых при скролле) в памяти в данный момент нет.
     */
    @NonNull
    @Override
    public NotesViewHolderMain onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 1. LayoutInflater.from(context) берет системный инструмент, который умеет
        // читать XML-код дизайна и превращать его в реальные, осязаемые элементы интерфейса.

        // 2. .inflate(R.layout.main_notes_list, parent, false) берет ваш файл дизайна
        // главной карточки заметки (main_notes_list.xml) и "надувает" его.
        // 'parent' (сам RecyclerView) передается для того, чтобы карточка понимала свои границы.
        // 'false' означает: "создай виджет, но пока не приклеивай его к списку намертво,
        // RecyclerView сам разберется, когда именно показать его на экране".

        // 3. new NotesViewHolderMain(...) берет эту только что созданную разметку,
        // заворачивает её в ваш класс-держатель (ViewHolder) и отдает системе.
        // Позже эта готовая пустая карточка автоматически попадет в метод onBindViewHolder,
        // который мы разбирали шагом ранее, чтобы заполниться текстом, цветом и таблицей.
        return new NotesViewHolderMain(LayoutInflater.from(context).inflate(R.layout.main_notes_list, parent, false));
    }

    /**
     * Этот метод — шедевр оптимизации вашего интерфейса! Он отвечает за глобальное включение
     * или выключение режима выделения заметок.
     * Самая главная фишка здесь: мы НЕ используем команду notifyDataSetChanged(),
     * которая заставила бы весь список моргать, пересчитывать позиции и перерисовывать все карточки с нуля.
     * Вместо этого мы точечно обращаемся только к тем карточкам, которые пользователь видит прямо сейчас.
     */
    @SuppressLint("NotifyDataSetChanged") // Аннотация осталась от старого кода, её можно даже удалить, так как notifyDataSetChanged мы больше не вызываем
    public void setSelectionMode(boolean isSelectionMode, List<Notes> selectedNotes, RecyclerView recyclerView) {
        // Сохраняем новые состояния: включен ли режим и какие заметки сейчас выделены
        this.isSelectionMode = isSelectionMode;
        this.selectedNotes = selectedNotes;

        // Мы удалили notifyDataSetChanged()!
        // Вместо этого мы вручную обновляем только те карточки, которые сейчас на экране.

        // Запускаем цикл. recyclerView.getChildCount() возвращает количество карточек,
        // видимых на экране в данную секунду (например, 6-8 штук), а не весь список из 1000 заметок.
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            // Получаем физическую карточку (View) под номером i на экране
            View child = recyclerView.getChildAt(i);

            // Замените NotesViewHolder на ваше название (например, NotesViewHolderMain), если оно отличается
            NotesViewHolderMain holder = (NotesViewHolderMain) recyclerView.getChildViewHolder(child);

            // Проверяем, что холдер действительно существует (защита от ошибок)
            if (holder != null) {
                // Узнаем, какую позицию в главном списке данных (list) занимает эта конкретная карточка
                int position = holder.getAdapterPosition();
                // Проверяем, что позиция валидна (NO_POSITION бывает, если карточка в этот момент удаляется или анимируется)
                if (position != RecyclerView.NO_POSITION) {
                    // Достаем саму заметку
                    Notes note = list.get(position);

                    // --- ЖИВАЯ СМЕНА ИНТЕРФЕЙСА ---
                    // Здесь происходит то же самое, что и в onBindViewHolder, но "наживую", прямо на глазах у пользователя,
                    // без перезагрузки всей сетки StaggeredGrid.
                    if (isSelectionMode) {
                        // Если режим включен: прячем булавку, чтобы не мешала
                        holder.imageView_pin.setVisibility(View.GONE);
                        // Смотрим, есть ли эта заметка в списке выделенных, и ставим нужную галочку
                        if (selectedNotes.contains(note)) {
                            holder.check_mark_on.setVisibility(View.VISIBLE);
                            holder.check_mark_off.setVisibility(View.GONE);
                        } else {
                            holder.check_mark_on.setVisibility(View.GONE);
                            holder.check_mark_off.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // Если мы выходим из режима выделения: прячем все галочки и кружочки
                        holder.check_mark_on.setVisibility(View.GONE);
                        holder.check_mark_off.setVisibility(View.GONE);
                        // И возвращаем булавку на место, если заметка была ранее закреплена
                        if (note.isPinned()) {
                            holder.imageView_pin.setVisibility(View.VISIBLE);
                        } else {
                            holder.imageView_pin.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }
}

/**
 * Этот класс — "держатель" (ViewHolder) для элементов главного списка (RecyclerView).
 * Паттерн ViewHolder — это основа производительности списков в Android.
 * Он позволяет найти все элементы интерфейса (иконки, тексты, контейнеры) внутри карточки
 * ровно один раз при её создании и сохранить их в памяти. Если бы мы искали их заново
 * при каждой прокрутке, список бы сильно тормозил.
 */
class NotesViewHolderMain extends RecyclerView.ViewHolder {

    ImageView check_mark_off, check_mark_on;
    ImageView imageView_pin;
    CardView notes_container;
    LinearLayout textView_notes;
    TextView textView_title, textView_date;

    // Конструктор: вызывается в методе onCreateViewHolder, когда система "надула"
    // пустую разметку из XML и передала её сюда в виде объекта (itemView).
    public NotesViewHolderMain(@NonNull View itemView) {
        // Обязательный вызов родительского конструктора
        super(itemView);

        // "Привязываем" наши Java-переменные к реальным элементам из файла разметки (main_notes_list.xml)
        // с помощью поиска по их ID (findViewById).
        // Обратите внимание, что мы ищем элементы не на всем экране (как в Activity),
        // а строго внутри конкретной переданной карточки (itemView).
        notes_container = itemView.findViewById(R.id.menu_list_container); // Главная рамка
        textView_title = itemView.findViewById(R.id.textView_title); // Текст заголовка
        textView_notes = itemView.findViewById(R.id.textView_notes); // Место для вставки таблицы
        textView_date = itemView.findViewById(R.id.textView_date); // Текст даты
        imageView_pin = itemView.findViewById(R.id.imageView_pin); // Картинка булавки

        // Картинки для режима множественного выбора
        check_mark_on = itemView.findViewById(R.id.check_mark_on);
        check_mark_off = itemView.findViewById(R.id.check_mark_off);
    }
}
