package com.project.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Projekt {
    private Integer projektId;
    private String nazwa;
    private String opis;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime dataCzasUtworzenia;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime dataCzasModyfikacji;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataOddania;

    // Musisz samodzielnie wygenerować tu Gettery i Settery dla tych pól
    // (lub dodać adnotację @Data z Lomboka).

    public Integer getProjektId() { return projektId; }
    public void setProjektId(Integer projektId) { this.projektId = projektId; }
    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }
    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }
    public LocalDateTime getDataCzasUtworzenia() { return dataCzasUtworzenia; }
    public void setDataCzasUtworzenia(LocalDateTime dataCzasUtworzenia) { this.dataCzasUtworzenia = dataCzasUtworzenia; }
    public LocalDateTime getDataCzasModyfikacji() { return dataCzasModyfikacji; }
    public void setDataCzasModyfikacji(LocalDateTime dataCzasModyfikacji) { this.dataCzasModyfikacji = dataCzasModyfikacji; }
    public LocalDate getDataOddania() { return dataOddania; }
    public void setDataOddania(LocalDate dataOddania) { this.dataOddania = dataOddania; }
}