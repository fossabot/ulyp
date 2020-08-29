package com.ulyp.ui.code;

import javafx.embed.swing.SwingNode;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SourceCodeView extends SwingNode {

    private final RSyntaxTextArea textArea;

    public SourceCodeView() {

        textArea = new RSyntaxTextArea();
        textArea.setPreferredSize(new Dimension(5000, 5000));

        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);

        Theme theme;
        try {
            theme = Theme.load(getClass().getResourceAsStream("/rsyntax-dark.xml"));
            theme.apply(textArea);
        } catch (IOException e) {
            throw new RuntimeException("Could not load theme", e);
        }

        setContent(textArea);
    }

    public void setText(SourceCode code) {
        SwingUtilities.invokeLater(
                () -> {
                    this.textArea.setText(code.getCode());
                }
        );
    }
}
