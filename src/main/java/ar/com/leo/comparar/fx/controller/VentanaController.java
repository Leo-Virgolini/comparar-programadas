package ar.com.leo.comparar.fx.controller;

import ar.com.leo.comparar.fx.Main;
import ar.com.leo.comparar.fx.service.ProgramadaService;
import ar.com.leo.comparar.model.ArticuloProgramada;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class VentanaController implements Initializable {

    @FXML
    private TableView<ArticuloProgramada> agregadoTable;
    @FXML
    private TableView<ArticuloProgramada> modificadoTable;
    @FXML
    private TableView<ArticuloProgramada> eliminadoTable;
    @FXML
    private TextField programadaAntiguaUbicacion;
    @FXML
    private TextField programadaNuevaUbicacion;
    @FXML
    private TextArea antiguaTextArea;
    @FXML
    private TextArea nuevaTextArea;
    @FXML
    private TextArea resultTextArea;

    private File programadaAntiguaPdf;
    private File programadaNuevaPdf;
    private ObservableList<ArticuloProgramada> addedObservable;
    private ObservableList<ArticuloProgramada> modifiedObservable;
    private ObservableList<ArticuloProgramada> deletedObservable;

    private String lastUsedPath;

    public void initialize(URL url, ResourceBundle rb) {
        agregadoTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("articulo"));
        agregadoTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("talle"));
        agregadoTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("producir"));
        modificadoTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("articulo"));
        modificadoTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("talle"));
        modificadoTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("producir"));
        eliminadoTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("articulo"));
        eliminadoTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("talle"));
        eliminadoTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("producir"));
    }

    @FXML
    public void buscarProgramadaAntigua(ActionEvent event) {
        antiguaTextArea.clear();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Elige archivo .PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        lastUsedPath = getLastUsedPath();
        if (lastUsedPath != null && !lastUsedPath.isEmpty()) {
            File initialDirectory = new File(lastUsedPath);
            if (initialDirectory.exists() && initialDirectory.isDirectory()) {
                fileChooser.setInitialDirectory(initialDirectory);
            }
        }
        programadaAntiguaPdf = fileChooser.showOpenDialog(Main.stage);
        if (programadaAntiguaPdf != null) {
            programadaAntiguaUbicacion.setText(programadaAntiguaPdf.getAbsolutePath());
            saveLastUsedPath(programadaAntiguaPdf.getParent());
        } else {
            programadaAntiguaUbicacion.clear();
        }
    }

    @FXML
    public void buscarProgramadaNueva(ActionEvent event) {
        nuevaTextArea.clear();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Elige archivo .PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        lastUsedPath = getLastUsedPath();
        if (lastUsedPath != null && !lastUsedPath.isEmpty()) {
            File initialDirectory = new File(lastUsedPath);
            if (initialDirectory.exists() && initialDirectory.isDirectory()) {
                fileChooser.setInitialDirectory(initialDirectory);
            }
        }
        programadaNuevaPdf = fileChooser.showOpenDialog(Main.stage);
        if (programadaNuevaPdf != null) {
            programadaNuevaUbicacion.setText(programadaNuevaPdf.getAbsolutePath());
            saveLastUsedPath(programadaNuevaPdf.getParent());
        } else {
            programadaNuevaUbicacion.clear();
        }
    }

    @FXML
    private void comparar(ActionEvent event) {
        agregadoTable.getItems().clear();
        modificadoTable.getItems().clear();
        eliminadoTable.getItems().clear();
        resultTextArea.clear();
        if (programadaAntiguaPdf != null && programadaAntiguaPdf.isFile() && programadaNuevaPdf != null && programadaNuevaPdf.isFile()) {
            ProgramadaService service = new ProgramadaService(programadaAntiguaPdf, programadaNuevaPdf, antiguaTextArea, nuevaTextArea);
            service.setOnSucceeded(e -> {
                final List<List<ArticuloProgramada>> resultados = service.getValue();
                if (resultados != null) {
                    final List<ArticuloProgramada> added = resultados.get(0);
                    final List<ArticuloProgramada> modified = resultados.get(1);
                    final List<ArticuloProgramada> deleted = resultados.get(2);
                    final List<ArticuloProgramada> result = resultados.get(3);

                    addedObservable = FXCollections.observableArrayList(added);
                    modifiedObservable = FXCollections.observableArrayList(modified);
                    deletedObservable = FXCollections.observableArrayList(deleted);

                    agregadoTable.setItems(addedObservable);
                    modificadoTable.setItems(modifiedObservable);
                    eliminadoTable.setItems(deletedObservable);

                    for (ArticuloProgramada articuloProgramada : result) {
                        final String art = articuloProgramada.getArticulo() + " T." + articuloProgramada.getTalle() + ": " + articuloProgramada.getProducir() + "\n";
                        resultTextArea.appendText(art);
                    }
                }
            });
            service.setOnFailed(e -> {
                service.getException().printStackTrace();
            });
            service.start();
        } else {
            antiguaTextArea.setText("Ubicaciones erróneas.");
            nuevaTextArea.setText("Ubicaciones erróneas.");
        }
    }

    private String getLastUsedPath() {
        Preferences preferences = Preferences.userNodeForPackage(VentanaController.class);
        return preferences.get("path", null);
    }

    private void saveLastUsedPath(String path) {
        Preferences preferences = Preferences.userNodeForPackage(VentanaController.class);
        preferences.put("path", path);
    }

}
