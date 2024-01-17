package com.website.util;

/**
 * @author ahl
 * @desc
 * @create 2023/7/27 10:12
 */
public class ChatGPTUtil {

    static enum Model {
        GPT_3_5_TURBO("gpt-3.5-turbo", 4096),
        GPT_3_5_TURBO_0613("gpt-3.5-turbo-0613", 4096),
        GPT_3_5_TURBO_16K("gpt-3.5-turbo-16k", 16384),
        GPT_3_5_TURBO_16K_0613("gpt-3.5-turbo-16k-0613", 16384),
        GPT_4("gpt-4", 8192),
        GPT_4_32K("gpt-4-32k", 32768),
        GPT_4_0613("gpt-4-0613", 8192),
        GPT_4_32K_0613("gpt-4-32k-0613", 32768),
        GPT_4_1106_PREVIEW("gpt-4-1106-preview", 128000);


        private final String name;
        private final int tokens;

        public String getName() {
            return this.name;
        }
        public int getTokens() {
            return this.tokens;
        }

        private Model(String name, int tokens) {
            this.name = name;
            this.tokens = tokens;
        }

        public static Model getModelEnumByName(String name) {
            for (Model model : Model.values()) {
                if (model.name.equals(name)) {
                    return model;
                }
            }
            return GPT_3_5_TURBO;
        }
    }


    static public int getMaxTokensByModel(String modelName) {
        Model model = Model.getModelEnumByName(modelName);
        return model.tokens;
    }

    static public int getMaxTokensByModel(String modelName, int hasTokens) {
        Model model = Model.getModelEnumByName(modelName);
        //多减10个防止出错
        int maxToken = model.tokens - hasTokens - 10;
        // maxToken 最大 4096
        if (maxToken > 4096) {
            maxToken = 4096;
        }
        return maxToken;
    }
}
