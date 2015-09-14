package com.fantasy.security.bean.databind;

import com.fantasy.security.bean.Menu;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class MenuDeserializer extends JsonDeserializer<Menu> {

    @Override
    public Menu deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        return new Menu(jp.getValueAsLong());
    }
}