package ar.com.leo.comparar.fx.service;

import ar.com.leo.comparar.model.ArticuloProgramada;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProgramadaService extends Service<List<List<ArticuloProgramada>>> {

    private final File programadaAntiguaPdf;
    private final File programadaNuevaPdf;
    private final TextArea antiguaTextArea;
    private final TextArea nuevaTextArea;

    public ProgramadaService(File programadaAntiguaPdf, File programadaNuevaPdf, TextArea antiguaTextArea, TextArea nuevaTextArea) {
        this.programadaAntiguaPdf = programadaAntiguaPdf;
        this.programadaNuevaPdf = programadaNuevaPdf;
        this.antiguaTextArea = antiguaTextArea;
        this.nuevaTextArea = nuevaTextArea;
    }

    @Override
    protected Task<List<List<ArticuloProgramada>>> createTask() {
        return new Task<>() {
            @Override
            protected List<List<ArticuloProgramada>> call() throws Exception {

                final List<ArticuloProgramada> articulosViejos = cargarProgramada(programadaAntiguaPdf, antiguaTextArea);
                final List<ArticuloProgramada> articulosNuevos = cargarProgramada(programadaNuevaPdf, nuevaTextArea);

                // Track added, modified, and deleted entries
                final List<ArticuloProgramada> added = new ArrayList<>();
                final List<ArticuloProgramada> modified = new ArrayList<>();
                final List<ArticuloProgramada> deleted = new ArrayList<>();
                final List<ArticuloProgramada> result = new ArrayList<>();

                // Identify added, modified, and removed items
                for (ArticuloProgramada articuloNuevo : articulosNuevos) {
                    if (articulosViejos.contains(articuloNuevo)) {
                        final ArticuloProgramada articuloViejo = articulosViejos.get(articulosViejos.indexOf(articuloNuevo));
                        if (!articuloViejo.getProducir().equals(articuloNuevo.getProducir())) {
                            // Modified
                            modified.add(articuloNuevo);
                            ArticuloProgramada resultArticulo = new ArticuloProgramada();
                            resultArticulo.setArticulo(articuloNuevo.getArticulo());
                            resultArticulo.setTalle(articuloNuevo.getTalle());
                            resultArticulo.setProducir(articuloNuevo.getProducir() - articuloViejo.getProducir());
                            result.add(resultArticulo);
                        }
                    } else {
                        // Added
                        added.add(articuloNuevo);
                        result.add(articuloNuevo);
                    }
                }

                for (ArticuloProgramada articuloViejo : articulosViejos) {
                    if (!articulosNuevos.contains(articuloViejo)) {
                        // Deleted
                        deleted.add(articuloViejo);
                        ArticuloProgramada resultArticulo = new ArticuloProgramada();
                        resultArticulo.setArticulo(articuloViejo.getArticulo());
                        resultArticulo.setTalle(articuloViejo.getTalle());
                        resultArticulo.setProducir(-articuloViejo.getProducir());
                        result.add(resultArticulo);
                    }
                }

                result.sort(Comparator.comparingDouble((ArticuloProgramada articuloProgramada) -> Integer.valueOf(articuloProgramada.getArticulo().substring(0, articuloProgramada.getArticulo().indexOf(","))))
                        .thenComparing((ArticuloProgramada articuloProgramada) -> articuloProgramada.getTalle()));

                final List<List<ArticuloProgramada>> resultados = new ArrayList<>();
                resultados.add(added);
                resultados.add(modified);
                resultados.add(deleted);
                resultados.add(result);

                return resultados;
            }
        };
    }

    private List<ArticuloProgramada> cargarProgramada(File programadaArchivo, TextArea textArea) {
        try (PDDocument document = Loader.loadPDF(programadaArchivo)) {
            final PDFTextStripper stripper = new PDFTextStripper();
            // This example uses sorting, but in some cases it is more useful to switch it off,
            // e.g. in some files with columns where the PDF content stream respects the
            // column order.
            stripper.setSortByPosition(true);
            // Set the page interval to extract. If you don't, then all pages would be extracted.
//            stripper.setStartPage(1);
//            stripper.setEndPage(document.getNumberOfPages());
            final String page = stripper.getText(document);
            // If the extracted text is empty or gibberish, please try extracting text
            // with Adobe Reader first before asking for help. Also read the FAQ
            // on the website:
            // https://pdfbox.apache.org/2.0/faq.html#text-extraction

            final List<ArticuloProgramada> articulos = new ArrayList<>();
            Integer total = 0;
            // Split the page content into lines
            final String[] lines = page.split("\n");
            // Loop through each line and store it in a variable if it contains a comma
            for (String line : lines) {
                if (line.contains(",")) {
                    final String[] words = line.split(" ");
                    final ArticuloProgramada articulo = new ArticuloProgramada();
                    articulo.setArticulo(words[0]);
                    articulo.setTalle(words[words.length - 4]);
                    articulo.setProducir(Integer.parseInt(words[words.length - 3]));
                    articulos.add(articulo);
                    total += Integer.parseInt(words[words.length - 3]);
                }
            }
            final int finalTotal = total;
            Platform.runLater(() -> textArea.setText("TOTAL: " + finalTotal));

            return articulos;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

