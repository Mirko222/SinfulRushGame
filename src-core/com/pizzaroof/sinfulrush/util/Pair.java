package com.pizzaroof.sinfulrush.util;

/**coppia di elementi generici*/
public class Pair <T, T2> {
    public T v1;
    public T2 v2;

    public Pair() {}

    public Pair(T a, T2 b) {
        v1 = a;
        v2 = b;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            return v1.equals(((Pair)obj).v1) && v2.equals(((Pair)obj).v2);
        }catch(Exception e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (v1.hashCode()+""+v2.hashCode()).hashCode();
    }
}
