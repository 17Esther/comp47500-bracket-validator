package ie.ucd.comp47500.stackvalidator;

import java.util.NoSuchElementException;

/**
 * A stack of chars backed by a resizable array.
 */
public final class ArrayCharStack {
    private static final int DEFAULT_CAPACITY = 16;

    private char[] elements;
    private int size;

    public ArrayCharStack() {
        this(DEFAULT_CAPACITY);
    }

    public ArrayCharStack(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be > 0");
        }
        this.elements = new char[initialCapacity];
        this.size = 0;
    }

    public void push(char value) {
        ensureCapacity(size + 1);
        elements[size++] = value;
    }

    public char pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Cannot pop from an empty stack");
        }
        char value = elements[--size];
        elements[size] = '\0';
        return value;
    }

    public char peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Cannot peek from an empty stack");
        }
        return elements[size - 1];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return elements.length;
    }

    public void clear() {
        while (size > 0) {
            elements[--size] = '\0';
        }
    }

    private void ensureCapacity(int required) {
        if (required <= elements.length) {
            return;
        }
        int newCapacity = elements.length;
        while (newCapacity < required) {
            newCapacity *= 2;
        }
        char[] newBuffer = new char[newCapacity];
        System.arraycopy(elements, 0, newBuffer, 0, size);
        elements = newBuffer;
    }
}
