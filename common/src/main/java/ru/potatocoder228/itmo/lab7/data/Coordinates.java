package ru.potatocoder228.itmo.lab7.data;


import java.io.Serializable;

/**
 * Класс, описывающий координаты x и y
 */

public class Coordinates implements Serializable {
    private static final long serialVersionUID = 669;
    private final long x;//Максимальное значение поля: 436
    private final Double y;//Максимальное значение поля: 101, Поле не может быть null

    /**
     * Конструктор, задающий координаты
     *
     * @param i координата x
     * @param k координата y
     */

    public Coordinates(long i, Double k) {
        this.x = i;
        this.y = k;
    }

    /**
     * Возвращает поля класса в понятном для пользователя виде
     *
     * @return fields
     */

    public String toString() {
        return "(" + x + ";" + y.intValue() + ")";
    }

    /**
     * Возвращает координату x
     *
     * @return x
     */

    public long getX() {
        return x;
    }

    /**
     * Возвращает координату y
     *
     * @return y
     */

    public Double getY() {
        return y;
    }

    public boolean validate() {
        return !(x > 436 || y > 101 || Double.isNaN(y));
    }
}
