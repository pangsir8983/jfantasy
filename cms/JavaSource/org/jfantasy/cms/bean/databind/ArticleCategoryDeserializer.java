package org.jfantasy.cms.bean.databind;

import org.jfantasy.cms.bean.ArticleCategory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class ArticleCategoryDeserializer extends JsonDeserializer<ArticleCategory> {

    @Override
    public ArticleCategory deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String code = jp.getValueAsString();
        if (code == null)
            return null;
        return new ArticleCategory(code);
    }
}
