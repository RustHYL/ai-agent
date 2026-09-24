package com.wulang.aiagent.rag;


import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

public class LoveAppContextualQueryAugmenterFactory {


    public static ContextualQueryAugmenter getContextualQueryAugmenter() {
        PromptTemplate emptyContext = new PromptTemplate("""
                你应该输出以下内容：
                抱歉，我们只能回答相关内容问题，若有需求请联系客服。
                """);
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContext)
                .build();
    }


}
