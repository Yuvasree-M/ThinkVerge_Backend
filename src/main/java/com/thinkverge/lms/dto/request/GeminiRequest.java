package com.thinkverge.lms.dto.request;

import java.util.List;

public class GeminiRequest {

    public List<Content> contents;

    public static class Content {
        public List<Part> parts;
    }

    public static class Part {
        public String text;
    }
}