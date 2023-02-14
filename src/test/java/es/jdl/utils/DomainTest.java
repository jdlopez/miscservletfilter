package es.jdl.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

public class DomainTest {
    private String sField;
    private int iField;
    private double dblField;
    private Integer intField;
    private Double doubleField;
    private LocalDateTime dtField;
    private LocalDate dField;
    private Date dateField;
    private BigDecimal bdField;

    public String getsField() {
        return sField;
    }

    public void setsField(String sField) {
        this.sField = sField;
    }

    public int getiField() {
        return iField;
    }

    public void setiField(int iField) {
        this.iField = iField;
    }

    public LocalDateTime getDtField() {
        return dtField;
    }

    public void setDtField(LocalDateTime dtField) {
        this.dtField = dtField;
    }

    public LocalDate getdField() {
        return dField;
    }

    public void setdField(LocalDate dField) {
        this.dField = dField;
    }

    public double getDblField() {
        return dblField;
    }

    public void setDblField(double dblField) {
        this.dblField = dblField;
    }

    public Date getDateField() {
        return dateField;
    }

    public void setDateField(Date dateField) {
        this.dateField = dateField;
    }

    public BigDecimal getBdField() {
        return bdField;
    }

    public void setBdField(BigDecimal bdField) {
        this.bdField = bdField;
    }

    public Integer getIntField() {
        return intField;
    }

    public void setIntField(Integer intField) {
        this.intField = intField;
    }

    public Double getDoubleField() {
        return doubleField;
    }

    public void setDoubleField(Double doubleField) {
        this.doubleField = doubleField;
    }

    @Override
    public String toString() {
        return "DomainTest{" +
                "sField='" + sField + '\'' +
                ", iField=" + iField +
                ", dblField=" + dblField +
                ", intField=" + intField +
                ", doubleField=" + doubleField +
                ", dtField=" + dtField +
                ", dField=" + dField +
                ", dateField=" + dateField +
                ", bdField=" + bdField +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainTest that = (DomainTest) o;
        return getiField() == that.getiField()
                && Double.compare(that.getDblField(), getDblField()) == 0
                && Objects.equals(getsField(), that.getsField())
                && Objects.equals(getIntField(), that.getIntField())
                && Objects.equals(getDoubleField(), that.getDoubleField())
                && Objects.equals(getDtField(), that.getDtField())
                && Objects.equals(getdField(), that.getdField())
                && Objects.equals(getDateField(), that.getDateField())
                && Objects.equals(getBdField(), that.getBdField());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getsField(), getiField(), getDblField(), getIntField(),
                getDoubleField(), getDtField(), getdField(), getDateField(), getBdField());
    }
}
