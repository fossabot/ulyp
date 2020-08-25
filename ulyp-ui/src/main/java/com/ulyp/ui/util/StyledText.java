package com.ulyp.ui.util;

import javafx.scene.text.Text;

public class StyledText {

    public static Text of(String content, String style) {
        Text text = new Text(content);
        text.getStyleClass().add(style);
        return text;
    }
}
