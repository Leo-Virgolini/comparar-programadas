package ar.com.leo.comparar.model;

import java.util.Objects;

public class ArticuloProgramada {

    private String articulo;
    private String talle;
    private Integer punto;
    private Integer producir;


    @Override
    public String toString() {
        return "ArticuloProgramada{" +
                "articulo='" + articulo + '\'' +
                ", talle='" + talle + '\'' +
                ", producir=" + producir +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticuloProgramada that = (ArticuloProgramada) o;
        return articulo.equals(that.articulo) && talle.equals(that.talle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articulo, talle);
    }

    public String getArticulo() {
        return articulo;
    }

    public void setArticulo(String articulo) {
        this.articulo = articulo;
    }

    public String getTalle() {
        return talle;
    }

    public void setTalle(String talle) {
        this.talle = talle;
    }

    public Integer getPunto() {
        return punto;
    }

    public void setPunto(Integer punto) {
        this.punto = punto;
    }

    public Integer getProducir() {
        return producir;
    }

    public void setProducir(Integer producir) {
        this.producir = producir;
    }
}
