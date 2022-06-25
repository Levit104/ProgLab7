package ru.potatocoder228.itmo.lab7.data;


import java.io.Serializable;

/**
 * Класс, описывающий пещеру дракона
 */

public class DragonCave implements Serializable {
    private static final long serialVersionUID = 670;
    private final float depth;

    /**
     * Конструктор, задающий координаты
     *
     * @param depth глубина пещеры
     */

    public DragonCave(float depth) {
        this.depth = depth;
    }

    /**
     * Возвращает глубину пещеры
     *
     * @return depth
     */

    public float getDepth() {
        return depth;
    }
}