package com.ulyp.ui;

import com.ulyp.transport.TMethodTraceLogUploadRequest;
import com.ulyp.ui.util.MethodTraceTree;
import com.ulyp.ui.util.MethodTraceTreeUtils;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class FXMLStackTraceViewController implements Initializable {

    public FXMLStackTraceViewController() {
    }

    public FXMLStackTraceViewController(TabPane processTabPane) {
        this.processTabPane = processTabPane;
    }

    @FXML
    public VBox primaryPane;

    @FXML
    public TabPane processTabPane;

    @FXML
    public TextField searchField;

    public Map<String, ProcessTab> mainClassToTab = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void onMethodTraceTreeUploaded(TMethodTraceLogUploadRequest request) {
        MethodTraceTree methodTraceTree = MethodTraceTreeUtils.from(request);
        Platform.runLater(() -> addTree(request, methodTraceTree));
    }

    private void addTree(TMethodTraceLogUploadRequest request, MethodTraceTree tree) {
        String mainClassName = request.getMainClassName();

        ProcessTab processTab = mainClassToTab.get(mainClassName);
        if (processTab == null) {
            Consumer<Event> closer = ev -> {
                ProcessTab processTabToRemove = mainClassToTab.remove(mainClassName);
                processTabPane.getTabs().remove(processTabToRemove.getTab());
            };

            mainClassToTab.put(request.getMainClassName(), processTab = new ProcessTab(processTabPane, mainClassName, closer));
            processTabPane.getTabs().add(processTab.getTab());
        }

        processTab.getTabList().add(tree, Duration.ofMillis(request.getLifetimeMillis()));
    }

    public void call(Event event) {
        processTabPane.getTabs().clear();
        mainClassToTab.clear();
    }

    public void tabPaneKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.D) {
            for (ProcessTab processTab : mainClassToTab.values()) {
                if (processTab.getTab().isSelected()) {
                    if (event.getCode() == KeyCode.A) {
                        processTab.getTabList().selectNextLeftTab();
                    } else {
                        processTab.getTabList().selectNextRightTab();
                    }
                    return;
                }
            }
        }
    }

    public void onKeyReleased(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER) {
            return;
        }

        mainClassToTab.values().forEach(
                processTab -> processTab.getTabList().applySearch(searchField.getText().trim())
        );
    }
}
