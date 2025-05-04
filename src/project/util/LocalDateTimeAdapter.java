package project.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime time) throws IOException {
        jsonWriter.value(FORMATTER.format(time));
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        return FORMATTER.parse(jsonReader.nextString(), LocalDateTime::from);
    }
}
