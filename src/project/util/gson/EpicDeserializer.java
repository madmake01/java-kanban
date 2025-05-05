package project.util.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import project.enums.Status;
import project.model.Epic;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EpicDeserializer implements JsonDeserializer<Epic> {
    @Override
    public Epic deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObj = jsonElement.getAsJsonObject();

        Epic.Builder builder = new Epic.Builder();

        if (jsonObj.has("id")) {
            builder.setId(jsonObj.get("id").getAsInt());
        }
        if (jsonObj.has("name")) {
            builder.setName(jsonObj.get("name").getAsString());
        }
        if (jsonObj.has("description")) {
            builder.setDescription(jsonObj.get("description").getAsString());
        }
        if (jsonObj.has("status")) {
            builder.setStatus(context.deserialize(jsonObj.get("status"), Status.class));
        }
        if (jsonObj.has("startTime")) {
            builder.setStartTime(context.deserialize(jsonObj.get("startTime"), LocalDateTime.class));
        }

        List<Integer> subtaskIds;
        if (jsonObj.has("subtaskIds") && jsonObj.get("subtaskIds").isJsonArray()) {
            subtaskIds = context.deserialize(jsonObj.get("subtaskIds"), List.class);
        } else {
            subtaskIds = new ArrayList<>();
        }
        builder.setSubtaskIds(subtaskIds);

        return builder.build();
    }
}
