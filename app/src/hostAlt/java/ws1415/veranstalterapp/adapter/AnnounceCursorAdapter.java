package ws1415.veranstalterapp.adapter;

/**
 * Klasse zum Füllen der ListView in AnnounceInformationFragment.
 * <p/>
 * Created by Martin Wrodarczyk on 28.11.2014.
 */
//public class AnnounceCursorAdapter extends BaseAdapter {
//    private List<Field> fieldList = new ArrayList<Field>();
//    private PictureChooserActivity parent;
//    private Context context;
//    private LayoutInflater inflater;
//    private Event event;
//    private boolean edit_mode;
//    /**
//     * Cache für die Bilder des Eevnts, damit diese nicht bei jedem Scrollen neu skaliert werden müssen.
//     */
//    private HashMap<Field, Bitmap> bitmapCache = new HashMap<Field, Bitmap>();
//
//    private int year;
//    private int month;
//    private int day;
//    private int hour;
//    private int minute;
//
//    private Route route;
//    private Button routePickerButton;
//
//    /**
//     * Konstruktor, der den Inhalt der Liste festlegt.
//     *
//     * @param parent    Activity, von der aus der Adapter aufgerufen wird
//     * @param fieldList Liste von den Routen
//     */
//    public AnnounceCursorAdapter(AnnounceInformationFragment parent, List<Field> fieldList, Event event) {
//        this.context = parent.getActivity();
//        this.fieldList = fieldList;
//        this.event = event;
//        this.parent = parent;
//        setStandardTime();
//        setCurrentDate();
//    }
//
//    /**
//     * Konstruktor, der den Inhalt der Liste festlegt.
//     */
//    public AnnounceCursorAdapter(EditEventActivity parent, List<Field> fieldList, Event event){
//        this.context = parent;
//        this.fieldList = fieldList;
//        this.event = event;
//        this.parent = parent;
//        setDate(new Date(event.getMetaData().getDate().getValue()));
//        setRoute(event.getRoute());
//    }
//
//    /**
//     * Gibt die Anzahl der zu bearbeitenden EventFelder in der Liste zurück.
//     *
//     * @return Anzahl der EventFelder
//     */
//    @Override
//    public int getCount() {
//        if (fieldList == null) {
//            return 0;
//        } else {
//            if (edit_mode) {
//                return fieldList.size() * 2 + 1;
//            } else {
//                return fieldList.size();
//            }
//        }
//    }
//
//    /**
//     * Gibt das zu bearbeitende Feld an der Stelle i in der Liste zurück.
//     *
//     * @param i Stelle
//     * @return Feld
//     */
//    @Override
//    public Field getItem(int i) {
//        return fieldList.get(i);
//    }
//
//    /**
//     * Gibt die Id der Route in der Liste zurück.
//     *
//     * @param i
//     * @return
//     */
//    @Override
//    public long getItemId(int i) {
//        return i;
//    }
//
//    /**
//     * Klasse zum Halten der GUI Elemente für ein ButtonField.
//     */
//    private class HolderButtonField {
//        TextView title;
//        Button button;
//    }
//
//    /**
//     * Klasse zum Halten der GUI Elemente um Felder zu ergänzen.
//     */
//    private class HolderAddField {
//        Button addFieldButton;
//    }
//
//    /**
//     * Klassen zum Halten der GUI um Felder für ein TextField.
//     */
//    private class HolderUniqueTextField {
//        TextView title;
//        EditText content;
//    }
//    private class HolderSimpleTextField {
//        TextView title;
//        EditText content;
//        Button deleteButton;
//    }
//    private class HolderPictureField {
//        TextView title;
//        Button button;
//        Button deleteButton;
//        ImageView image;
//    }
//
//    /**
//     * Setzt das Layout der Items in der ListView.
//     *
//     * @param position    Position in der ListView
//     * @param convertView
//     * @param viewGroup
//     * @return die View des Items an der Stelle position in der Liste
//     */
//    @Override
//    public View getView(int position, View convertView, ViewGroup viewGroup) {
//        View view = null;
//        // TODO Verwendung von Dynamic Fields anpassen
////        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
////
////        if (position % 2 == 1 || edit_mode == false) {
////            if (edit_mode) position = position / 2;
////
////            final Field field = getItem(position);
////
////            final int focusPos = position;
////
////            // Textwatcher um die temporären Änderungen von den EditTexts zu speichern
////            TextWatcher watcher = new TextWatcher() {
////                @Override
////                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
////                    fieldList.get(focusPos).setValue(charSequence.toString());
////                }
////
////                @Override
////                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
////                }
////
////                @Override
////                public void afterTextChanged(Editable editable) {
////                }
////            };
////
////            if (field.getType() == FieldType.TITLE.getId() ||
////                    field.getType() == FieldType.LOCATION.getId() ||
////                    field.getType() == FieldType.DESCRIPTION.getId()) {
////                HolderUniqueTextField holder = new HolderUniqueTextField();
////                view = inflater.inflate(R.layout.list_view_item_announce_information_unique_text, viewGroup, false);
////                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_uniquetext_textView);
////                holder.content = (EditText) view.findViewById(R.id.list_view_item_announce_information_uniquetext_editText);
////
////                setTitleAndContentInView(holder.title, holder.content, position, watcher);
////            } else if (field.getType() == FieldType.FEE.getId()) {
////                HolderUniqueTextField holder = new HolderUniqueTextField();
////                view = inflater.inflate(R.layout.list_view_item_announce_information_fee, viewGroup, false);
////                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_fee_textView);
////                holder.content = (EditText) view.findViewById(R.id.list_view_item_announce_information_fee_editText);
////
////                setTitleAndContentInView(holder.title, holder.content, position, watcher);
////            } else if (field.getType() == FieldType.DATE.getId()) {
////                HolderButtonField holder = new HolderButtonField();
////                view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
////                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
////                holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);
////
////                setDateInView(holder.title, holder.button, position);
////            } else if (field.getType() == FieldType.TIME.getId()) {
////                HolderButtonField holder = new HolderButtonField();
////                view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
////                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
////                holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);
////
////                setTimeInView(holder.title, holder.button, position);
////            } else if (field.getType() == FieldType.ROUTE.getId()) {
////                HolderButtonField holder = new HolderButtonField();
////                view = inflater.inflate(R.layout.list_view_item_announce_information_button, viewGroup, false);
////                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_button_textView);
////                holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_button_button);
////                holder.title.setText(field.getTitle());
////                setRouteInView(holder.title, holder.button, position);
////            } else if (field.getType() == FieldType.SIMPLETEXT.getId() ||
////                    field.getType() == FieldType.LINK.getId()) {
////                HolderSimpleTextField holder = new HolderSimpleTextField();
////                view = inflater.inflate(R.layout.list_view_item_announce_information_simpletext, viewGroup, false);
////                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_simpletext_textView);
////                holder.content = (EditText) view.findViewById(R.id.list_view_item_announce_information_simpletext_editText);
////                holder.deleteButton = (Button) view.findViewById(R.id.list_view_item_announce_information_simpletext_deleteButton);
////
////                setSimpleTextInView(holder.title, holder.content, holder.deleteButton, position, watcher);
////            } else if (field.getType() == FieldType.PICTURE.getId()) {
////                final HolderPictureField holder = new HolderPictureField();
////                view = inflater.inflate(R.layout.list_view_item_announce_information_picture, viewGroup, false);
////                holder.title = (TextView) view.findViewById(R.id.list_view_item_announce_information_picture_textView);
////                holder.button = (Button) view.findViewById(R.id.list_view_item_announce_information_picture_button);
////                holder.deleteButton = (Button) view.findViewById(R.id.list_view_item_announce_information_picture_deleteButton);
////                holder.image = (ImageView) view.findViewById(R.id.list_view_item_announce_information_picture_picture);
////                setPictureInView(holder.title, holder.deleteButton, position);
////
////                Bitmap bm = bitmapCache.get(field);
////                if (bm == null) {
////                    Text encodedBytes = field.getData();
////                    if (encodedBytes != null) {
////                        byte[] bytes = Base64.decodeBase64(encodedBytes.getValue());
////                        // Zunächst nur Auflösung des Bilds abrufen und passende SampleSize berechnen
////                        BitmapFactory.Options options = new BitmapFactory.Options();
////                        options.inJustDecodeBounds = true;
////                        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
////                        options.inSampleSize = ImageUtil.calculateInSampleSize(options, 720);
////                        // Skalierte Version des Bilds abrufen
////                        options.inJustDecodeBounds = false;
////                        bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
////                        bitmapCache.put(field, bm);
////                    }
////                }
////                // Bild anzeigen
////                holder.image.setImageBitmap(bm);
////
////                final int finalPosition = position;
////                holder.button.setOnClickListener(new View.OnClickListener() {
////                    @Override
////                    public void onClick(View v) {
////                        Intent intent = new Intent(Intent.ACTION_PICK,
////                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
////                        intent.setType("image/*");
////                        parent.startActivityForResult(Intent.createChooser(intent, context.getString(R.string.choose_image)), finalPosition);
////                        bitmapCache.remove(field);
////                    }
////                });
////            }
////        } else {
////            HolderAddField holder = new HolderAddField();
////            final int pos = position;
////            view = inflater.inflate(R.layout.list_view_item_announce_information_plus_item, viewGroup, false);
////            holder.addFieldButton = (Button) view.findViewById(R.id.list_view_item_announce_information_plus_item_button);
////            holder.addFieldButton.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View view) {
////                    createFieldChooser(pos);
////                }
////            });
////        }
//        return view;
//    }
//
//    private void setTitleAndContentInView(TextView title, EditText content, int position, TextWatcher watcher) {
//        content.addTextChangedListener(watcher);
//        if (getItem(position).getValue() != null)
//            content.setText(getItem(position).getValue().toString());
//        else content.setText("");
//        title.setText(getItem(position).getTitle());
//    }
//
//    private void setDateInView(TextView title, Button button, int position) {
//        title.setText(getItem(position).getTitle());
//        setDateListener(button, getItem(position));
//        button.setText(day + "." + (month + 1) + "." + year);
//    }
//
//    private void setTimeInView(TextView title, Button button, int position) {
//        title.setText(getItem(position).getTitle());
//        setTimeListener(button, getItem(position));
//        if (minute < 10) button.setText(hour + ":0" + minute + " Uhr");
//        else button.setText(hour + ":" + minute + " Uhr");
//    }
//
//    private void setRouteInView(TextView title, Button button, int position) {
//        routePickerButton = button;
//
//        title.setText(getItem(position).getTitle());
//        setRouteListener(button);
//        if (route != null) button.setText(route.getName());
//        else button.setText(context.getResources().getString(R.string.announce_info_choose_map));
//    }
//
//    private void setPictureInView(TextView title, Button deleteButton, int position) {
//        if (!edit_mode) deleteButton.setVisibility(View.GONE);
//        title.setText(getItem(position).getTitle());
//        final int pos = position;
//        deleteButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                notifyDataSetChanged();
//            }
//        });
//    }
//
//    private void setSimpleTextInView(TextView title, EditText content, Button deleteButton, int position, TextWatcher watcher) {
//        setTitleAndContentInView(title, content, position, watcher);
//        if (!edit_mode) deleteButton.setVisibility(View.GONE);
//        final int pos = position;
//        deleteButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                notifyDataSetChanged();
//            }
//        });
//    }
//
//    /**
//     * Erstellt einen Dialog um ein Editierfeld auszuwählen.
//     *
//     * @param position
//     */
//    public void createFieldChooser(final int position) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle(R.string.field_picker_title)
//                .setItems(R.array.field_picker, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        createTitleInput(which, position);
//                    }
//                });
//        builder.show();
//    }
//
//    /**
//     * Erstellt je nach Auswahl, ein Editierfeld mit dem angegebenen Titel.
//     *
//     * @param which    Auswahl des Editierfelds
//     * @param position Position in der ListView
//     */
//    public void createTitleInput(final int which, final int position) {
//        AlertDialog.Builder alert = new AlertDialog.Builder(context);
//
//        if (which == 0) alert.setTitle(R.string.field_picker_text);
//        else if (which == 1) alert.setTitle(R.string.field_picker_picture);
//        else if (which == 2) alert.setTitle(R.string.field_picker_link);
//
//        final EditText input = new EditText(context);
//        input.setSingleLine(true);
//
//        // Setzt die Maximal-Länge
//        InputFilter[] filters = new InputFilter[1];
//        filters[0] = new InputFilter.LengthFilter(15); //Filter to 10 characters
//        input.setFilters(filters);
//
//        alert.setView(input);
//
//        // TODO Verwendung von Dynamic Fields anpassen
////        alert.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
////            public void onClick(DialogInterface dialog, int whichButton) {
////                String value = input.getText().toString() + ":";
////
////                if (which == 0)
////                    EventUtils.getInstance(context).addDynamicField(value, FieldType.SIMPLETEXT, event, position / 2);
////                else if (which == 1)
////                    EventUtils.getInstance(context).addDynamicField(value, FieldType.PICTURE, event, position / 2);
////                else if (which == 2)
////                    EventUtils.getInstance(context).addDynamicField(value, FieldType.LINK, event, position / 2);
////                notifyDataSetChanged();
////            }
////        });
//
//        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//            }
//        });
//
//        alert.show();
//    }
//
//    /**
//     * Setzt den DateListener auf den DateButton.
//     *
//     * @param button Button des Editierfeldes Datum
//     * @param field  Das Field-Objekt in das das Datum geschrieben werden soll
//     */
//    private void setDateListener(final Button button, final Field field) {
//        final OnDateSetListener datePickerListener = new OnDateSetListener() {
//
//            // when dialog box is closed, below method will be called.
//            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
//                year = selectedYear;
//                month = selectedMonth;
//                day = selectedDay;
//
//                button.setText(day + "." + (month + 1) + "." + year);
//            }
//        };
//
//        button.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new DatePickerDialog(context, datePickerListener, year, month, day).show();
//            }
//        });
//    }
//
//    /**
//     * Setzt den TimeListener auf den Uhrzeit Button.
//     *
//     * @param button Button für die Uhrzeit
//     * @param field  Das Field-Objekt in das das Datum geschrieben werden soll
//     */
//    private void setTimeListener(final Button button, final Field field) {
//        final OnTimeSetListener timePickerListener = new OnTimeSetListener() {
//            @Override
//            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
//                hour = selectedHour;
//                minute = selectedMinute;
//                if (minute < 10) {
//                    button.setText(hour + ":0" + minute + " Uhr");
//                } else {
//                    button.setText(hour + ":" + minute + " Uhr");
//                }
//            }
//        };
//
//        button.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new TimePickerDialog(context, timePickerListener, hour, minute, true).show();
//            }
//        });
//    }
//
//    /**
//     * Setzt die Standardzeit(20 Uhr).
//     */
//    public void setStandardTime() {
//        hour = 20;
//        minute = 0;
//    }
//
//    /**
//     * Setzt das aktuelle Datum als Text auf den datePickerButton.
//     */
//    public void setCurrentDate() {
//        final Calendar c = Calendar.getInstance();
//        year = c.get(Calendar.YEAR);
//        month = c.get(Calendar.MONTH);
//        day = c.get(Calendar.DAY_OF_MONTH);
//    }
//
//    /**
//     * Setzt den Click-Listener auf den Routeauswahl-Button um die Route auszuwählen.
//     *
//     * @param button Button um die Route zu setzen
//     */
//    private void setRouteListener(final Button button) {
//        button.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent chooseRouteIntent = new Intent(context, ChooseRouteDialog.class);
//                context.startActivity(chooseRouteIntent);
//            }
//        });
//    }
//
//    /**
//     * Dies Methode setzt die Route für das Event
//     *
//     * @param selectedRoute Die Route für das Event
//     */
//    public void setRouteAndText(Route selectedRoute) {
//        route = selectedRoute;
//        routePickerButton.setText(selectedRoute.getName());
//    }
//
//    /**
//     * Aktiviert den Editiermodus und updated die ListView.
//     */
//    public void startEditMode() {
//        edit_mode = true;
//        notifyDataSetChanged();
//    }
//
//    /**
//     * Deaktiviert den Editiermodus und updated die ListView.
//     */
//    public void exitEditMode() {
//        edit_mode = false;
//        notifyDataSetChanged();
//    }
//
//    public boolean getEditMode() {
//        return edit_mode;
//    }
//
//    public List<Field> getFieldlist() {
//        return fieldList;
//    }
//
//    public void setFieldlist(List<Field> fieldList) {
//        this.fieldList = fieldList;
//    }
//
//    public Date getDate() {
//        Calendar cal = Calendar.getInstance();
//        cal.set(year, month, day, hour, minute, 0);
//        return cal.getTime();
//    }
//
//    public void setDate(Date date){
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(date);
//        year = cal.get(Calendar.YEAR);
//        month = cal.get(Calendar.MONTH);
//        day = cal.get(Calendar.DAY_OF_MONTH);
//        hour = cal.get(Calendar.HOUR_OF_DAY);
//        minute = cal.get(Calendar.MINUTE);
//    }
//
//    public Route getRoute() {
//        return route;
//    }
//
//    public void setRoute(Route route) {
//        this.route = route;
//    }
//
//    /**
//     * Verarbeitet ein Bild, das über den PictureChooser ausgewählt wurde.
//     * @param fieldPosition Die Position des dynamischen Feldes, dessen Bild geändert werden soll.
//     * @param data Die Daten, die vom Chooser zurückgegeben wurden
//     */
//    public void processImage(int fieldPosition, Intent data) {
//        Field pictureField = getItem(fieldPosition);
//        if (data != null && pictureField != null) {
//            // Pfad ermitteln
//            Uri selectedImageUri = data.getData();
//            String[] projection = {MediaStore.MediaColumns.DATA};
//            CursorLoader cl = new CursorLoader(context);
//            cl.setUri(selectedImageUri);
//            cl.setProjection(projection);
//            Cursor cursor = cl.loadInBackground();
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
//            cursor.moveToFirst();
//            String tempPath = cursor.getString(column_index);
//            cursor.close();
//            Bitmap bm;
//            BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
//            bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            byte[] byteArray = stream.toByteArray();
//            pictureField.setData(new Text().setValue(android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)));
//
//            // Speicher des Bildes zum Freigeben vorbereiten
//            bm.recycle();
//
//            // Bild in die ListView übernehmen
//            this.notifyDataSetChanged();
//        }
//    }
//
//    /**
//     * Interface für Activities und Fragments, die einen PictureChooser anzeigen können.
//     */
//    public interface PictureChooserActivity {
//        public void startActivityForResult(Intent intent, int requestCode);
//    }
//
//    // Getter und Setter für die Tests
//    public int getYear() {
//        return year;
//    }
//
//    public void setYear(int year) {
//        this.year = year;
//    }
//
//    public int getMonth() {
//        return month;
//    }
//
//    public void setMonth(int month) {
//        this.month = month;
//    }
//
//    public int getDay() {
//        return day;
//    }
//
//    public void setDay(int day) {
//        this.day = day;
//    }
//
//}
